#!/bin/bash

# Script to build CAR components with minimal configuration driven by externally mounted
# volumes (files or Docker volumes created and managed elsewhere).
#
# Similar to the master build-and-start-car script for building and starting a single-server
# standalone instance of CAR, but targeting deployers who just want to build the containers and
# deploy them manually (or via other means) in a wider environment.
#
# We build the containers to accept command line arguments for overriding settings.
#
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
	-d)
		DATABASE=yes  # default to no
		shift
		;;
	*)
		shift
		;;
    esac
  done
  set -- "${POSITIONAL[@]}"
}

QUIET=no
DATABASE=no

parse_args $*

#
# Container-only builds use docker-compose in build mode only.
#

cp docker-compose-build-only.yml.tmpl docker-compose.yml.tmpl

#
# Load any pre-existing config from before
#
if [ -e ./config.prev ]
then
  source ./config.prev
fi

# 


if [ "$QUIET" != "yes" ]
then
 if [ "$DATABASE" == "yes" ]
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
 else
 echo 
 echo "Components of the CAR system rely on a (usually SQL) database to store policy and "
 echo "other important information persistently.  You have chosen not to configure a database"
 echo "at this time (to do so, pass the "-d" flag to this script).  Once you determine what"
 echo "database you wish to use with your CAR instance and have its schema established to match"
 echo "the schema from this package, you will need to configure your CAR components to "
 echo "connect and authenticate to your chosen database server(s) by modifying both the component"
 echo "configuration files and the hibernate.cfg.xml files for each container."
 fi
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
fi

# Fix up the template for the docker-compose file
cat docker-compose.yml.tmpl | sed 's/%mysql_root_password%/'$mysql_root_password'/g' | sed 's/%mysql_replication_password%/'$mysql_replication_password'/g' | sed 's/%mysql_arpsi_password%/'$mysql_arpsi_password'/g' | sed 's/%mysql_copsu_password%/'$mysql_copsu_password'/g' | sed 's/%mysql_icm_password%/'$mysql_icm_password'/g' | sed 's/%mysql_informed_password%/'$mysql_informed_password'/g' | sed 's/%carma_user%/'$carma_user'/g' | sed 's/%carma_password%/'$carma_password'/g' | sed 's/%apache_fqdn%/'$apache_fqdn'/g' > docker-compose.yml 


# Record what we have so far

echo "mysql_root_password=$mysql_root_password" > config.prev
echo "mysql_replication_password=$mysql_replication_password" >> config.prev
echo "mysql_arpsi_password=$mysql_arpsi_password" >> config.prev
echo "mysql_copsu_password=$mysql_copsu_password" >> config.prev
echo "mysql_icm_password=$mysql_icm_password" >> config.prev
echo "mysql_informed_password=$mysql_informed_password" >> config.prev
echo "carma_user=$carma_user" >> config.prev
echo "carma_password=$carma_password" >> config.prev

# Bake configurations
#
# Configure the conf files in arpsinode
cp arpsinode/Dockerfile.co arpsinode/Dockerfile
(cd arpsinode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user /bin/bash ./bake-config.co $*)

#
# Configure the conf files in the copsunode
cp copsunode/Dockerfile.co copsunode/Dockerfile
(cd copsunode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user /bin/bash ./bake-config.co $*)

#
# Configure the icm conf files
cp icmnode/Dockerfile.co icmnode/Dockerfile
(cd icmnode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password /bin/bash ./bake-config.co $*)

# 
# Configure the informed conf files
cp informednode/Dockerfile.co informednode/Dockerfile
(cd informednode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password /bin/bash ./bake-config.co $*)

#
# Configure the car conf files
cp carnode/Dockerfile.co carnode/Dockerfile
(cd carnode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password /bin/bash ./bake-config.co $*)

#
# Configure the caradmin conf files
cp caradminnode/Dockerfile.co caradminnode/Dockerfile
(cd caradminnode; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS CARMA_USER=$carma_user CARMA_PASSWORD=$carma_password /bin/bash ./bake-config.co $*)


#
# (Re)build the CAR components' source code using the buildnode
#
# We rely on docker-compose "up" (without the -d flag) terminating with the container

cp buildnode/Dockerfile.co buildnode/Dockerfile
(cd buildnode;APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS thisdir=`pwd` ./bake-config.co; APACHE_FQDN=$apache_fqdn SKUNKWORKS=$SKUNKWORKS thisdir=`pwd` docker-compose up)

#
# After the relevant configs are baked, execute docker-compose to build containers (build only)
#

docker-compose build -d

# And we're done.
#
