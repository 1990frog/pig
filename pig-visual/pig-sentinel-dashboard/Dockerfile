FROM dockerhub.clinbrain.com/jdk:1.8.0_202
ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY target/pig-sentinel-dashboard.jar /home/java/application-1.0-SNAPSHOT.jar

WORKDIR /home/java
ENTRYPOINT java "-Djava.security.egd=file:/dev/./urandom" ${JAVA_OPTS:=-Xms1024m -Xmx1024m} -jar /home/java/application-1.0-SNAPSHOT.jar ${BOOT_OPTIONS:='--server.port=8080'}
