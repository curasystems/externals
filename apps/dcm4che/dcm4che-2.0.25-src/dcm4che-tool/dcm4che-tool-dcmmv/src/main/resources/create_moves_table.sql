/*
SQLyog Community Edition- MySQL GUI v6.15
MySQL - 5.0.26-community-nt : Database - dcmmoverservice_development
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

/*Table structure for table `moves` */

DROP TABLE IF EXISTS `moves`;

CREATE TABLE `moves` (
  `id` int(11) NOT NULL auto_increment,
  `status` enum('in_progress','complete','cancelling','cancelled') character set utf8 NOT NULL,
  `successful` tinyint(1) NOT NULL default '0',
  `error` varchar(256) character set utf8 default NULL,
  `study_uid` varchar(64) NOT NULL,
  `num_objects_found` int(11) NOT NULL default '0',
  `num_objects_moved` int(11) NOT NULL default '0',
  `source_ae` varchar(16) character set utf8 default NULL,
  `destination_ae` varchar(16) character set utf8 default NULL,
  `anonymized` tinyint(1) default '0',
  `anonymize_data` text character set utf8,
  `uid_mapping_doc` text character set utf8,
  `storage_commit_failures_doc` text character set utf8,
  `move_started` datetime default NULL,
  `move_updated` datetime default NULL,
  `move_ended` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;