export CLASSPATH=/opt/shibboleth-idp/dist/webapp/WEB-INF/lib/*:/opt/shibboleth-idp/edit-webapp/WEB-INF/lib/*:/usr/local/tomcat/lib/*
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-amazon-corretto

rm -rf /opt/shibboleth-idp/webapp/*
mkdir -p /opt/shibboleth-idp/webapp
/bin/cp -R /opt/shibboleth-idp/dist/webapp/* /opt/shibboleth-idp/webapp/
rm -f /opt/shibboleth-idp/webapp/WEB-INF/lib/bcprov-jdk15on-1.51.jar
rm -f /opt/shibboleth-idp/webapp/WEB-INF/lib/slf4j-api-1.7.10.jar
rm -f /opt/shibboleth-idp/webapp/WEB-INF/lib/guava-18.0.jar
rm -f /opt/shibboleth-idp/webapp/WEB-INF/lib/httpclient-4.3.6.jar
rm -f /opt/shibboleth-idp/webapp/WEB-INF/lib/httpcore-4.3.3.jar
cd /opt/shibboleth-idp/edit-webapp/WEB-INF/classes
find . -name \*.java -print > file
javac @file
rm -f file
cd /opt/shibboleth-idp/bin/
./ant.sh build-war
cd /opt/shibboleth-idp/edit-webapp/WEB-INF/classes
