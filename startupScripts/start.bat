@echo off
title WatameBot

:: Logging level (info | debug | trace)
set logLevel=info
set tokenLocation=config/token.txt

java -DLOG_LEVEL=%logLevel% -jar "watamebot.jar" -token %tokenLocation%