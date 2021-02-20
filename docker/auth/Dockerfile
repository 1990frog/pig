FROM java:8

EXPOSE 3000

ADD pig-auth.jar auth.jar

#COPY bootstrap.yml /

RUN bash -c 'touch pig-auth.jar'

ENTRYPOINT ["java","-jar", "/auth.jar"]