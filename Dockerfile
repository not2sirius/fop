FROM tomcat:8.5-alpine

#https://www.freedesktop.org/wiki/Software/fontconfig/
RUN apk add --no-cache fontconfig

COPY wls-servlet/resources/fonts/ /usr/share/fonts/

COPY wls-servlet/target/wls-servlet-2.3.war /usr/local/tomcat/webapps/fop.war