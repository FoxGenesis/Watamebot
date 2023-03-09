CREATE DATABASE IF NOT EXISTS {{database}};
CREATE TABLE IF NOT EXISTS `{{database}}`.`{{table}}` (`lookup` bigint unsigned NOT NULL,`name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,`property` varchar(500) NOT NULL,`type` enum('string','int','long','boolean','float','double') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',PRIMARY KEY (`lookup`,`name`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;