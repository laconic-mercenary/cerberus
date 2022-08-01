@echo off
%APPSRV%\asadmin deploy --name cerberus cerberus*.ear && %APPSRV%\asadmin deploy --name landing-zone --contextroot landing-zone landing-zone*windows*.war