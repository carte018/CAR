#
# From the demo sshd docker from docker.io
#

FROM ubuntu:18.04

MAINTAINER Rob Carter "rob@duke.edu"

#
# Start by preloading the user for tomcat to run under
#
RUN groupadd -r tomcat8 && useradd -r -g tomcat8 tomcat8


#
# Misc Prep
#
ADD sources.list /etc/apt/sources.list
RUN apt-get -y update
RUN apt-get -y install apt-utils
RUN apt-get -y update
RUN apt-get -y upgrade
#
# Load Azul Systems key to prep for instlaling Zulu-11
#
RUN apt-get install -y gnupg2
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0xB1998361219BD9C9
RUN apt-get install -y software-properties-common
RUN apt-add-repository 'deb http://repos.azulsystems.com/ubuntu stable main'
RUN apt-get update
#
# And install Zulu OpenJDK-11
RUN apt-get install -y zulu-11
#

#
# Tomcat 8
#
RUN apt-get -y install tomcat8
ADD server.xml /etc/tomcat8/server.xml

#
# Timezone fix.  We default to UTC -- to change to another timezone, uncomment the
# cp command below and rebuild with whatever timezone you want to specify
#
RUN DEBIAN_FRONTEND=noninteractive apt-get -y install tzdata
#RUN cp /usr/share/zoneinfo/America/New_York /etc/localtime


#
# Add a block to the supervisord config
#
# This one starts tomcat8 (rather than tomcat7)
ADD supervisord.conf /etc/supervisor/conf.d/99local.conf

#
# Expose ports
#
EXPOSE 8009

#
# Volumes
#
# Note that tomcat is now tomcat8 not tomcat7
VOLUME ["/var/log/tomcat8"]
VOLUME ["/var/lib/tomcat8/webapps"]
VOLUME ["var/log/httpd"]
VOLUME ["/var/www"]
VOLUME ["/var/log/supervisor"]
VOLUME ["/var/www/carma"]

#ADD rsyslog.conf /etc/rsyslog.d/99-local.conf

RUN apt-get -y install supervisor
RUN ln -s /etc/supervisor/supervisord.conf /etc/supervisord.conf

VOLUME ["/etc/car"]

# RUN ln -s /var/lib/tomcat7/* /usr/share/tomcat7/

# ADD tomcat7.init /etc/init.d/tomcat7
# rather, tomcat8 as /etc/default/tomcat8
ADD tomcat8 /etc/default/tomcat8

ADD entrypoint.sh /entrypoint.sh
ADD caradmin.conf /tmp/caradmin.conf

ADD ssl.pem /tmp/ssl.pem

ENTRYPOINT ["/entrypoint.sh"]


CMD ["/usr/bin/supervisord"]
