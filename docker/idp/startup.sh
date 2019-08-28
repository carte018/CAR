#!/bin/sh

#for passed-in env vars, remove spaces and replace any ; with : in usertoken env var since we will use ; as a delimiter
export USERTOKEN="${USERTOKEN//;/:}"
export USERTOKEN="${USERTOKEN// /}"
export ENV="${ENV//;/:}"
export ENV="${ENV// /}"

# Build a JAR for the CAR plugin
(cd /opt/shibboleth-idp/edit-webapp/WEB-INF/classes;
find . -name \*.java -print > file;
javac @file;
rm -f file;
jar cvf /opt/shibboleth-idp/dist/webapp/WEB-INF/lib/car-module-0.0.1.jar edu)
cp /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/* /opt/shiboleth-idp/dist/webapp/WEB-INF/lib/

#rebuild idp war file to incorporate any post-install additions
#/opt/shibboleth-idp/bin/custom_build.sh -q -Didp.target.dir=/opt/shibboleth-idp
/opt/shibboleth-idp/bin/build.sh -q -Didp.target.dir=/opt/shibboleth-idp


# generic console logging pipe for anyone
mkfifo -m 666 /tmp/logpipe
cat <> /tmp/logpipe 1>&2 &

mkfifo -m 666 /tmp/logcrond
(cat <> /tmp/logcrond  | awk -v ENV="$ENV" -v UT="$USERTOKEN" '{printf "crond;console;%s;%s;%s\n", ENV, UT, $0; fflush()}' 1>/tmp/logpipe) &

mkfifo -m 666 /tmp/logtomcat
(cat <> /tmp/logtomcat | awk -v ENV="$ENV" -v UT="$USERTOKEN" '{printf "tomcat;console;%s;%s;%s\n", ENV, UT, $0; fflush()}' 1>/tmp/logpipe) &

mkfifo -m 666 /tmp/logsuperd
(cat <> /tmp/logsuperd | awk -v ENV="$ENV" -v UT="$USERTOKEN" '{printf "supervisord;console;%s;%s;%s\n", ENV, UT, $0; fflush()}' 1>/tmp/logpipe) &

mkfifo -m 666 /tmp/logidp-process
(cat <> /tmp/logidp-process | awk -v ENV="$ENV" -v UT="$USERTOKEN" '{printf "shib-idp;idp-process.log;%s;%s;%s\n", ENV, UT, $0; fflush()}' 1>/tmp/logpipe) &

mkfifo -m 666 /tmp/logidp-warn
(cat <> /tmp/logidp-warn | awk -v ENV="$ENV" -v UT="$USERTOKEN" '{printf "shib-idp;idp-warn.log;%s;%s;%s\n", ENV, UT, $0; fflush()}' 1>/tmp/logpipe) &

mkfifo -m 666 /tmp/logidp-audit
(cat <> /tmp/logidp-audit | awk -v ENV="$ENV" -v UT="$USERTOKEN" '{printf "shib-idp;idp-audit.log;%s;%s;%s\n", ENV, UT, $0; fflush()}' 1>/tmp/logpipe) &

mkfifo -m 666 /tmp/logidp-consent-audit
(cat <> /tmp/logidp-consent-audit | awk -v ENV="$ENV" -v UT="$USERTOKEN" '{printf "shib-idp;idp-consent-audit.log;%s;%s;%s\n", ENV, UT, $0; fflush()}' 1>/tmp/logpipe) &


# fix IdP's logback.xml to log to use above pipe
IDP_LOG_CFG_FILE=/opt/shibboleth-idp/conf/logback.xml
if test \! -f ${IDP_LOG_CFG_FILE}.dist; then
   cp ${IDP_LOG_CFG_FILE} ${IDP_LOG_CFG_FILE}.dist
fi
sed "s#<File>\${idp.logfiles}/idp-process.log</File>#<File>/tmp/logidp-process</File>#" ${IDP_LOG_CFG_FILE}.dist > ${IDP_LOG_CFG_FILE}.tmp
sed "s#<File>\${idp.logfiles}/idp-warn.log</File>#<File>/tmp/logidp-warn</File>#" ${IDP_LOG_CFG_FILE}.tmp > ${IDP_LOG_CFG_FILE}.tmp2
sed "s#<File>\${idp.logfiles}/idp-audit.log</File>#<File>/tmp/logidp-audit</File>#" ${IDP_LOG_CFG_FILE}.tmp2 > ${IDP_LOG_CFG_FILE}.tmp3
sed "s#<File>\${idp.logfiles}/idp-consent-audit.log</File>#<File>/tmp/logidp-consent-audit</File>#" ${IDP_LOG_CFG_FILE}.tmp3 > ${IDP_LOG_CFG_FILE}
rm -f ${IDP_LOG_CFG_FILE}.tmp
rm -f ${IDP_LOG_CFG_FILE}.tmp2
rm -f ${IDP_LOG_CFG_FILE}.tmp
# Remove auto-rolling of logfile
sed -i -e 's/rolling.RollingFileAppender/FileAppender/g' ${IDP_LOG_CFG_FILE}
sed -i -e '/<rollingPolicy/,/<\/rollingPolicy>/d' ${IDP_LOG_CFG_FILE}


#launch supervisord
/usr/bin/supervisord -c /etc/supervisor/supervisord.conf
