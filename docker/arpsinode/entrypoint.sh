#!/bin/bash

#
# Update the configuration files in the data volume
#

mkdir -p /etc/car/arpsi
cp /tmp/hibernate.cfg.xml /etc/car/arpsi/hibernate.cfg.xml
cp /tmp/arpsi.conf /etc/car/arpsi/arpsi.conf

# Fix the credential for the arpsiuser
sed -i'' 's/ResistanceIsFutile/'$ARPSIUSER_PASSWORD'/g' /etc/car/arpsi/hibernate.cfg.xml

# Make sure tomcat8 can write to the logs and the webapps tree

chown -R tomcat8:tomcat8 /var/log/tomcat8
chown -R tomcat8:tomcat8 /var/lib/tomcat8/webapps

exec $*
