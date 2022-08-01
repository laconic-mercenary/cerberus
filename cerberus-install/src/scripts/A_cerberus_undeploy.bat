@echo off
%APPSRV%\asadmin undeploy cerberus && %APPSRV%\asadmin restart-domain && %APPSRV%\asadmin undeploy landing-zone && %APPSRV%\asadmin restart-domain