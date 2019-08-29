#!/bin/bash

#
# Master script to perform localization as necessary and then
# execute docker-compose as necessary to build and deploy the relevant
# Docker containers for running a standalone CAR instance.
#

parse_args() {
  POSITIONAL=()
  while [[ $# -gt 0 ]]
  do
    key="$1"
    case "$key" in 
	-q)
		QUIET=yes     # currently supported
		shift
		;;
	-b)
		REBUILD=yes   # not yet supported
		shift
		;;
	-s)
		RESTART=yes   # not yet supported
		shift
		;;
	-k)
		SKUNKWORKS=yes   # beta support
		shift
		;;
	*)
		shift
		;;
    esac
  done
  set -- "${POSITIONAL[@]}"
}

SKUNKWORKS=no
RESTART=no
REBUILD=no
QUIET=no

parse_args $*

if [ $QUIET == "yes" -a $SKUNKWORKS == "yes" ]
then
  echo "You have requested both quiet operation and skunkworks semantics."
  echo "If your last build was not in skunkworks mode, results may not be"
  echo "what you expect.  It is recommended that you run in non-quiet "
  echo "mode if this is your first skunkworks build."
  echo "Do you really want to run in quiet mode *and* skunkworks mode? [y/n]]"
  read qands
  if [ $qands != "y" ]
  then
    echo "exiting."
    exit
  fi
fi
#
# Prepare the compose template
#
# Skunkworks builds include building, configuring and populating
# test data into an LDAP and a Shibboleth IDP and setting up a group of 
# trivial test relying parties behind the apache-sp environment.  
#
# Non-skunkworks builds only build and deploy the CAR installation and 
# allow for configuring the CAR instance to interact with external
# RHs and RPs.
#
# If you have an existing skunkworks environment with an existing test IDP or 
# another test resource holder you'd like to use, use the non-skunkworks build and
# answer the build questions so as to integrate with your existing environment. 
#
# If you don't have an existing skunkworks environment or you want to try CAR out in 
# a bespoke test environment before trying it in our own test environment, use the 
# skunkworks build.
#


if [ "$SKUNKWORKS" == "yes" ]
then
  cp docker-compose.skunkworks docker-compose.yml.tmpl
  #
  # And build the customized base image from the TAP IDP image
  #
  (cd clean-idp; docker build --rm -t amber/idp-base .)
else
  cp docker-compose.caronly docker-compose.yml.tmpl
fi

if [ -e ./config.prev ]
then
  source ./config.prev
fi

#
# Create an SSL certificate to use in the apache configuration
# 
# Required for both caronly and skunkworks builds
#
if [ $QUIET == "yes" ]
then
  if [ ! -n $apache_fqdn ]
  then
    apache_fqdn="localhost"
  fi
else
echo
echo "We first need to determine the hostname that will be used by clients (browsers) to"
echo "access the components of this deployment.  In many cases, for testing purposes, you"
echo "may deploy the various components onto a personal workstation or laptop, in which "
echo "case you may access the components via 'localhost'.  If you are deploying onto a "
echo "dedicated test server shared among multiple clients, you will need to specify the "
echo "FQDN of the server as it will be presented by the clients when making HTTP connections."
echo
echo "Enter the FQDN of the server you're deploying to. [ $apache_fqdn ]"
read afq
if [ -n $afq ]
then
  apache_fqdn="$afq"
fi
fi

cat ssl.cnf.tmpl | sed 's/%apache_fqdn%/'$apache_fqdn'/' > ssl.cnf

openssl req -newkey rsa:2048 -x509 -nodes -keyout ssl.key -new -out ssl.pem -subj '/C=UF/ST=Sector 1/L=Earth/O=United Federation of Planets/OU=StarFleet Academy/CN=localhost' -config ssl.cnf -sha256 -days 3650
cp ssl.key apache-sp/ssl.key
cp ssl.pem apache-sp/ssl.pem

#
# If we aren't called with the "-q" flag, update the docker-compose script with credential information
#
# Required for both caronly and skunkworks builds.
# Varies according to build type
#

if [ "$QUIET" != "yes" ]
then
 echo "The components of the CAR system rely on database tables to store various information."
 echo "Access to those database tables requires the use of schema users.  Before we begin, we need"
 echo "to set passwords for those schema users (and for the defautl MariaDB users the database will"
 echo "use to operate)."
 echo
 echo "Enter the password you wish to use for the 'root' user in your CAR database: [$mysql_root_password]: "
 read arp
 if [ -n "$arp" ]
 then
  mysql_root_password="$arp"
  mysql_root_password=`echo $mysql_root_password | sed 's/\//_/g' | sed 's/ /_/g'`
 fi
 echo "Enter the password you wish to use for the 'replcation' user in your CAR database: [$mysql_replication_password]: "
 read aep
 if [ -n "$aep" ]
 then
  mysql_replication_password="$aep";
  mysql_replication_password=`echo $mysql_replication_password | sed 's/\//_/g' | sed 's/ /_/g'`
 fi
 echo "Enter the password you wish to use for the 'arpsiuser' user in your CAR database.  This password"
 echo "will be associated with the 'arpsiuser' the ARPSI component uses to access the database: [$mysql_arpsi_password]:"
 read aap
 if [ -n "$aap" ]
 then
  mysql_arpsi_password="$aap"
  mysql_arpsi_password=`echo $mysql_arpsi_password | sed 's/\//_g/' | sed 's/ /_/g'`
 fi
 echo "Enter the password you wish to use for the 'copsuuser' user in your CAR database.  This password"
 echo "will be associated with the 'copsuuser' the COPSU component uses to access the database: [$mysql_copsu_password]:"
 read acp
 if [ -n "$acp" ]
 then
  mysql_copsu_password="$acp"
  mysql_copsu_password=`echo $mysql_copsu_password | sed 's/\//_g/' | sed 's/ /_/g'`
 fi
 echo "Enter the password you wish to use for the 'icmuser' user in your CAR database.  This password"
 echo "will be associated with the 'icmuser' the CARMA component uses to manage metapolicies in the database: [$mysql_icm_password]:"
 read aip
 if [ -n "$aip" ]
 then
  mysql_icm_password="$aip" 
  mysql_icm_password=`echo $mysql_icm_password | sed 's/\//_g/' | sed 's/ /_/g'`
 fi
 echo "Enter the password you wish to use for the 'informeduser' user in your CAR database.  This password"
 echo "will be associated with the 'informeduser' the CARMA component uses to manage information content in the database: [$mysql_informed_password]:"
 read anp
 if [ -n "$anp" ]
 then
  mysql_informed_password="$anp"
  mysql_informed_password=`echo $mysql_informed_password | sed 's/\//_g/' | sed 's/ /_/g'`
 fi
 if [ $SKUNKWORKS != "yes" ]
 then
 echo
 echo
 echo "Components of the CARMA (like the icm) must make requests to the"
 echo "ARPSI and COPSU APIs in order to do their work.  Since the ARPSI"
 echo "and COPSU APIs require authentication, the CARMA needs credentials"
 echo "it can use to authenticate to the APIs."
 echo "If you are going to use the null authentication plugin, your choice"
 echo "of username and password/credential will not matter, and you can"
 echo "anything you like here.  If you are using the Kerberos plugin"
 echo "in the ARPSI and COPSU, you will need to specify a valid Kerberos"
 echo "principal as the username and its password when prompted below."
 echo
 echo "Enter the username the CARMA should use to authenticate to the"
 echo "ARPSI and COPSU: [$carma_user]: "
 read cu
 if [ -n "$cu" ]
 then
  carma_user="$cu"
 fi
 echo "Enter the password or credential for the $carma_user user"
 echo "[$carma_password]: "
 read cpw
 if [ -n "$cpw" ]
 then
  carma_password="$cpw"
 fi
 else
   echo
   echo
   echo "Skunkworks build will use the null authentication plugin by default."
   echo "Setting carma_user to 'skunkworks'."
   carma_user="skunkworks"
   carma_password="skunkworks"
 fi
fi

cat docker-compose.yml.tmpl | sed 's/%mysql_root_password%/'$mysql_root_password'/g' | sed 's/%mysql_replication_password%/'$mysql_replication_password'/g' | sed 's/%mysql_arpsi_password%/'$mysql_arpsi_password'/g' | sed 's/%mysql_copsu_password%/'$mysql_copsu_password'/g' | sed 's/%mysql_icm_password%/'$mysql_icm_password'/g' | sed 's/%mysql_informed_password%/'$mysql_informed_password'/g' | sed 's/%carma_user%/'$carma_user'/g' | sed 's/%carma_password%/'$carma_password'/g' > docker-compose.yml 

echo "mysql_root_password=$mysql_root_password" > config.prev
echo "mysql_replication_password=$mysql_replication_password" >> config.prev
echo "mysql_arpsi_password=$mysql_arpsi_password" >> config.prev
echo "mysql_copsu_password=$mysql_copsu_password" >> config.prev
echo "mysql_icm_password=$mysql_icm_password" >> config.prev
echo "mysql_informed_password=$mysql_informed_password" >> config.prev
echo "carma_user=$carma_user" >> config.prev
echo "carma_password=$carma_password" >> config.prev

#
# Configure the conf files in apache-sp
#

(cd apache-sp; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user /bin/bash ./bake-config $*)

#
# Configure the conf files in arpsinode
(cd arpsinode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user /bin/bash ./bake-config $*)

#
# Configure the conf files in the copsunode
(cd copsunode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user /bin/bash ./bake-config $*)

#
# Configure the icm conf files
(cd icmnode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password /bin/bash ./bake-config $*)

# 
# Configure the informed conf files
(cd informednode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password /bin/bash ./bake-config $*)

#
# Configure the car conf files
(cd carnode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password /bin/bash ./bake-config $*)

#
# Configure the caradmin conf files
(cd caradminnode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password /bin/bash ./bake-config $*)

#
# Configure the IDP node
#
(cd idp; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_uaser CARMA_PASSWORD=$carma_password /bin/bash ./bake-config $*)

#
# (Re)build the CAR components' source code using the buildnode
#
# We rely on docker-compose "up" (without the -d flag) terminating with the container

(cd buildnode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS thisdir=`pwd` docker-compose up)

#
# After the relevant configs are baked (or not, if we're just running to start up after a 
# shutdown), execute docker-compose
#

docker-compose up -d

# Wait for the containers to complete their start-ups...
sleep 5

# Extract a copy of the IDP's metadata and install it in the SP
docker cp docker_idpnode_1:/usr/local/tomcat/webapps/ROOT/idp-metadata.xml idp-metadata.xml
docker cp idp-metadata.xml docker_apache-sp_1:/etc/shibboleth/idp-metadata.xml
docker exec -i -t docker_apache-sp_1 rm /var/run/apache2/apache2.pid

# And exchange keys between the CAR module and the IDP (for skunkworks only)

if [ "$SKUNKWORKS" == "yes" ]
then
    docker cp docker_idpnode_1:/opt/shibboleth-idp/credentials/car_idp.crt shibcert
    docker cp docker_carnode_1:/var/www/carma/carmacert car_carma.crt
    docker cp shibcert docker_carnode_1:/var/www/carma/shibcert
    docker cp car_carma.crt docker_idpnode_1:/usr/local/tomcat/webapps/idp/credentials/car_carma.crt
fi

# And restart the Apache-SP node to pick up the update

docker restart docker_apache-sp_1

# And ensure that the Apache daemon started properly (with another restart attempt)

docker exec -i -t docker_apache-sp_1 /etc/init.d/apache2 start

#
# And restart the carnode and the idpnode to pick up the updated keys
#

docker restart docker_carnode_1
docker restart docker_idpnode_1



