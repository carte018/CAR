#!/bin/bash

#
# Build timing is somewhat...  complicated.
#
# While the initnode is built and started last by docker-compose, it starts
# as soon as the node's container is started, which may (frequently) happen before
# the other components have successfully deployed their servlets.  
#
# Here, we simply wait 20 seconds to give the other components time to 
# deploy their servlets before starting to populate registration information.
#

sleep 20

#
# Force tomcat8 to deploy the ICM
#
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure 'https://apache-sp/consent/v1/icm/icm-info-release-policies' 

#
# Force tomcat8 to deply the ARPSI
#
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure 'https://apache-sp/consent/v1/icm/org-info-release-policies' 

#
# Force tomcat8 to deploy the COPSU
#
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure 'https://apache-sp/consent/v1/icm/user-info-release-policies' 

#
# Force tomcat8 to deploy the informed content app
# and load the demo "Amber" RH 
#
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_rh_metainfo 'https://apache-sp/consent/v1/informed/rhic/metainformation/entityId/urn:mace:multiverse:amber' 

#
# Load attributes available through the Amber RH
#
for num in amberTitle cn displayName eduPersonOrcid eduPersonOrgDN eduPersonPrimaryAffiliation eduPersonPrincipalName eduPersonScopedAffiliation eduPersonTargetedID eduPersonUniqueId isMemberOf mail sn
do
  curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8'  --insecure -X PUT -d @/tmp/demo_data/amber_${num}_iimetainfo 'https://apache-sp/consent/v1/informed/iiic/iimetainformation/entityId/urn:mace:multiverse:amber/attribute/'$num 
done

#
# Load up information about the Pattern in Amber RP
#
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8'  --insecure -X PUT -d @/tmp/demo_data/amber_pattern_rp_metainfo 'https://apache-sp/consent/v1/informed/rpic/metainformation/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.amber.org!shibboleth'

curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8'  --insecure -X PUT -d @/tmp/demo_data/amber_pattern_rp_optionaliilist 'https://apache-sp/consent/v1/informed/rpic/optionaliilist/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.amber.org!shibboleth'

curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8'  --insecure -X PUT -d @/tmp/demo_data/amber_pattern_rp_requirediilist 'https://apache-sp/consent/v1/informed/rpic/requirediilist/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.amber.org!shibboleth'

# 
# Replace the entrypoint routine with one that only forces deployments
# -- on restart, we must not overwrite registration information.
#

echo '#!/bin/bash' > /replaceme
echo "mv /entrypoint.sh /entrypoint.sh.original" >> /replaceme
echo "curl -u \"${CARMA_USER}:${CARMA_PASSWORD}\" --insecure 'https://apache-sp/consent/v1/icm/icm-info-release-policies' >> /dev/null" >> /replaceme
echo "curl -u \"${CARMA_USER}:${CARMA_PASSWORD}\" --insecure 'https://apache-sp/consent/v1/icm/org-info-release-policies' >> /dev/null" >> /replaceme
echo "curl -u \"${CARMA_USER}:${CARMA_PASSWORD}\" --insecure 'https://apache-sp/consent/v1/icm/user-info-release-policies' >> /dev/null" >> /replaceme
echo 'echo exit 1 >> /entrypoint.sh' >> /replaceme
echo 'chmod 777 /entrypoint.sh' >> /replaceme
chmod 755 /replaceme
exec /replaceme
