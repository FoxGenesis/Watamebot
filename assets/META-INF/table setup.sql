CREATE DATABASE IF NOT EXISTS `WatameBot`;
CREATE TABLE IF NOT EXISTS `WatameBot`.`Guild` ( `GuildID` BIGINT UNSIGNED NOT NULL , `GuildProperties` JSON NOT NULL, PRIMARY KEY (`GuildID`)) ENGINE = InnoDB;