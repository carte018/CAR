#!/bin/bash

#
# Update the configuration files in the data volume
#

mkdir -p /etc/car/icm
cp /tmp/hibernate.cfg.xml /etc/car/icm/hibernate.cfg.xml
cp /tmp/icm.conf /etc/car/icm/icm.conf

# Trust the SSL certificate we placed in the Apache server
keytool -importcert -file /tmp/ssl.pem -trustcacerts -cacerts -alias 'carapache' -storepass changeit -noprompt

# Fix the credential for the icmuser
sed -i'' 's/ResistanceIsFutile/'$ICMUSER_PASSWORD'/g' /etc/car/icm/hibernate.cfg.xml

# Make sure tomcat8 can write to the logs and the webapps tree

chown -R tomcat8:tomcat8 /var/log/tomcat8
chown -R tomcat8:tomcat8 /var/lib/tomcat8/webapps

exec $*
