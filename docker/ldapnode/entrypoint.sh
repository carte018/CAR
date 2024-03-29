#!/bin/bash

# Run the LDAP server in background mode
if [ -e /var/lib/ldap/ldaploaded ]
then
  export KEEP_EXISTING_CONFIG=true
fi

$* &

sleep 20  # Give the LDAP server 20 seconds to complete initialization

# If the database isn't loaded, load it
if [ ! -e /var/lib/ldap/ldaploaded ] 
then
	ldapadd -x -H ldap://localhost -D 'cn=admin,cn=config' -w 'config' -f /tmp/eduperson.schema
	ldapadd -x -H ldap://localhost -D 'cn=admin,cn=config' -w 'config' -f /tmp/amberite.schema
	ldapadd -x -H ldap://localhost -D 'cn=admin,dc=amber,dc=org' -w 'admin' -f /tmp/people.ldif
	ldapadd -x -H ldap://localhost -D 'cn=admin,dc=amber,dc=org' -w 'admin' -f /tmp/idpuser.ldif
	ldapmodify -x -H ldap://localhost -D 'cn=admin,cn=config' -w 'config' -f /tmp/idpuser.access
	ldapadd -c -x -H ldap://localhost -D 'cn=admin,dc=amber,dc=org' -w 'admin' -f /tmp/users.ldif

	# Mark the database as loaded
	touch /var/lib/ldap/ldaploaded
fi
wait
