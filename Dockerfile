FROM tomcat:8.5.39-jre8

COPY wls-servlet/resources/fonts/ /usr/share/fonts/

COPY wls-servlet/target/wls-servlet-2.3.war /usr/local/tomcat/webapps/fop.war