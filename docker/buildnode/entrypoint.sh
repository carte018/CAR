#!/bin/bash -v

#
# Assume /var/build has all the components as subdirectories 
#

cd /var/build/copsu
mvn install
cd /var/build/arpsi
mvn install
cd /var/build/informed
mvn install
cd /var/build/icm
mvn install
cd /var/build/caradmin
cat src/main/resources/i18n/components_en.properties.tmpl | sed 's^%ilogo_url%^'${ILOGO_URL}'^g' > src/main/resources/i18n/components_en.properties
cat src/main/resources/i18n/components_es.properties.tmpl | sed 's^%ilogo_url%^'${ILOGO_URL}'^g' > src/main/resources/i18n/components_es.properties
cat src/main/resources/i18n/components_de.properties.tmpl | sed 's^%ilogo_url%^'${ILOGO_URL}'^g' > src/main/resources/i18n/components_de.properties

mvn install
cd /var/build/car
cat src/main/resources/i18n/components_en.properties.tmpl | sed 's^%inst_long%^'"${INST_LONG}"'^g' | sed 's^%inst_short%^'"${INST_SHORT}"'^g' | sed 's^%ilogo_url%^'${ILOGO_URL}'^g' > src/main/resources/i18n/components_en.properties
cat src/main/resources/i18n/components_es.properties.tmpl | sed 's^%inst_long%^'"${INST_LONG}"'^g' | sed 's^%inst_short%^'"${INST_SHORT}"'^g' | sed 's^%ilogo_url%^'${ILOGO_URL}'^g' > src/main/resources/i18n/components_es.properties
cat src/main/resources/i18n/components_de.properties.tmpl | sed 's^%inst_long%^'"${INST_LONG}"'^g' | sed 's^%inst_short%^'"${INST_SHORT}"'^g' | sed 's^%ilogo_url%^'${ILOGO_URL}'^g' > src/main/resources/i18n/components_de.properties


mvn install
cd /var/build/regloader
mvn install
cd /var/build/demorp
mvn install

#
# And cp the results into the presumed-mounted volumes
#

cp /var/build/arpsi/target/arpsi-0.0.1.war /var/lib/arpsi/webapps/arpsi.war
cp /var/build/copsu/target/copsu-0.0.1.war /var/lib/copsu/webapps/copsu.war
cp /var/build/car/target/car-0.0.1.war /var/lib/car/webapps/car.war
cp /var/build/caradmin/target/caradmin-0.0.1.war /var/lib/caradmin/webapps/caradmin.war
cp /var/build/icm/target/icm-0.0.1.war /var/lib/icm/webapps/icm.war
cp /var/build/informed/target/informed-0.0.1.war /var/lib/informed/webapps/informed.war
cp -rp /var/build/regloader/* /var/lib/regloader/webapps/
cp -rp /var/build/demorp/target/demorp-0.0.1.war /var/lib/car/webapps/demorp.war

#
# And if we are in skunkworks mode, dump in the LDAP data
#
if [ "$SKUNKWORKS" == "yes" ]
then
  cp -rp /tmp/ldap/* /var/lib/ldap/data/
  echo "/var/lib/ldap/data contents:"
  ls -l /var/lib/ldap/data/
  cp -rp /tmp/container/* /var/lib/ldap/container/
  echo "/var/lib/ldap/container contents:"
  ls -l /var/lib/ldap/container/
  cp -rp /etc/ldap/* /var/lib/ldap/config/
  echo "/var/lib/ldap/config contents:"
  ls -l /var/lib/ldap/config/
fi

#
# And terminate successfully so docker-compose will release the flow
#

exit 0
