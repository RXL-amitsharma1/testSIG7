FROM tomcat:9.0.83-jdk8-corretto-al2

ENV JAVA_OPTS="-Xms2G -Xmx8G -XX:+CMSClassUnloadingEnabled -Duser.timezone=GMT -Duser.home=/opt/prod/conf -Dfile.encoding=UTF-8"

EXPOSE 8080

COPY ./build/libs/signal.war /usr/local/tomcat/webapps/signal.war
COPY ./build/libs/pvcc.war /usr/local/tomcat/webapps/pvcc.war
RUN rm -rf /usr/local/tomcat/conf/server.xml
COPY ./server.xml /usr/local/tomcat/conf/server.xml
RUN yum install -y hostname

CMD ["catalina.sh", "run"]




