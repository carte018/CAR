#!/bin/bash

#
# Update the configuration files in the data volume
#

mkdir -p /etc/car/car
cp /tmp/car.conf /etc/car/car/car.conf

# Trust the SSL certificate we placed in the Apache server
keytool -importcert -file /tmp/ssl.pem -trustcacerts -cacerts -alias 'carapache' -storepass changeit -noprompt

# Make sure tomcat8 can write to the logs and the webapps tree

chown -R tomcat8:tomcat8 /var/log/tomcat8
chown -R tomcat8:tomcat8 /var/lib/tomcat8/webapps

#
# Check to see if we have a signing key for responding to decision requests.
# If we don't, create one now. 
# If we do, leave it be.
#
if [ ! -e /var/www/carma/carmaprivkey.pk ]
then
  # Create a new signing key and store it in the proper location
  openssl req -newkey rsa:2048 -x509 -nodes -keyout /tmp/signing.key -new -out /tmp/signing.cert -subj '/C=UF/ST=Sector 7/L=Vulcan/O=United Federation of Planets/OU=Vulcan Homeworld/CN=carsigning' -sha256 -days 3650

  # Recast the key as a PKCS#8
  openssl pkcs8 -topk8 -inform PEM -outform DER -in /tmp/signing.key -out /tmp/signing.p8 -nocrypt

  # Put them in the right locations
  cp /tmp/signing.cert /var/www/carma/carmacert
  cp /tmp/signing.p8 /var/www/carma/carmaprivkey.p8
  
  # Build the directory for holding RH certificates
  mkdir -p /var/www/carma/rhcerts

fi


exec $*
