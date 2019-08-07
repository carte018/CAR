#!/bin/bash

#
# Update the configuration files in the data volume
#

mkdir -p /etc/car/copsu
cp /tmp/hibernate.cfg.xml /etc/car/copsu/hibernate.cfg.xml
cp /tmp/copsu.conf /etc/car/copsu/copsu.conf

# Fix up password for copsuuser
sed -i'' 's/ResistanceIsFutile/'$COPSUUSER_PASSWORD'/g' /etc/car/copsu/hibernate.cfg.xml

# Make sure tomcat8 can write to the logs and the webapps tree

chown -R tomcat8:tomcat8 /var/log/tomcat8
chown -R tomcat8:tomcat8 /var/lib/tomcat8/webapps

exec $*
