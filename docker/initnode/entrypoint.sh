#!/bin/bash

#
# Build timing is somewhat...  complicated.
#
# While the initnode is built and started last by docker-compose, it starts
# as soon as the node's container is started, which may (frequently) happen before
# the other components have successfully deployed their servlets.  
#
# Here, we simply wait 60 seconds to give the other components time to 
# deploy their servlets before starting to populate registration information.
#

sleep 60


#
# Start by baking out the rp metainfo objects
#

echo "Baking amber_pattern_rp_metainfo"
cat /tmp/demo_data/amber_pattern_rp_metainfo.tmpl | sed 's/%apache_fqdn%/'${APACHE_FQDN}'/g' > /tmp/demo_data/amber_pattern_rp_metainfo
echo "Baking carma_rp_metainfo"
cat /tmp/demo_data/carma_rp_metainfo.tmpl | sed 's/%apache_fqdn%/'${APACHE_FQDN}'/g' > /tmp/demo_data/carma_rp_metainfo
echo "Baking logrus_metainfo"
cat /tmp/demo_data/logrus_metainfo.tmpl | sed 's/%apache_fqdn%/'${APACHE_FQDN}'/g' > /tmp/demo_data/logrus_metainfo
echo "Baking rebma_pattern_rp_metainfo"
cat /tmp/demo_data/rebma_pattern_rp_metainfo.tmpl | sed 's/%apache_fqdn%/'${APACHE_FQDN}'/g' > /tmp/demo_data/rebma_pattern_rp_metainfo
echo "Baking content-r-us metainfo"
cat /tmp/demo_data/amber_contentrus_rp_metainfo.tmpl | sed 's/%apache_fqdn%/'${APACHE_FQDN}'/g' > /tmp/demo_data/amber_contentrus_rp_metainfo
echo "Baking research-r-us metainfo"
cat /tmp/demo_data/amber_randsrus_rp_metainfo.tmpl | sed 's/%apache_fqdn%/'${APACHE_FQDN}'/g' > /tmp/demo_data/amber_randsrus_rp_metainfo
echo "Baking scholarly garage metainfo"
cat /tmp/demo_data/amber_scholars_rp_metainfo.tmpl | sed 's/%apache_fqdn%/'${APACHE_FQDN}'/g' > /tmp/demo_data/amber_scholars_rp_metainfo
echo "Baking peanuts metainfo"
cat /tmp/demo_data/amber_payroll_rp_metainfo.tmpl | sed 's/%apache_fqdn%/'${APACHE_FQDN}'/g' > /tmp/demo_data/amber_payroll_rp_metainfo

#
# Force tomcat8 to deploy the ARPSI
#
echo "Triggering deployment of arpsi"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure 'https://apache-sp/consent/v1/icm/org-info-release-policies' 

#
# Force tomcat8 to deploy the COPSU
#
echo "Triggering deployment of copsu"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure 'https://apache-sp/consent/v1/icm/user-info-release-policies' 

#
# Force tomcat8 to deploy the ICM 
#
echo "Triggering deployment of ICM"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" --insecure 'https://apache-sp/consent/v1/icm/icm-info-release-policies' 


#
# Force tomcat8 to deploy the informed content app
# and load the demo "Amber" RH 
#
echo "Loading Amber RH information"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_rh_metainfo 'https://apache-sp/consent/v1/informed/rhic/metainformation/entityId/urn:mace:multiverse:amber' 

#
# And add the Chaos RH
#

echo "Loading Chaos RH information"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/chaos_rh_metainfo 'https://apache-sp/consent/v1/informed/rhic/metainformation/entityId/urn:mace:multiverse:chaos'

#
# Load attributes available through the Amber RH
#
for num in amberTitle cn displayName eduPersonOrcid eduPersonOrgDN eduPersonPrimaryAffiliation eduPersonPrincipalName eduPersonScopedAffiliation eduPersonTargetedID eduPersonUniqueId isMemberOf mail sn eduPersonOrgUnitDN departmentCode ferpaFlag
do
  echo "Inserting attribute $num"
  curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8'  --insecure -X PUT -d @/tmp/demo_data/amber_${num}_iimetainfo 'https://apache-sp/consent/v1/informed/iiic/iimetainformation/entityId/urn:mace:multiverse:amber/attribute/'$num 
  sleep 2
done

echo "Adding rhiilist"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_rh_iilist 'https://apache-sp/consent/v1/informed/rhic/iilist/entityId/urn:mace:multiverse:amber'
sleep 2
echo "And rh infotypelist"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_rh_infotypes 'https://apache-sp/consent/v1/informed/rhic/infotypes/entityId/urn:mace:multiverse:amber'
sleep 2
echo "And the rplist"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_rh_rplist 'https://apache-sp/consent/v1/informed/rhic/rplist/entityId/urn:mace:multiverse:amber'
sleep 2
# Ditto for Chaos

for num in chaosFamily cn displayName eduPersonOrcid eduPersonOrgDN eduPersonPrimaryAffiliation eduPersonPrincipalName eduPersonScopedAffiliation eduPersonTargetedID eduPersonUniqueId isMemberOf mail sn
do
  echo "Inserting attribute $num"
  curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/chaos_${num}_iimetainfo 'https://apache-sp/consent/v1/informed/iiic/iimetainformation/entityId/urn:mace:multiverse:chaos/attribute/'$num
done

echo "Adding rhiilist"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/chaos_rh_iilist 'https://apache-sp/consent/v1/informed/rhic/iilist/entityid/urn:mace:multiverse:chaos'

echo "Adding rh infotypelist"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/chaos_rh_infotypes 'https://apache-sp/consent/v1/informed/rhic/infotypes/entityId/urn:mace:multiverse:chaos'

echo "Adding rh rplist"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/chaos_rh_rplist 'https://apache-sp/consent/v1/informed/rhic/rplist/entityId/urn:mace:multiverse:chaos'

#
# Load up information about the Pattern in Amber RP
#
echo "Inserting Pattern in Amber RP metainfo"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8'  --insecure -X PUT -d @/tmp/demo_data/amber_pattern_rp_metainfo 'https://apache-sp/consent/v1/informed/rpic/metainformation/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.amber.org!shibboleth'
sleep 2
echo "Inserting Pattern in Amber RP optional attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8'  --insecure -X PUT -d @/tmp/demo_data/amber_pattern_rp_optionaliilist 'https://apache-sp/consent/v1/informed/rpic/optionaliilist/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.amber.org!shibboleth'
sleep 2
echo "Inserting Pattern in Amber RP required attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8'  --insecure -X PUT -d @/tmp/demo_data/amber_pattern_rp_requirediilist 'https://apache-sp/consent/v1/informed/rpic/requirediilist/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.amber.org!shibboleth'
sleep2
#
# And information about the Pattern in Rebma RP
#
echo "Inserting Pattern in Rebma RP metainfo"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/rebma_pattern_rp_metainfo 'https://apache-sp/consent/v1/informed/rpic/metainformation/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.rebma.org!shibboleth'
sleep 2
echo "Inserting Pattern in Rebma RP optional attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/rebma_pattern_rp_optionaliilist 'https://apache-sp/consent/v1/informed/rpic/optionaliilist/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.rebma.org!shibboleth'
sleep 2
echo "Inserting Pattern in Rebma RP required attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/rebma_pattern_rp_requirediilist 'https://apache-sp/consent/v1/informed/rpic/requirediilist/entityId/urn:mace:multiverse:amber/entityId/https:!!pattern.rebma.org!shibboleth'
sleep 2
#
# And the Logrus (in the Courts of Chaos)
#
echo "Inserting Logrus metainfo"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/logrus_metainfo 'https://apache-sp/consent/v1/informed/rpic/metainformation/entityId/urn:mace:multiverse:chaos/entityId/https:!!logrus.coc.org!shibboleth'
sleep 2
echo "Inserting Logrus optional attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/logrus_optionaliilist 'https://apache-sp/consent/v1/informed/rpic/optionaliilist/entityId/urn:mace:multiverse:chaos/entityId/https:!!logrus.coc.org!shibboleth'
sleep 2
echo "Inserting Logrus required attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/logrus_requirediilist 'https://apache-sp/consent/v1/informed/rpic/requirediilist/entityId/urn:mace:multiverse:chaos/entityId/https:!!logrus.coc.org!shibboleth'
 sleep 2
#
# And the CARMA instance itself needs a definition
#
echo "Inserting CARMA metainfo"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/carma_rp_metainfo 'https://apache-sp/consent/v1/informed/rpic/metainformation/entityId/urn:mace:multiverse:amber/entityId/https:!!localhost!car-demo-sp'
sleep 2
echo "Inserting CARMA optional attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/carma_rp_optionaliilist 'https://apache-sp/consent/v1/informed/rpic/optionaliilist/entityId/urn:mace:multiverse:amber/entityId/https:!!localhost!car-demo-sp'
sleep 2
echo "Inserting CARMA required attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/carma_rp_requirediilist 'https://apache-sp/consent/v1/informed/rpic/requirediilist/entityId/urn:mace:multiverse:amber/entityId/https:!!localhost!car-demo-sp'
sleep 2
#
# And the contentrus RP
#
echo "Inserting ContentRUs metainfo"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-Type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_contentrus_rp_metainfo 'https://apache-sp/consent/v1/informed/rpic/metainformation/entityId/urn:mace:multiverse:amber/entityId/https:!!content-r-us.com!shibboleth'
sleep 2
echo "Inserting ContentRUs optional attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_contentrus_rp_optionaliilist 'https://apache-sp/consent/v1/informed/rpic/optionaliilist/entityId/urn:mace:multiverse:amber/entityId/https:!!content-r-us.com!shibboleth'
sleep 2
echo "Inserting ContentRUs required attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json; charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_contentrus_rp_requirediilist 'https://apache-sp/consent/v1/informed/rpic/requirediilist/entityId/urn:mace:multiverse:amber/entityId/https:!!content-r-us.com!shibboleth'
sleep 2
#
# And the researchrus RP
#
echo "Inserting ResearchRUs metainfo"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-Type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_randsrus_rp_metainfo 'https://apache-sp/consent/v1/informed/rpic/metainformation/entityId/urn:mace:multiverse:amber/entityId/https:!!research-r-us.com!shibboleth'
sleep 2
echo "Inserting ResearchRUs optional attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_randsrus_rp_optionaliilist 'https://apache-sp/consent/v1/informed/rpic/optionaliilist/entityId/urn:mace:multiverse:amber/entityId/https:!!research-r-us.com!shibboleth'
sleep 2
echo "Inserting ResearchRUs required attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json; charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_randsrus_rp_requirediilist 'https://apache-sp/consent/v1/informed/rpic/requirediilist/entityId/urn:mace:multiverse:amber/entityId/https:!!research-r-us.com!shibboleth'
sleep 2
#
# And the scholars RP
#
echo "Inserting scholarly garage metainfo"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_scholars_rp_metainfo 'https://apache-sp/consent/v1/informed/rpic/metainformation/entityId/urn:mace:multiverse:amber/entityId/https:!!scholars.amber.org!shibboleth'
sleep 2
echo "Inserting scholarly garage optional attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_scholars_rp_optionaliilist 'https://apache-sp/consent/v1/informed/rpic/optionaliilist/entityId/urn:mace:multiverse:amber/entityId/https:!!scholars.amber.org!shibboleth'
sleep 2
echo "Inserting scholarly garage required attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_scholars_rp_requirediilist 'https://apache-sp/consent/v1/informed/rpic/requirediilist/entityId/urn:mace:multiverse:amber/entityId/https:!!scholars.amber.org!shibboleth'
sleep 2
#
# And the peanuts RP
#
echo "Inserting peanuts metainfo"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-Type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_payroll_rp_metainfo 'https://apache-sp/consent/v1/informed/rpic/metainformation/entityId/urn:mace:multiverse:amber/entityId/https:!!payroll.amber.org!shibboleth'
sleep 2
echo "Inserting peanuts optional attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-Type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_payroll_rp_optionaliilist 'https://apache-sp/consent/v1/informed/rpic/optionaliilist/entityId/urn:mace:multiverse:amber/entityId/https:!!payroll.amber.org!shibboleth'
sleep 2 
echo "Inserting peanuts required attributes"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-Type: application/json;charset=UTF-8' --insecure -X PUT -d @/tmp/demo_data/amber_payroll_rp_requirediilist 'https://apache-sp/consent/v1/informed/rpic/requirediilist/entityId/urn:mace:multiverse:amber/entityId/https:!!payroll.amber.org!shibboleth'
sleep 2
#
# And apply the base policies for CARMA, which mandate release of everything to ourselves
#

echo "Inserting CARMA ARPSI policy"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/carma_arpsi 'https://apache-sp/consent/v1/icm/org-info-release-policies'
sleep 2
echo "Inserting CARMA META policy"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/carma_icm 'https://apache-sp/consent/v1/icm/icm-info-release-policies'
sleep 2

echo "Inserting Faculty R&S Org policy"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type:application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/orgfaculty.json 'https://apache-sp/consent/v1/icm/org-info-release-policies'
sleep 2
echo "Inserting Faculty R&S ICM policy"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type:application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/icmfac.json 'https://apache-sp/consent/v1/icm/icm-info-release-policies'
sleep 2

echo "Inserting FERPA R&S ICM policy (no matching org policy)"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type:application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/ferparandsicm.json 'https://apache-sp/consent/v1/icm/icm-info-release-policies'
sleep 2

echo "Inserting Grad R&S Org policy"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/orggrad.json 'https://apache-sp/consent/v1/icm/org-info-release-policies'
sleep 2
echo "Inserting Grad R&S ICM policy"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/icmgrad.json 'https://apache-sp/consent/v1/icm/icm-info-release-policies'

echo "Inserting OrgStatefulPrivacy policy"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/orgstateful.json 'https://apache-sp/consent/v1/icm/org-info-release-policies'
sleep 2
echo "Inserting IcmStateful policy"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/icmstateful.json 'https://apache-sp/consent/v1/icm/icm-info-release-policies'

echo "Inserting Student R&S Org policy"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/orgstudent.json 'https://apache-sp/consent/v1/icm/org-info-release-policies'
sleep 2
echo "Inserting Generic R&S ICM policy"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/icmgen.json 'https://apache-sp/consent/v1/icm/icm-info-release-policies'

#
# Make kjk-faculty an rh auditor for demo purposes
#
echo "Adding rhaudit capability for kjk-faculty"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/kjk-faculty-rhaudit.json 'https://apache-sp/consent/v1/informed/adminrole/'

#
# Load value metainformation for the departmentCode values
#
echo "Adding value metainfo for departmentCode raw values"
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/amber_departmnetCode_12130100_valuemetainfo 'https://apache-sp/consent/v1/informed/iiic/valuemetainformation/departmentCode/12130100'
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/amber_departmentCode_12180000_valuemetainfo 'https://apache-sp/consent/v1/informed/iiic/valuemetainformation/departmentCode/12180000'
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/amber_departmentCode_12171555_valuemetainfo 'https://apache-sp/consent/v1/informed/iiic/valuemetainformation/departmentCode/12171555'
curl -u "${CARMA_USER}:${CARMA_PASSWORD}" -H 'Content-type: application/json;charset=UTF-8' --insecure -X POST -d @/tmp/demo_data/amber_departmentCode_10010011_valuemetainfo 'https://apache-sp/consent/v1/informed/iiic/valuemetainformation/departmentCode/10010011'

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
