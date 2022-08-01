@echo off
:RETRY
set /p ENV_APPSRV="Enter directory of 'asadmin' executable: "
if not exist %ENV_APPSRV%\asadmin goto RETRY 
echo "Successfully specified"

:RETRY
set /p ENV_JAVADB="Enter directory of 'ij' javadb executable: "
if not exist %ENV_JAVADB%\ij goto RETRY 
echo "Successfully specified"

rem this sets a global environment variable
setx APPSRV %ENV_APPSRV%
setx JAVADB %ENV_JAVADB%
pause