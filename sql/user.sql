
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(32) DEFAULT NULL COMMENT '用户名',
  `id_card` varchar(32) DEFAULT NULL COMMENT '身份证号',
  `age` int(11) DEFAULT NULL,
  `email` varchar(32) DEFAULT NULL COMMENT '邮箱',
  `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
  `controller_action` varchar(64) DEFAULT NULL COMMENT 'MVC接口入口',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路ID',
  `visit_ip` varchar(32) DEFAULT NULL COMMENT '请求IP',
  `app_name` varchar(32) DEFAULT NULL COMMENT '请求应用名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
