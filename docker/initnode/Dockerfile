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
# We need curl
#
RUN apt-get -y install curl

#
# And the demo data
#
ADD demo_data /tmp/demo_data

ADD entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

