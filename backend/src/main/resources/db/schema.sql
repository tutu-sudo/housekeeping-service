-- 家政服务预约平台数据库设计
-- 数据库名称: housekeeping_service
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `housekeeping_service` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `housekeeping_service`;

-- ============================================
-- 4.1 用户相关实体表
-- ============================================

-- 用户基础信息表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型：1-顾客，2-服务人员，3-管理员',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';

-- 顾客详细信息表
DROP TABLE IF EXISTS `customer_detail`;
CREATE TABLE `customer_detail` (
    `customer_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '顾客ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（关联user表）',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` TINYINT DEFAULT NULL COMMENT '性别：0-女，1-男',
    `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `contact_preference` VARCHAR(50) DEFAULT NULL COMMENT '联系偏好：phone-电话，email-邮件，sms-短信',
    `points` INT NOT NULL DEFAULT 0 COMMENT '积分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`customer_id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_name` (`name`),
    CONSTRAINT `fk_customer_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='顾客详细信息表';

-- 家政服务人员表
DROP TABLE IF EXISTS `staff`;
CREATE TABLE `staff` (
    `staff_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务人员ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（关联user表）',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` TINYINT DEFAULT NULL COMMENT '性别：0-女，1-男',
    `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
    `origin` VARCHAR(100) DEFAULT NULL COMMENT '籍贯',
    `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `resume` TEXT DEFAULT NULL COMMENT '简历',
    `work_experience` INT DEFAULT 0 COMMENT '工作经验（年）',
    `hourly_rate` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '时薪',
    `rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '综合评分（0-5分）',
    `verification_status` TINYINT NOT NULL DEFAULT 0 COMMENT '验证状态：0-待审核，1-审核通过，2-审核驳回',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`staff_id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_id_card` (`id_card`),
    KEY `idx_verification_status` (`verification_status`),
    KEY `idx_rating` (`rating`),
    CONSTRAINT `fk_staff_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家政服务人员表';

-- ============================================
-- 4.2 服务相关实体表（业务核心数据表）
-- ============================================

-- 服务类型表（服务类型配置）
DROP TABLE IF EXISTS `service_type`;
CREATE TABLE `service_type` (
    `service_type_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务类型ID',
    `type_name` VARCHAR(50) NOT NULL COMMENT '类型名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `base_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '基础价格',
    `min_duration` INT NOT NULL DEFAULT 1 COMMENT '最小时长（小时）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`service_type_id`),
    UNIQUE KEY `uk_type_name` (`type_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务类型表';

-- 服务表
DROP TABLE IF EXISTS `service`;
CREATE TABLE `service` (
    `service_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务ID',
    `service_type_id` BIGINT NOT NULL COMMENT '服务类型ID（关联service_type表）',
    `service_name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT '描述',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
    `estimated_duration` INT NOT NULL DEFAULT 1 COMMENT '预估时长（小时）',
    `available_status` TINYINT NOT NULL DEFAULT 1 COMMENT '可用状态：0-不可用，1-可用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`service_id`),
    KEY `idx_service_type_id` (`service_type_id`),
    KEY `idx_available_status` (`available_status`),
    CONSTRAINT `fk_service_service_type` FOREIGN KEY (`service_type_id`) REFERENCES `service_type` (`service_type_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务表';

-- 服务技能关联表
DROP TABLE IF EXISTS `staff_service_skill`;
CREATE TABLE `staff_service_skill` (
    `skill_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `staff_id` BIGINT NOT NULL COMMENT '服务人员ID（关联staff表）',
    `service_id` BIGINT NOT NULL COMMENT '服务ID（关联service表）',
    `proficiency_level` TINYINT NOT NULL DEFAULT 1 COMMENT '熟练程度：1-初级，2-中级，3-高级',
    `certificate_url` VARCHAR(255) DEFAULT NULL COMMENT '证书链接',
    `experience_years` INT DEFAULT 0 COMMENT '经验年限（年）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`skill_id`),
    UNIQUE KEY `uk_staff_service` (`staff_id`, `service_id`),
    KEY `idx_staff_id` (`staff_id`),
    KEY `idx_service_id` (`service_id`),
    CONSTRAINT `fk_skill_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_skill_service` FOREIGN KEY (`service_id`) REFERENCES `service` (`service_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务技能关联表';

-- 预约记录表
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment` (
    `appointment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    `customer_id` BIGINT NOT NULL COMMENT '顾客ID（关联customer_detail表）',
    `staff_id` BIGINT NOT NULL COMMENT '服务人员ID（关联staff表）',
    `service_id` BIGINT NOT NULL COMMENT '服务ID（关联service表）',
    `appointment_date` DATE NOT NULL COMMENT '预约日期',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `total_duration` DECIMAL(5,2) NOT NULL COMMENT '总时长（小时）',
    `service_address` VARCHAR(255) NOT NULL COMMENT '服务地址',
    `special_requirements` VARCHAR(500) DEFAULT NULL COMMENT '特殊要求',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待确认，1-已确认，2-进行中，3-已完成，4-已取消，5-已拒绝',
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`appointment_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_staff_id` (`staff_id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_appointment_date` (`appointment_date`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_appointment_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer_detail` (`customer_id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_appointment_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_appointment_service` FOREIGN KEY (`service_id`) REFERENCES `service` (`service_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录表';

-- 订单支付表
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment` (
    `payment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `appointment_id` BIGINT NOT NULL COMMENT '预约ID（关联appointment表）',
    `payment_amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式：alipay-支付宝，wechat-微信支付',
    `payment_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款',
    `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方交易号',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`payment_id`),
    UNIQUE KEY `uk_appointment_id` (`appointment_id`),
    KEY `idx_payment_status` (`payment_status`),
    KEY `idx_transaction_id` (`transaction_id`),
    CONSTRAINT `fk_payment_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`appointment_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单支付表';

-- 服务评价表
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

-- ============================================
-- 4.3 运营管理实体表（运营管理数据表）
-- ============================================

-- 公司信息表
DROP TABLE IF EXISTS `company`;
CREATE TABLE `company` (
    `company_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公司ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `business_hours` VARCHAR(100) DEFAULT NULL COMMENT '营业时间（如：09:00-18:00）',
    `service_cities` VARCHAR(500) DEFAULT NULL COMMENT '服务城市（多个城市用逗号分隔）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司信息表';

-- 员工排班表
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule` (
    `schedule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班ID',
    `staff_id` BIGINT NOT NULL COMMENT '服务人员ID（关联staff表）',
    `work_date` DATE NOT NULL COMMENT '工作日期',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `available_status` TINYINT NOT NULL DEFAULT 1 COMMENT '可用状态：0-不可用，1-可用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`schedule_id`),
    KEY `idx_staff_id` (`staff_id`),
    KEY `idx_work_date` (`work_date`),
    KEY `idx_available_status` (`available_status`),
    CONSTRAINT `fk_schedule_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工排班表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认管理员用户（密码：admin123，实际使用时需要加密）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `user_type`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pL5O', 'admin@housekeeping.com', '13800138000', 3, 1);

-- 插入服务类型示例数据
INSERT INTO `service_type` (`type_name`, `description`, `base_price`, `min_duration`) VALUES
('日常保洁', '包括客厅、卧室、厨房、卫生间等区域的日常清洁服务', 50.00, 2),
('深度保洁', '包括日常保洁的所有内容，外加深度清洁、除垢、消毒等服务', 100.00, 3),
('家电清洗', '空调、洗衣机、冰箱、油烟机等家电的清洗服务', 80.00, 1),
('月嫂服务', '专业的月嫂护理服务，包括新生儿护理、产妇护理等', 200.00, 8),
('育儿嫂服务', '专业的育儿嫂服务，包括婴幼儿日常护理、早教等', 150.00, 8),
('老人护理', '专业的老人护理服务，包括日常照料、健康监测等', 120.00, 4);

-- 插入公司信息示例数据
INSERT INTO `company` (`company_name`, `contact_phone`, `email`, `address`, `business_hours`, `service_cities`) VALUES
('家政服务有限公司', '400-123-4567', 'service@housekeeping.com', '北京市朝阳区xxx街道xxx号', '08:00-20:00', '北京,上海,广州,深圳');

