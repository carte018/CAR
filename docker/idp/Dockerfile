FROM amber/idp-base:latest

# The build args below can be used at build-time to tell the build process where to find your config files.  This is for a completely burned-in config.
ARG SHBCFG=config/shib-idp/conf
ARG SHBCREDS=credentials/shib-idp
ARG SHBVIEWS=config/shib-idp/views
ARG SHBEDWAPP=config/shib-idp/edit-webapp
ARG SHBMSGS=config/shib-idp/messages
ARG SHBMD=config/shib-idp/metadata
ARG CARFLOWS=config/shib-idp/flows

# copy in the needed config files for adding CAR integration
## ADD ${SHBCFG} /opt/shibboleth-idp/conf
ADD ${SHBCFG}/attribute-filter.xml /opt/shibboleth-idp/conf/attribute-filter.xml
ADD ${SHBCFG}/attribute-resolver.xml /opt/shibboleth-idp/conf/attribute-resolver.xml
ADD ${SHBCFG}/global.xml /opt/shibboleth-idp/conf/global.xml
ADD ${SHBCFG}/metadata-providers.xml /opt/shibboleth-idp/conf/metadata-providers.xml
ADD ${SHBCFG}/relying-party.xml /opt/shibboleth-idp/conf/relying-party.xml
ADD ${SHBCFG}/intercept/profile-intercept.xml /opt/shibboleth-idp/conf/intercept/profile-intercept.xml
ADD ${SHBCFG}/idp.properties /tmp/idp.properties
##ADD ${SHBCREDS} /opt/shibboleth-idp/credentials
##ADD ${SHBVIEWS} /opt/shibboleth-idp/views
ADD ${SHBVIEWS}/car-attribute-release.vm /opt/shibboleth-idp/views/car-attribute-release.vm
ADD ${SHBEDWAPP} /opt/shibboleth-idp/edit-webapp
##ADD ${SHBMSGS} /opt/shibboleth-idp/messages
##ADD ${SHBMD} /opt/shibboleth-idp/metadata
ADD ${SHBMD}/local-sites.xml.tmpl /opt/shibboleth-idp/metadata/local-sites.xml.tmpl
ADD ${SHBMD}/local-sites.xml /opt/shibboleth-idp/metadata/local-sites.xml
ADD ${CARFLOWS} /opt/shibboleth-idp/flows/

#
# Add the requisite change to idp.properties
#
RUN sed -i'.orig' 's/https:\/\/localhost\/idp\/shibboleth/urn:mace:multiverse:amber/g' /opt/shibboleth-idp/conf/idp.properties
RUN grep '^car\.' /tmp/idp.properties >> /opt/shibboleth-idp/conf/idp.properties
RUN grep '^idp.session.StorageService' /tmp/idp.properties >> /opt/shibboleth-idp/conf/idp.properties

# Get openssl installed (for key management)
RUN yum -y install openssl

# Prepare for rebuild
RUN mkdir -p /opt/shibboleth-idp/webapp
RUN mkdir -p /tmp/credentials
ADD initial.key /tmp/credentials/car_idp.key
ADD initial.crt /tmp/credentials/car_carma.crt
ADD startup.sh /usr/bin/startup.sh
ADD custom_build.sh /opt/shibboleth-idp/bin/custom_build.sh
#
# Modulate the startup to check for keys and create new ones if needed.
#

ADD entrypoint.sh /entrypoint.sh
WORKDIR /tmp
RUN ls
RUN jar uvf /opt/shibboleth-idp/war/idp.war credentials

ENTRYPOINT ["/entrypoint.sh"]

CMD ["/usr/bin/startup.sh"]
