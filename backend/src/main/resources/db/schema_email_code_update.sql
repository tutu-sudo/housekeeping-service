-- 为email_record表添加验证码相关字段
-- 如果字段已存在，此脚本会报错，可以忽略

ALTER TABLE `email_record` 
ADD COLUMN `code` VARCHAR(10) DEFAULT NULL COMMENT '验证码（仅验证码类型）' AFTER `content`,
ADD COLUMN `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（仅验证码类型）' AFTER `send_time`;

-- 添加索引以便快速查询验证码
ALTER TABLE `email_record` 
ADD INDEX `idx_code` (`code`);

