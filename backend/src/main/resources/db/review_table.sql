-- 服务评价表
-- 如果已存在则删除（谨慎使用，仅用于开发环境）
-- 生产环境请确保数据已备份

DROP TABLE IF EXISTS `review`;

CREATE TABLE `review` (
    `review_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `appointment_id` BIGINT NOT NULL COMMENT '预约ID（关联appointment表）',
    `overall_rating` DECIMAL(3,2) NOT NULL COMMENT '总体评分（0-5分）',
    `service_attitude_rating` DECIMAL(3,2) DEFAULT NULL COMMENT '服务态度分（0-5分）',
    `professional_ability_rating` DECIMAL(3,2) DEFAULT NULL COMMENT '专业能力分（0-5分）',
    `service_quality_rating` DECIMAL(3,2) DEFAULT NULL COMMENT '服务质量分（0-5分）',
    `review_content` VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
    `is_anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`review_id`),
    UNIQUE KEY `uk_appointment_id` (`appointment_id`),
    KEY `idx_overall_rating` (`overall_rating`),
    CONSTRAINT `fk_review_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`appointment_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务评价表';

