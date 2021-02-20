FROM java:8

EXPOSE 4000

ADD pig-upms-biz.jar upms.jar

#COPY bootstrap.yml /

RUN bash -c 'touch pig-upms-biz.jar'

ENTRYPOINT ["java","-jar", "/upms.jar"]