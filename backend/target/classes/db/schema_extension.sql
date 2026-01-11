-- ============================================
-- 家政服务预约平台数据库扩展表
-- 系统支撑模块相关表
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 消息通知相关表
-- ============================================

-- 站内信表
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（关联user表）',
    `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型：system-系统消息，order-订单消息，appointment-预约消息，review-评价消息',
    `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID（如订单ID、预约ID等）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    PRIMARY KEY (`message_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内信表';

-- 短信发送记录表
DROP TABLE IF EXISTS `sms_record`;
CREATE TABLE `sms_record` (
    `sms_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '短信ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `sms_type` VARCHAR(20) NOT NULL COMMENT '短信类型：verification-验证码，order-订单通知，appointment-预约通知',
    `content` VARCHAR(500) NOT NULL COMMENT '短信内容',
    `code` VARCHAR(10) DEFAULT NULL COMMENT '验证码（仅验证码类型）',
    `send_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送，1-发送成功，2-发送失败',
    `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（仅验证码类型）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`sms_id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_sms_type` (`sms_type`),
    KEY `idx_send_status` (`send_status`),
    KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信发送记录表';

-- 邮件发送记录表
DROP TABLE IF EXISTS `email_record`;
CREATE TABLE `email_record` (
    `email_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '邮件ID',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    `email_type` VARCHAR(20) NOT NULL COMMENT '邮件类型：verification-验证码，order-订单通知，appointment-预约通知，system-系统通知',
    `subject` VARCHAR(200) NOT NULL COMMENT '邮件主题',
    `content` TEXT NOT NULL COMMENT '邮件内容',
    `code` VARCHAR(10) DEFAULT NULL COMMENT '验证码（仅验证码类型）',
    `send_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送，1-发送成功，2-发送失败',
    `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（仅验证码类型）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`email_id`),
    KEY `idx_email` (`email`),
    KEY `idx_email_type` (`email_type`),
    KEY `idx_send_status` (`send_status`),
    KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件发送记录表';

-- ============================================
-- 文件管理相关表
-- ============================================

-- 文件表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
    `file_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_url` VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `file_category` VARCHAR(50) DEFAULT NULL COMMENT '文件分类：avatar-头像，certificate-证书，document-文档，other-其他',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID（如用户ID、服务人员ID等）',
    `upload_user_id` BIGINT DEFAULT NULL COMMENT '上传用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`file_id`),
    KEY `idx_file_category` (`file_category`),
    KEY `idx_related_id` (`related_id`),
    KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- ============================================
-- 日志记录相关表
-- ============================================

-- 业务操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：create-创建，update-更新，delete-删除，login-登录，logout-登出',
    `operation_module` VARCHAR(50) NOT NULL COMMENT '操作模块：user-用户，service-服务，appointment-预约，order-订单，payment-支付等',
    `operation_desc` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法：GET，POST，PUT，DELETE',
    `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `operation_status` TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_operation_module` (`operation_module`),
    KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务操作日志表';

-- 访问日志表
DROP TABLE IF EXISTS `access_log`;
CREATE TABLE `access_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（未登录则为NULL）',
    `request_method` VARCHAR(10) NOT NULL COMMENT '请求方法',
    `request_url` VARCHAR(500) NOT NULL COMMENT '请求URL',
    `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `response_status` INT DEFAULT NULL COMMENT '响应状态码',
    `response_time` INT DEFAULT NULL COMMENT '响应时间（毫秒）',
    `access_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_request_url` (`request_url`),
    KEY `idx_access_time` (`access_time`),
    KEY `idx_ip_address` (`ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访问日志表';

-- ============================================
-- 系统配置表（可选）
-- ============================================

-- 系统配置表
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
    `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT DEFAULT NULL COMMENT '配置值',
    `config_desc` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
    `config_type` VARCHAR(50) DEFAULT NULL COMMENT '配置类型：string-字符串，number-数字，boolean-布尔值，json-JSON',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

