#!/bin/bash

#
# Update the configuration files in the data volume
#

mkdir -p /etc/car/caradmin
cp /tmp/caradmin.conf /etc/car/caradmin/caradmin.conf

# Trust the SSL certificate we placed in the Apache server
keytool -importcert -file /tmp/ssl.pem -trustcacerts -cacerts -alias 'carapache' -storepass changeit -noprompt

# Make sure tomcat8 can write to the logs and the webapps tree

chown -R tomcat8:tomcat8 /var/log/tomcat8
chown -R tomcat8:tomcat8 /var/lib/tomcat8/webapps

exec $*
