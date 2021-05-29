-- --------------------------------------------------------
-- Hôte :                        146.59.239.239
-- Version du serveur:           10.3.25-MariaDB-0+deb10u1 - Debian 10
-- SE du serveur:                debian-linux-gnu
-- HeidiSQL Version:             9.4.0.5125
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Export de la structure de la table battle. groups
CREATE TABLE IF NOT EXISTS `groups` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `roles` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_n5j3m31chouwma55dvo2cce4u` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

-- Export de données de la table battle.groups : ~3 rows (environ)
/*!40000 ALTER TABLE `groups` DISABLE KEYS */;
REPLACE INTO `groups` (`id`, `name`, `roles`) VALUES
	(1, 'admin', 'USER,ADMIN,BATTLE'),
	(2, 'user', 'USER,BATTLE');
/*!40000 ALTER TABLE `groups` ENABLE KEYS */;

-- Export de la structure de la table battle. parameter
CREATE TABLE IF NOT EXISTS `parameter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `value` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_pmm2v6hl9f1ts86sdfpnsxqi8` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

-- Export de données de la table battle.parameter : ~1 rows (environ)
/*!40000 ALTER TABLE `parameter` DISABLE KEYS */;
REPLACE INTO `parameter` (`id`, `code`, `description`, `value`) VALUES
	(3, 'AUTO_CREATE_DISCORD', 'Auto create Discord Account.', 'true'),
	(4, 'VERSION', 'version', '1.9.130');
/*!40000 ALTER TABLE `parameter` ENABLE KEYS */;

-- Export de la structure de la table battle. troop
CREATE TABLE IF NOT EXISTS `troop` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fighter` longtext DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtqbxt0oigxk5cprbi790kc8tv` (`user_id`),
  CONSTRAINT `FKrvx92jdd6gd4ds4dytgvnvwdd` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKtqbxt0oigxk5cprbi790kc8tv` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4717 DEFAULT CHARSET=utf8;

-- Export de données de la table battle.troop : ~4 069 rows (environ)
/*!40000 ALTER TABLE `troop` DISABLE KEYS */;
REPLACE INTO `troop` (`id`, `fighter`, `name`, `user_id`) VALUES
	(1, '{"boost":{"300002":0.3,"300004":0.195,"300006":0.22,"300008":0.19,"300014":1.2336,"300016":0.9961,"300018":1.3531,"300020":1.4181,"300026":1.0911,"300028":1.1386,"300030":1.4181,"300032":1.3531,"300038":1.0436,"300040":1.0911,"300042":1.2234,"300044":1.1584},"troops":[{"id":1300010,"number":36333},{"id":1300022,"number":36333},{"id":1300034,"number":36333}],"heroes":[{"id":26400086,"skills":[25500170,25500180,25500190],"equipment":[27300043,27300044,27300045],"selected":true},{"id":26400140,"skills":[25500382,25500392,25500402],"equipment":[27300034,27300035,27300036],"selected":true},{"id":26400046,"skills":[25500080,25500089,25500098],"equipment":[27300052,27300053,27300054],"selected":false},{"id":26400149,"skills":[25500413,25500418,25500423],"equipment":[27300052,27300053,27300054],"selected":true}],"monster":false}', 'Player HQ30', NULL);
/*!40000 ALTER TABLE `troop` ENABLE KEYS */;

-- Export de la structure de la table battle. user
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(1000) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `player` bigint(20) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `cpt` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_f8ywmit8jxg8tjs66w5k5k44` (`code`),
  KEY `FKjjd2v5jgeirneedhvws8bbvcv` (`group_id`),
  CONSTRAINT `FKjjd2v5jgeirneedhvws8bbvcv` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`),
  CONSTRAINT `FKq26f3bgxxep4iicdrkgldrkic` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5891 DEFAULT CHARSET=utf8mb4;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;