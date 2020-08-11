@echo off
title Raw Cleaner
java -Xmx2G -cp "lib/*;config;RawCleaner-1.0-SNAPSHOT.jar" -Dlog4j.configurationFile=config/log4j.xml fr.lsmbo.rawcleaner.App %*
rem pause