FROM osixia/openldap:1.2.5
MAINTAINER Rob Carter "rob@duke.edu"

ADD eduperson.schema /tmp/eduperson.schema
ADD amberite.schema /tmp/amberite.schema
ADD people.ldif /tmp/people.ldif
ADD idpuser.ldif /tmp/idpuser.ldif
ADD idpuser.access /tmp/idpuser.access
ADD users.ldif /tmp/users.ldif
 
ADD entrypoint.sh /entrypoint.sh
RUN chmod a+x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

CMD ["/container/tool/run"]