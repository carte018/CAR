FROM tier/shib-idp:latest

# The build args below can be used at build-time to tell the build process where to find your config files.  This is for a completely burned-in config.
ARG TOMCFG=config/tomcat
ARG TOMCERT=credentials/tomcat
ARG TOMWWWROOT=wwwroot
ARG SHBCFG=config/shib-idp/conf
ARG SHBCREDS=credentials/shib-idp
ARG SHBVIEWS=config/shib-idp/views
ARG SHBEDWAPP=config/shib-idp/edit-webapp
ARG SHBMSGS=config/shib-idp/messages
ARG SHBMD=config/shib-idp/metadata
ARG CARFLOWS=config/shib-idp/flows/intercept

# copy in the needed config files
ADD ${TOMCFG} /usr/local/tomcat/conf
ADD ${TOMCERT} /opt/certs
ADD ${TOMWWWROOT} /usr/local/tomcat/webapps/ROOT
ADD ${SHBCFG} /opt/shibboleth-idp/conf
ADD ${SHBCREDS} /opt/shibboleth-idp/credentials
ADD ${SHBVIEWS} /opt/shibboleth-idp/views
ADD ${SHBEDWAPP} /opt/shibboleth-idp/edit-webapp
ADD ${SHBMSGS} /opt/shibboleth-idp/messages
ADD ${SHBMD} /opt/shibboleth-idp/metadata
ADD ${CARFLOWS} /opt/shibboleth-idp/flows/

# Get openssl installed (for key management)
RUN yum -y install openssl

# Prepare for rebuild
RUN mkdir -p /opt/shibboleth-idp/webapp
RUN mkdir -p /usr/local/tomcat/webapps/idp/credentials
ADD initial.key /usr/local/tomcat/webapps/idp/credentials/car_idp.key
ADD initial.crt /usr/local/tomcat/webapps/idp/credentials/car_carma.crt
#
# Modulate the startup to check for keys and create new ones if needed.
#

ADD entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

