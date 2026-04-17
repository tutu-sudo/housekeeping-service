-- 新增：三方互评扩展表（家政自评 / 管理员评价）
-- 说明：
-- - 旧表 review 继续作为「顾客对订单的评价」（每单一条）
-- - 新表 review_extra 存「服务人员自评」与「管理员评价」（每单每角色一条）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `review_extra`;
CREATE TABLE `review_extra` (
  `review_extra_id` bigint NOT NULL AUTO_INCREMENT COMMENT '扩展评价ID',
  `appointment_id` bigint NOT NULL COMMENT '预约ID（关联appointment表）',
  `reviewer_role` tinyint NOT NULL COMMENT '评价者角色：2-服务人员，3-管理员',
  `review_target` tinyint NOT NULL COMMENT '评价对象：1-顾客，2-服务人员',
  `reviewer_user_id` bigint NOT NULL COMMENT '评价者userId（用于审计）',
  `overall_rating` decimal(3,2) NOT NULL COMMENT '总体评分（0-5分）',
  `review_content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`review_extra_id`) USING BTREE,
  UNIQUE KEY `uk_appt_role_target` (`appointment_id`, `reviewer_role`, `review_target`) USING BTREE,
  KEY `idx_appointment_id` (`appointment_id`) USING BTREE,
  KEY `idx_reviewer_user_id` (`reviewer_user_id`) USING BTREE,
  CONSTRAINT `fk_review_extra_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`appointment_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='扩展评价表（服务人员自评/管理员评价）' ROW_FORMAT=Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

