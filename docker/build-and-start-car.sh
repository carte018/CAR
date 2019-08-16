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
    esac
  done
  set -- "${POSITIONAL[@]}"
}

SKUNKWORKS=no
RESTART=no
REBUILD=no
QUIET=no

parse_args $*

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
else
  cp docker-compose.caronly docker-compose.yml.tmpl
fi

#
# Create an SSL certificate to use in the apache configuration
# 
# Required for both caronly and skunkworks builds
#
openssl req -newkey rsa:2048 -x509 -nodes -keyout ssl.key -new -out ssl.pem -subj '/C=UF/ST=Sector 1/L=Earth/O=United Federation of Planets/OU=StarFleet Academy/CN=localhost' -config ssl.cnf -sha256 -days 3650
cp ssl.key apache-sp/ssl.key
cp ssl.pem apache-sp/ssl.pem

#
# If we aren't called with the "-q" flag, update the docker-compose script with credential information
#
# Required for both caronly and skunkworks builds.
# Varies according to build type
#

if [ -e ./config.prev ]
then
  source ./config.prev
fi

if [ "$QUIET" != "yes" ]
then
 echo "The components of the CAR system rely on database tables to store various information."
 echo "Access to those database tables requires the use of schema users.  Before we begin, we need"
 echo "to set passwords for those schema users (and for the defautl MariaDB users the database will"
 echo "use to operate)."
 echo
 echo "Enter the password you wish to use for the 'root' user in your CAR database: [$mysql_root_password]: "
 read mrp
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

(cd apache-sp; SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user ./bake-config $*)

#
# Configure the conf files in arpsinode
(cd arpsinode; SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user ./bake-config $*)

#
# Configure the conf files in the copsunode
(cd copsunode; SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user ./bake-config $*)

#
# Configure the icm conf files
(cd icmnode; SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password ./bake-config $*)

# 
# Configure the informed conf files
(cd informednode; SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password ./bake-config $*)

#
# Configure the car conf files
(cd carnode; SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password ./bake-config $*)

#
# Configure the caradmin conf files
(cd caradminnode; SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password ./bake-config $*)

#
# (Re)build the CAR components' source code using the buildnode
#
# We rely on docker-compose "up" (without the -d flag) terminating with the container

(cd buildnode; SKUNKWORKS=$SKUNKWORKS thisdir=`pwd` docker-compose up)

#
# After the relevant configs are baked (or not, if we're just running to start up after a 
# shutdown), execute docker-compose
#

exec docker-compose up -d
