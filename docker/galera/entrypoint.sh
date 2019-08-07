#!/bin/bash

set -e
DATADIR=/var/lib/mysql
IP=$(hostname --ip-address | cut -d" " -f1)

if [ -n "$TIMEZONE" ]; then
	echo ${TIMEZONE} > /etc/timezone && \
	dpkg-reconfigure -f noninteractive tzdata
fi

# Make sure we have somewhere to park the mariadb local socket
#

mkdir -p /var/run/mysqld
chown mysql:mysql /var/run/mysqld

#
# Convert the environment into configuraton
#
if [ -n "$PORT" ]; then
	sed -i -e "s/^port.*=.*/port-${PORT}/" /etc/mysql/my.cnf
fi

if [ -n "$MAX_CONNECTIONS" ]; then
	sed -i -e "s/^.*max_connections.*=.*/max_connections=${MAX_CONNECTIONS}/" /etc/mysql/my.cnf
fi

if [ -n "$MAX_ALLOWED_PACKET" ]; then
	sed -i -e "s/^max_allowed_packet.*=.*/max_allowed_packet=${MAX_ALLOWED_PACKET}/" /etc/mysql/my.cnf
fi

if [ -n "$QUERY_CACHE_SIZE" ]; then
	sed -i -e "s/^query_cache_size.*=.*/query_cache_size=${QUERY_CACHE_SIZE}/" /etc/mysql/my.cnf
fi


if [ "${1:0:1}" = '-' ]; then
	set -- mysqld "$@"
fi


if [ ! -d "$DATADIR/mysql" ]  && ! grep Done /interlock/`hostname` >/dev/null 2>&1 && [ ${PRIMARY} == `hostname` ]; then
	echo 'Running mysql_install_db' | tee /tmp/install_db_log
	mysql_install_db --user=mysql --datadir="$DATADIR" --wsrep_cluster_name="car_cluster" --wsrep_cluster_address="$CLUSTER_ADDRESS" --wsrep_node_name="$NODE_NAME" --wsrep_sst_auth="replication:$REPLICATION_PASSWORD" --wsrep_sst_receive_address="$IP"
	echo 'Done.' | tee /tmp/install_db_log
        echo 'Done' >> /interlock/`hostname`

	initializer='/tmp/initial.sql'
	
	cat > "$initializer" <<-ENDIT
		SET @@SESSION.SQL_LOG_BIN=0;
		DELETE FROM mysql.user;
		CREATE USER 'builder'@'%' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}';
		GRANT ALL ON *.* TO 'builder'@'%' WITH GRANT OPTION;
		DROP DATABASE IF EXISTS test;
		CREATE USER 'replication'@'%' IDENTIFIED BY '${REPLICATION_PASSWORD}';
		GRANT RELOAD,LOCK TABLES,REPLICATION CLIENT ON *.* TO 'replication'@'%';
		FLUSH PRIVILEGES;
	ENDIT

	set -- "$@" --init-file="$initializer"

	# and if we are the fist node, add the new cluster flag
	if [ ${PRIMARY} == `hostname` ]
	then
		set -- "$@" --wsrep_new_cluster
	fi
elif [ ${PRIMARY} == `hostname` ]
then
  alone=1
  for o in ${OTHER_NODES}
  do
    if nc -zv $o 3306 > /dev/null 2>&1
    then
      alone=0
    fi
  done
  if [ $alone == 1 ]
  then
    set -- "$@" --wsrep_new_cluster
  fi
fi

chown -R mysql:mysql "$DATADIR"

set -- "$@" \
	--wsrep_cluster_name="car_cluster" \
	--wsrep_cluster_address="$CLUSTER_ADDRESS" \
	--wsrep_node_name="$NODE_NAME" \
	--wsrep_sst_auth="replication:$REPLICATION_PASSWORD" \
	--wsrep_sst_receive_address="$IP" \
	--wsrep_node_address="$IP"

cat > /tmp/second.sql <<-FINIS
        CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}';
        GRANT ALL ON *.* TO 'root'@'%' WITH GRANT OPTION;
	CREATE USER IF NOT EXISTS 'arpsiuser'@'%' IDENTIFIED BY '${MYSQL_ARPSI_PASSWORD}';
	CREATE USER IF NOT EXISTS 'copsuuser'@'%' IDENTIFIED BY '${MYSQL_COPSU_PASSWORD}';
	CREATE USER IF NOT EXISTS 'icmuser'@'%' IDENTIFIED BY '${MYSQL_ICM_PASSWORD}';
	CREATE USER IF NOT EXISTS 'informeduser'@'%' IDENTIFIED BY '${MYSQL_INFORMED_PASSWORD}';
	GRANT ALL ON arpsi.* TO 'arpsiuser'@'%';
	GRANT ALL ON copsu.* TO 'copsuuser'@'%';
	GRANT ALL ON icm.* TO 'icmuser'@'%';
	GRANT ALL ON informed.* TO 'informeduser'@'%';
        FLUSH PRIVILEGES;
FINIS


echo "#!/bin/bash" > /tmp/start
echo "rm -f $DATADIR/grastate.dat" >> /tmp/start
echo "$@ &" >> /tmp/start
echo "until nc -zv 127.0.0.1 3306 2>/dev/null; do sleep 1; done" >> /tmp/start
echo "if [ \"${PRIMARY}\" == \"`hostname`\" ]; then until mysql -u builder -p${MYSQL_ROOT_PASSWORD} -e \"select count(*) from mysql.wsrep_cluster_members\" 2>&1 | grep ${NODE_COUNT}; do echo waiting for wsrep >> /tmp/waiting; sleep 1; done; echo continuing > /tmp/continuing; mysql -u builder -p${MYSQL_ROOT_PASSWORD} < /tmp/second.sql > /tmp/output 2>&1; fi" >> /tmp/start
echo "if [ \"${PRIMARY}\" == \"`hostname`\" ] && ! grep Schema /interlock/`hostname`; then mysql -u builder -p${MYSQL_ROOT_PASSWORD} < /tmp/schema.sql >> /tmp/schema.out 2>&1; echo Schema >> /interlock/`hostname`; fi" >> /tmp/start
echo "wait" >> /tmp/start
chmod 755 /tmp/start

exec "/tmp/start"

