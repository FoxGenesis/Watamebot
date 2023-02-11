@echo off
title WatameBot

:: Logging level (info | debug | trace)
set logLevel=info
set tokenLocation=config/token.txt

java -DLOG_LEVEL=%logLevel% -p "watamebot.jar;lib;plugins" --add-modules ALL-MODULE-PATH -m watamebot/net.foxgenesis.watame.Main -token %tokenLocation%
pause