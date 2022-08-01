echo "CERBERUS - SHUTDOWN"

APPSRV="/opt/app/glassfish/bin"
while [ ! -f $APPSRV/asadmin ];
do
	read -p "Enter the 'asadmin' directory (Glassfish): " APPSRV
done

export PATH=$PATH:$APPSRV

echo "Undeploying Applications"
asadmin undeploy cerberus
asadmin undeploy landing-zone
echo "Complete"
echo " "
echo "Stopping Server..."
asadmin restart-domain
asadmin stop-domain
echo "Complete"

# clean up database
echo "Stopping Database Service..."

# to see how this works, check out the doc folder in the cerberus-install project
# HINT: /etc/init.d/h2
# it's important that these are called when it comes time to shutdown the system
# otherwise pid files are left over
service h2 stop
service h2 zap

echo "[INFO] Don't forget to remove the database file 'CERBERUS_MAIN.h2.db' (check /opt/db/h2/bin ?)"

echo "Complete"
