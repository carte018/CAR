#!/bin/bash

# Run the LDAP server
$*

# If the database isn't loaded, load it
if [ ! -e /var/lib/ldaploaded ] 
then
	ldapadd -x -H ldap://127.0.0.1:7389 -D 'cn=admin,cn=config' -w 'config' -f /tmp/eduperson.schema
	ldapadd -x -H ldap://127.0.0.1:7389 -D 'cn=admin,cn=config' -w 'config' -f /tmp/amberite.schema
	ldapadd -x -H ldap://127.0.0.1:7389 -D 'cn=admin,dc=amber,dc=org' -w 'admin' -f /tmp/people.ldif
	ldapadd -x -H ldap://127.0.0.1:7389 -D 'cn=admin,dc=amber,dc=org' -w 'admin' -f /tmp/idpuser.ldif
	ldapmodify -x -H ldap://127.0.0.1:7389 -D 'cn=admin,cn=config' -w 'config' -f /tmp/idpuser.access
	ldapadd -c -x -H ldap://127.0.0.1:7389 -D 'cn=admin,dc=amber,dc=org' -w 'admin' -f /tmp/users.ldif

	# Mark the database as loaded
	touch /var/lib/ldaploaded
fi

