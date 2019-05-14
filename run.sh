#!/bin/sh

###
CLASSPATH=./starmx-1.3.1.jar:./lib/javassist.jar:./lib/log4j.jar:./lib/stax-api.jar:./lib/woodstox-core-lgpl.jar:./lib/stax2-api.jar

IMPERIUS_CP=./lib/imperius-splcore.jar:./lib/imperius-javaspl.jar:./lib/antlr.jar
CLASSPATH="$CLASSPATH:$IMPERIUS_CP"

if $cygwin; then
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi
echo CLASSPATH=$CLASSPATH

### Add the path for starmx.xml and policy files to the classpath or use -Dstarmx.config.path=<DIR> property 

java -classpath $CLASSPATH  org.starmx.StarMXFramework
