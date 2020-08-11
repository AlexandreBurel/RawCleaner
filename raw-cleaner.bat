@echo off
title Raw Cleaner
cd target\RawCleaner-1.0-SNAPSHOT
java -Xmx2G -cp "lib/*;config;RawCleaner-1.0-SNAPSHOT.jar" -Dlog4j.configurationFile=config/log4j.xml fr.lsmbo.rawcleaner.App %*
cd ../..
rem pause