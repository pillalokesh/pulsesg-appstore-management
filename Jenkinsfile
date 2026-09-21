pipeline {
    agent any

    parameters {
        choice(name: 'Environment', choices: ['dev', 'qa', 'uat', 'uateuc1', 'dmo', 'prod', 'prodeuc1', 'gsa', 'gsaprod'], description: 'Target deployment environment')
        string(name: 'Git Tag', defaultValue: '', description: 'Exact Git tag to checkout, build and deploy')
        string(name: 'Application', defaultValue: 'appstoremanagement', description: 'Kubernetes workload / Helm release name')
    }

    tools {
        // These names must match the installations configured in Manage Jenkins.
        jdk 'JDK17'
        maven 'Maven-3.9.16'
    }

    environment {
        AWS_REGION = 'ap-south-1'
        EKS_CLUSTER = "pulsesg-${params.Environment}-eks"
        KUBE_NAMESPACE = "pulsesg-${params.Environment}"
        ECR_REPOSITORY = 'app1'
        HELM_CHART = 'docker-images/invocation/charts/appstoremanagement'
        HELM_RELEASE = "${params.Application}"
        DOCKERFILE = 'docker-images/appstoremanagement/Dockerfile'
        JAR_FILE = 'pulsesg-appstore-management-service-0.0.1-SNAPSHOT.jar'
        // Immutable image version derived from the Git tag being deployed (never latest, never BUILD_NUMBER).
        IMAGE_TAG = "${params['Git Tag']}"
        APP_DOMAIN = 'lokeshwaffle.in'
    }

    stages {
        stage('Checkout Git Tag') {
            steps {
                script {
                    if (!params['Git Tag']?.trim()) {
                        error 'Git Tag parameter is required'
                    }
                }
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "refs/tags/${params['Git Tag']}"]],
                    extensions: scm.extensions,
                    userRemoteConfigs: scm.userRemoteConfigs
                ])
            }
        }

        stage('Frontend Test/Build') {
            when {
                expression { fileExists('package.json') || fileExists('frontend/package.json') }
            }
            steps {
                sh '''
                    set -eu
                    if [ -f package.json ]; then
                        npm ci
                        npm test --if-present
                        npm run build --if-present
                    elif [ -f frontend/package.json ]; then
                        cd frontend
                        npm ci
                        npm test --if-present
                        npm run build --if-present
                    fi
                '''
            }
        }

        stage('Backend Test/Build') {
            steps {
                sh 'mvn -B clean package'
                sh 'test -f "target/$JAR_FILE"'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    env.AWS_ACCOUNT_ID = ''
                    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-jenkins']]) {
                        env.AWS_ACCOUNT_ID = sh(
                            script: 'aws sts get-caller-identity --query Account --output text',
                            returnStdout: true
                        ).trim()
                    }
                    env.ECR_REGISTRY = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"
                    env.IMAGE_URI = "${env.ECR_REGISTRY}/${env.ECR_REPOSITORY}:${env.IMAGE_TAG}"
                }
                sh '''
                    set -eu
                    docker build --file "$DOCKERFILE" --tag "$IMAGE_URI" .
                '''
            }
        }

        stage('ECR Login') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-jenkins']]) {
                    sh '''
                        set -eu
                        aws ecr get-login-password --region "$AWS_REGION" \
                          | docker login --username AWS --password-stdin "$ECR_REGISTRY"
                    '''
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh '''
                    set -eu
                    docker push "$IMAGE_URI"
                '''
            }
        }

        stage('Configure EKS') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-jenkins']]) {
                    sh '''
                        set -eu
                        aws eks update-kubeconfig \
                          --region "$AWS_REGION" \
                          --name "$EKS_CLUSTER"
                        kubectl get namespace "$KUBE_NAMESPACE" >/dev/null 2>&1 || kubectl create namespace "$KUBE_NAMESPACE"
                    '''
                }
            }
        }

        stage('Helm Deploy') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-jenkins'],
                    string(credentialsId: 'lokeshwaffle-acm-certificate-arn', variable: 'ACM_CERTIFICATE_ARN')
                ]) {
                    sh '''
                        set -eu
                        helm upgrade --install "$HELM_RELEASE" "$HELM_CHART" \
                          --namespace "$KUBE_NAMESPACE" \
                          --set-string fullnameOverride="$HELM_RELEASE" \
                          --set-string image.repository="$ECR_REGISTRY/$ECR_REPOSITORY" \
                          --set-string image.tag="$IMAGE_TAG" \
                          --set ingress.enabled=true \
                          --set ingress.className=alb \
                          --set-string ingress.hosts[0].host="$APP_DOMAIN" \
                          --set-string ingress.annotations.alb\\.ingress\\.kubernetes\\.io/certificate-arn="$ACM_CERTIFICATE_ARN" \
                          --wait \
                          --timeout 10m
                    '''
                }
            }
        }

        stage('Rollout Verification') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-jenkins'
                ]]) {
                    sh '''
                        set -eu
                        kubectl rollout status deployment/"$HELM_RELEASE" \
                            --namespace "$KUBE_NAMESPACE" \
                            --timeout=10m
                    '''
                }
            }
        }

        stage('Health Verification') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-jenkins'
                ]]) {
                    sh '''
                        set -eu
                        for i in $(seq 1 10); do
                            READY=$(kubectl get deployment "$HELM_RELEASE" --namespace "$KUBE_NAMESPACE" -o jsonpath='{.status.readyReplicas}')
                            if [ -n "$READY" ] && [ "$READY" -ge 1 ]; then
                                exit 0
                            fi
                            sleep 6
                        done
                        echo "Deployment did not become ready in time" >&2
                        exit 1
                    '''
                }
            }
        }

        stage('Final Deployment Summary') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-jenkins'
                ]]) {
                    sh '''
                        set -eu
                        POD_NAME=$(kubectl get pods --namespace "$KUBE_NAMESPACE" \
                            -l app.kubernetes.io/instance="$HELM_RELEASE" \
                            -o jsonpath='{.items[0].metadata.name}')
                        echo "Application: pulsesg-appstore-management"
                        echo "Domain: https://$APP_DOMAIN"
                        echo "Pod: $POD_NAME"
                    '''
                }
            }
        }
    }

    post {
        always {
            deleteDir()
        }
    }
}