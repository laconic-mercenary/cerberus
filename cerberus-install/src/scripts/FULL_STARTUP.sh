echo "CERBERUS - STARTUP"

H2_JAR="h2-1.3.176.jar" # this version must sync up with what's in the init script
H2_INIT=/etc/init.d/h2 # this file is expected to be there already, if not, it can be found in the cerberus-install project
DB_NAME=CERBERUS_MAIN

if [ ! -f $H2_INIT ];
then
	echo "[ERROR] Expected h2 init script to be at $H2_INIT."
	echo "[INFO] The h2 init script can be found in the cerberus-install maven project, in src/scripts"
	echo "[INFO] After placing the script in the init.d dir, run the following commands: "
	echo "   cd /etc/init.d"
	echo "   sudo chmod 775 h2"
	echo "   sudo chkconfig --add h2"
	echo "[INFO] You may need to install chkconfig to do so."
	# sudo chmod 755 h2
	# sudo chkconfig --add h2
	exit 1
fi

JAVA_HM="/opt/jdk"
while [ ! -d $JAVA_HM/jre ];
do
	read -p "Enter the jdk directory (jre will be a subdirectory in this directory): " JAVA_HM
done

APPSRV="/opt/apps/glassfish/bin"
while [ ! -f $APPSRV/asadmin ];
do
	read -p "Enter the 'asadmin' directory (Glassfish): " APPSRV
done

JAVADB="/opt/db/h2/bin"
while [ ! -f $JAVADB/$H2_JAR ];
do
	echo "[WARNING] : Expected file to exist: $JAVADB/$H2_JAR. This path must sync up with what is in $H2_INIT."
	echo "[INFO] This is also where the database will be created at (with .h2 extension)"
	read -p "Enter the directory containing the $H2_JAR jar file directory (H2 DB): " JAVADB
done

# WARNING: these environment variables will go away
# after the script is done executing
export PATH=$PATH:$JAVADB:$APPSRV:$JAVA_HM/bin
export JAVA_HOME=$JAVA_HM

umask 775
chmod -R 775 .

#0) make sure the services are stopped
echo "Checking if Glassfish is stopped..."
asadmin stop-domain
echo "Done"

# 1) start the database
echo "1) Starting H2 Database Service..."
# java -cp $JAVADB/$H2_JAR org.h2.tools.Server -tcp -tcpPort 9092 -tcpAllowOthers
service h2 init
service h2 start
echo " "
echo "INFO: The path to this database must match what is configured in Glassfish's connection pool properties."

ISOK="n"
while [ $ISOK != "y" ];
do
	read -p "Was the service successfully started (see if h2.pid file exists): (y/n) " ISOK
done

# 2) create the database and the schema 
# this will create a directory with the database name locally
echo "2) Creating Database and Schema"
java -cp $JAVADB/$H2_JAR org.h2.tools.RunScript -user cerberus_user -password IDKFAIDDQD -url jdbc:h2:tcp://localhost:9092//$JAVADB/$DB_NAME -script ./schema.sql
echo "Completed, database can be found at: $JAVADB/$DB_NAME.h2"

# 3) load the database with data
echo "3) Running Loaders"
java -cp $JAVADB/$H2_JAR org.h2.tools.RunScript -user cerberus_user -password IDKFAIDDQD -url jdbc:h2:tcp://localhost:9092//$JAVADB/$DB_NAME -script ./loaders.sql
echo "Completed"

ISOK2="n"
while [ $ISOK2 != "y" ];
do
	read -p "Everything look good so far? (y/n) " ISOK2
done

# 4) start the glassfish server
echo "4) Starting Glassfish Server"
asadmin restart-domain

# 5) deploy cerberus
echo "5) Deploying Cerberus"
asadmin deploy --name cerberus cerberus*.ear

echo "[6] Deploying Landing Zone"
echo "WARN: If the landing-zone war is not present, you must deploy it manually"
asadmin deploy --name landing-zone --contextroot landing-zone landing-zone*unix*.war
echo "Completed"
