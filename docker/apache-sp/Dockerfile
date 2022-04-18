FROM ubuntu:18.04
MAINTAINER Rob Carter <rob@duke.edu>

ENV DEBIAN_FRONTEND noninteractive

# Make some users and groups for internal consistency

RUN groupadd -r _shibd && useradd -r -g _shibd _shibd
# RUN groupadd -r www-data && useradd -r -g www-data www-data

# Prepare for installation and clean up after ourselves
#ADD sources.list /etc/apt/sources.list
RUN apt-get update -qq && \ 
    apt-get upgrade --yes && \
    apt-get -y install software-properties-common && \
    apt-get -y clean autoclean && \
    apt-get autoremove --yes && \
    rm -rf /var/lib/{apt,dpkg,cache,log}/

# Begin by installing the default Apache distro from Ubuntu
# Note that we install the SSL and proxy_ajp modules, too

RUN apt-get -y install apache2 && \
    a2enmod ssl && \
    a2enmod proxy_ajp

# Configuration should have been adjusted by the script that called the composer that's 
# building us, so we can simply pull in the files of relevance now to configur Apache

ADD custom.conf /etc/apache2/sites-enabled/custom.conf
ADD ssl.conf /etc/apache2/sites-enabled/ssl.conf
ADD ssl.pem /etc/ssl/certs/ssl.pem
ADD ssl.key /etc/ssl/private/ssl.key

# Add our federation signing certificate (for handling signature validation of the 
# metadata we are about to consume

ADD sslsigner.crt /etc/ssl/certs/sslsigner.crt


# Timezone - default to UTC for logging, etc.  If you prefer to use a different zone, 
# replace in the cp below -- eg., "UTC -> America/New_York" and uncomment the "RUN cp" command

RUN DEBIAN_FRONTEND=noninteractive apt-get -y install tzdata
# RUN cp /usr/share/zoneinfo/UTC /etc/localtime

#
# And install the Shibboleth SP for those things that require federated SSO (eg., the self-
# service interface and the admin interface.
#

RUN apt-get -y install libapache2-mod-shib2

#
# Install the supervisor
#

RUN apt-get -y install supervisor

#
# Add a block to the supervisord config
#
ADD supervisord.conf /etc/supervisor/conf.d/99local.conf

#
# Expose ports
#
EXPOSE 443

#
# Add baked configurations
#

# Apache base configuration with localizations
ADD apache2.conf /etc/apache2/apache2.conf
#
# Apache mod_proxy_ajp configuration (to forward CAR calls to appropriate containers)
#
ADD proxy_ajp.conf /etc/apache2/sites-enabled/proxy_ajp.conf
#
# CARMA configuration for Shibboleth SP module in Apache
ADD carma-shib.conf /etc/apache2/conf-enabled/carma-shib.conf
#
# Attribute mapping for Shibboleth SP for relevant UIs
#
ADD attribute-map.xml /etc/shibboleth/attribute-map.xml
#
# Signing key for federation metadata.  If SP is federated bilaterally with a local IDP, 
# this may be local signing key or a completely bogus key (if local metadata is kept unsigned
# If the SP is federated multilaterally, this should be the signing key for the federation 
# metadata aggregate the SP will be configured to retrieve.
#
ADD fedsigner.pem /etc/shibboleth/fedsigner.pem
#
# Shibboleth SP configuration
ADD shibboleth2.xml /etc/shibboleth/shibboleth2.xml

# Demo configuration for Sliced Bread environment with SeamlessAccess
ADD shibboleth2.xml.sb-sa /etc/shibboleth/shibboleth2.xml.sb-sa

#
# Key and certificate for Shibboleth SP
ADD sp-key.pem /etc/shibboleth/sp-key.pem
ADD sp-cert.pem /etc/shibboleth/sp-cert.pem

#
# Icons for Amber Entities
#
ADD Amber-Icon.png /var/www/Amber-Icon.png
ADD AmberPattern.png /var/www/AmberPattern.png
ADD Logrus_of_Chaos.png /var/www/Logrus_of_Chaos.png
ADD Pattern_in_Rebma.png /var/www/Pattern_In_Rebma.png
ADD ContentRUs.png /var/www/ContentRUs.png
ADD PeanutsLogo.png /var/www/PeanutsLogo.png
ADD RandSRUs.png /var/www/RandSRUs.png
ADD ScholarlyGarage.png /var/www/ScholarlyGarage.png
ADD amber-logo.png /var/www/amber-logo.png
ADD amber-logo.png /var/www/amber_logo.png
ADD index.html /var/www/index.html

VOLUME ["/interlock"]
VOLUME ["/var/log/httpd"]

#
# Start the supervisor, which in turn starts the Aapche/SP stack
#

CMD ["/usr/bin/supervisord"]

