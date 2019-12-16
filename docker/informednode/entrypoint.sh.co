#!/bin/bash

#
# Update the configuration files in the data volume
#

mkdir -p /etc/car/informed
cp /tmp/hibernate.cfg.xml /etc/car/informed/hibernate.cfg.xml
cp /tmp/informed.conf /etc/car/informed/informed.conf

# Trust the SSL certificate we placed in the Apache server
keytool -importcert -file /tmp/ssl.pem -trustcacerts -cacerts -alias 'carapache' -storepass changeit -noprompt

# Fix the credential for the informeduser
sed -i'' 's/ResistanceIsFutile/'$INFORMEDUSER_PASSWORD'/g' /etc/car/informed/hibernate.cfg.xml

# Make sure tomcat8 can write to the logs and the webapps tree

chown -R tomcat8:tomcat8 /var/log/tomcat8
chown -R tomcat8:tomcat8 /var/lib/tomcat8/webapps

exec $*
