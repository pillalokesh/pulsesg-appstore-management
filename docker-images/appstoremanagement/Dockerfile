FROM amazoncorretto:17.0.16-al2023-headful
RUN dnf upgrade -y
RUN dnf install dos2unix -y
RUN dnf install -y shadow-utils
ARG APPENV 
ENV APPENV=$APPENV
RUN mkdir -p /release && mkdir /config
RUN groupadd --gid 1000 puser 
RUN useradd -m -u 1000 -g 1000 puser
RUN chmod -R 755 /release
ADD ./target/pulsesg-appstore-management-service-0.0.1-SNAPSHOT.jar /release/app-build.jar
#COPY ./src/main/resources/config/application-dev.yml /config
COPY ./docker-images/appstoremanagement/entrypoint /release
COPY ./docker-images/appstoremanagement/Procfile /release
COPY ./docker-images/appstoremanagement/ENV /release
#RUN dos2unix /release/entrypoint /config/application-dev.yml
RUN chmod a+x /release/entrypoint /release/Procfile /release/ENV 
RUN rm -rf /usr/share/python3-wheels/*
USER puser
ENTRYPOINT [ "/release/entrypoint" ]
