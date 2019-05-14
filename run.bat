@echo off
set CLASSPATH=starmx-1.3.1.jar;lib\javassist.jar;lib\log4j.jar;lib\stax-api.jar;lib\woodstox-core-lgpl.jar;lib\stax2-api.jar

set IMPERIUS_CP=lib\imperius-splcore.jar;lib\imperius-javaspl.jar;lib\antlr.jar
set CLASSPATH=%CLASSPATH%;%IMPERIUS_CP%

rem Add the path for starmx.xml and policy files to the classpath or use -Dstarmx.config.path=<DIR> property 

java -cp %CLASSPATH% org.starmx.StarMXFramework 
