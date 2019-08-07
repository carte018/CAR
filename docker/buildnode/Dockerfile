#
# From the demo sshd docker from docker.io
#

FROM ubuntu:18.04

MAINTAINER Rob Carter "rob@duke.edu"

#
# Misc Prep
#
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

# And install Maven

RUN apt-get install -y maven

#
# Volumes
#
VOLUME ["/var/lib/arpsi/webapps"]
VOLUME ["/var/lib/copsu/webapps"]
VOLUME ["/var/lib/car/webapps"]
VOLUME ["/var/lib/caradmin/webapps"]
VOLUME ["/var/lib/icm/webapps"]
VOLUME ["/var/lib/informed/webapps"]
VOLUME ["/var/lib/regloader/webapps"]

VOLUME ["/var/build/arpsi"]
VOLUME ["/var/build/copsu"]
VOLUME ["/var/build/car"]
VOLUME ["/var/build/caradmin"]
VOLUME ["/var/build/icm"]
VOLUME ["/var/build/informed"]
VOLUME ["/var/build/regloader"]

ADD entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

