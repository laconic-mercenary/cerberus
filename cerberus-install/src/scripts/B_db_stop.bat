@echo off
set DB_FILE=D:\opt\db\CERBERUS_MAIN.h2
set H2_JAR=D:\opt\db\h2\bin\h2-1.3.176.jar
@echo on
echo "WARNING: make sure that the database file (CERBERUS_MAIN.h2.db) is deleted after this is complete."
@echo off
java -cp %H2_JAR% org.h2.tools.Server -tcpShutdown tcp:localhost:9092 && del %DB_FILE%
