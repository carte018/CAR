FROM ubuntu:18.04
MAINTAINER Rob Carter <rob@duke.edu>

ENV DEBIAN_FRONTEND noninteractive

# We want consistency in uidnumber and gidnumber between instances

RUN groupadd -r mysql && useradd -r -g mysql mysql

# Prepare to install, and clean up after ourselves
#ADD sources.list /etc/apt/sources.list
RUN apt-get update -qq && \
	apt-get upgrade --yes && \
	apt-get -y install software-properties-common && \
	apt-get -y clean autoclean && \
	apt-get autoremove --yes && \
	rm -rf /var/lib/{apt,dpkg,cache,log}/

RUN apt-get -y install dirmngr
RUN apt-get -y install gpg-agent

# install mariadb

RUN apt-key adv --recv-keys --keyserver hkp://keyserver.ubuntu.com:80 0xF1656F24C74CD1D8

#RUN add-apt-repository "deb [arch=amd64,arm64,ppc64el] http://mariadb.mirror.liquidtelecom.com/repo/10.4/ubuntu $(lsb_release -cs) main"
RUN add-apt-repository "deb [arch=amd64,arm64,ppc64el] http://sfo1.mirrors.digitalocean.com/mariadb/repo/10.4/ubuntu $(lsb_release -cs) main"

RUN apt-get -y install mariadb-server mariadb-client && \
	apt-get -y  clean autoclean && \
	apt-get autoremove --yes && \
	rm -rf /var/lib/{apt,dpkg,cache,log} && \
	rm -rf /var/lib/mysql && \
	mkdir /var/lib/mysql && \
	sed -ri 's/^(bind-address|skip-networking|log)/;\1/' /etc/mysql/my.cnf

# install netcat for port checking
RUN apt-get -y install netcat

# install additional galera configuration and force utf8 support

ADD utf8.cnf /etc/mysql/conf.d/utf8.cnf
ADD galera.cnf /etc/mysql/conf.d/galera.cnf
ADD schema.sql /tmp/schema.sql

# Change ownership of /var/lib/mysql mount point

RUN chown mysql:mysql /var/lib/mysql

# Open ports for mariadb and galera

EXPOSE 3306 4444 4567 4568

# Mountable volume for our database(s)

VOLUME ["/var/lib/mysql"]
VOLUME ["/interlock"]

COPY entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

CMD ["mysqld"]
