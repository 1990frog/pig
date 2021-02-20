FROM java:8

EXPOSE 9999

ADD pig-gateway.jar gateway.jar

#COPY bootstrap.yml /

RUN bash -c 'touch pig-gateway.jar'

ENTRYPOINT ["java","-jar", "/gateway.jar"]