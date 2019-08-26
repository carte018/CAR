#!/bin/bash

# Check for existing keys -- create new ones if they don't already exist
#

if [ ! -e /opt/shibboleth-idp/credentials/car_idp.key ]
then
    openssl req -newkey rsa:2048 -nodes -keyout /opt/shibboleth-idp/credentials/car_idp.key -x509 -days 365 -out /opt/shibboleth-idp/credentials/car_idp.crt
fi
 
exec $*
