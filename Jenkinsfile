pipeline {
    agent any

    parameters {
        string(name: 'INGRESS_HOST', defaultValue: 'app.lokeshwaffle.in', description: 'Public hostname for this environment')
        string(name: 'ACM_CERTIFICATE_ARN', defaultValue: '', description: 'ACM certificate ARN covering INGRESS_HOST')
    }

    tools {
        // These names must match the installations configured in Manage Jenkins.
        jdk 'JDK17'
        maven 'Maven-3.9.16'
    }

    environment {
        AWS_REGION = 'ap-south-1'
        EKS_CLUSTER = 'pulsesg-dev-eks'
        ECR_REPOSITORY = 'app1'
        HELM_CHART = 'docker-images/invocation/charts/appstoremanagement'
        HELM_RELEASE = 'appstoremanagement'
        KUBE_NAMESPACE = 'pulsesg-dev'
        DOCKERFILE = 'docker-images/appstoremanagement/Dockerfile'
        JAR_FILE = 'pulsesg-appstore-management-service-0.0.1-SNAPSHOT.jar'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Validate Deployment Parameters') {
            steps {
                sh '''
                    set -eu
                    test -n "$INGRESS_HOST"
                    test -n "$ACM_CERTIFICATE_ARN"
                '''
            }
        }

        stage('Maven Build & Test') {
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

        stage('Docker Push') {
            steps {
                sh '''
                    set -eu
                    docker push "$IMAGE_URI"
                '''
            }
        }

        stage('Verify AWS Identity') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-jenkins']]) {
                    sh '''
                        set -eu
                        aws sts get-caller-identity
                    '''
                }
            }
        }

        stage('Configure EKS Access') {
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
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-jenkins'
                ]]) {
                    sh '''
                        set -eu
                        helm upgrade --install "$HELM_RELEASE" "$HELM_CHART" \
                          --namespace "$KUBE_NAMESPACE" \
                          --set-string image.repository="$ECR_REGISTRY/$ECR_REPOSITORY" \
                          --set-string image.tag="$IMAGE_TAG" \
                          --set ingress.enabled=true \
                          --set ingress.className=alb \
                          --set-string ingress.hosts[0].host="$INGRESS_HOST" \
                          --set-string ingress.annotations.alb\\.ingress\\.kubernetes\\.io/certificate-arn="$ACM_CERTIFICATE_ARN" \
                          --wait \
                          --timeout 10m
                    '''
                }
            }
        }

        stage('Rollout Status') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-jenkins'
                ]]) {
                    sh '''
                        set -eu
                        kubectl rollout status deployment/appstoremanagement \
                            --namespace "$KUBE_NAMESPACE" \
                            --timeout=10m
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Build, image push, and Helm deployment completed successfully.'
        }
        failure {
            echo 'Pipeline failed. Review the failed stage above; credentials are not printed by this pipeline.'
        }
        always {
            deleteDir()
        }
    }
}