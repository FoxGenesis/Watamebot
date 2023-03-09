@echo off
title WatameBot

:: Logging level (info | debug | trace)
java -p "watamebot.jar;lib;plugins" --add-modules ALL-MODULE-PATH -m watamebot/net.foxgenesis.watame.Main
pause