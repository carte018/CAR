#!/bin/bash

#
# Force tomcat8 to deploy the ICM
#
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure 'https://apache-sp/consent/v1/icm/icm-info-release-policies' >> /dev/null

#
# Force tomcat8 to deply the ARPSI
#
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure 'https://apache-sp/consent/v1/icm/org-info-release-policies' >> /dev/null

#
# Force tomcat8 to deploy the COPSU
#
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure 'https://apache-sp/consent/v1/icm/user-info-release-policies' >> /dev/null

#
# Force tomcat8 to deploy the informed content app
# and load the demo "Amber" RH 
#
curl -u "${CARMA_USER}:$CARMA_PASSWORD" --insecure -X PUT -d @/tmp/demo_data/amber_rh_metainfo 'https://apache-sp/consent/v1/informed/rhic/entityId/urn:mace:multiverse:amber' >> /dev/null

#
# Load attributes available through the Amber RH
#
for num in amberTitle cn displayName eduPersonOrcid eduPersonOrgDN eduPersonPrimaryAffiliation eduPersonPrincipalName eduPersonScopedAffiliation eduPersonTargetedID eduPersonUniqueId isMemberOf mail sn
do
  curl -u "${CARMA_USER}:$CARMA_PASSWORD" --insecure -X PUT -d @/tmp/demo_data/amber_$num_iimetainfo 'https://apache-sp/consent/v1/informed/iiic/entityId/urn:mace:multiverse:amber/attribute/'$num >> /dev/null
done

#
# Load up information about the Pattern in Amber RP
#
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure -X PUT -d @/tmp/demo_data/amber_pattern_rp_metainfo 'https://apache-sp/consent/v1/informed/rpic/metainformation/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.amber.org!shibboleth'

curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure -X PUT -d @/tmp/demo_data/amber_pattern_rp_optionaliilist 'https://apache-sp/consent/v1/informed/rpic/optionaliilist/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.amber.org!shibboleth'

curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure -X PUT -d @/tmp/demo_data/amber_pattern_rp_requirediilist 'https://apache-sp/consent/v1/informed/rpic/requirediilist/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.amber.org!shibboleth'

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
