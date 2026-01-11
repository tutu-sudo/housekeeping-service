-- ============================================
-- 数据库索引优化脚本
-- 根据实际业务查询需求添加的额外索引
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 预约表额外索引
-- ============================================

-- 复合索引：按服务人员和日期查询
ALTER TABLE `appointment` ADD INDEX `idx_staff_date` (`staff_id`, `appointment_date`);

-- 复合索引：按顾客和状态查询
ALTER TABLE `appointment` ADD INDEX `idx_customer_status` (`customer_id`, `status`);

-- 复合索引：按日期和状态查询
ALTER TABLE `appointment` ADD INDEX `idx_date_status` (`appointment_date`, `status`);

-- ============================================
-- 服务人员表额外索引
-- ============================================

-- 复合索引：按验证状态和评分查询
ALTER TABLE `staff` ADD INDEX `idx_verification_rating` (`verification_status`, `rating`);

-- ============================================
-- 服务技能关联表额外索引
-- ============================================

-- 复合索引：按服务ID和熟练程度查询
ALTER TABLE `staff_service_skill` ADD INDEX `idx_service_proficiency` (`service_id`, `proficiency_level`);

-- ============================================
-- 排班表额外索引
-- ============================================

-- 复合索引：按日期和可用状态查询
ALTER TABLE `schedule` ADD INDEX `idx_date_status` (`work_date`, `available_status`);

-- 复合索引：按服务人员和日期范围查询
ALTER TABLE `schedule` ADD INDEX `idx_staff_date_range` (`staff_id`, `work_date`, `start_time`, `end_time`);

-- ============================================
-- 评价表额外索引
-- ============================================

-- 复合索引：按评分范围查询
ALTER TABLE `review` ADD INDEX `idx_rating_range` (`overall_rating`, `create_time`);

-- ============================================
-- 消息表额外索引
-- ============================================

-- 复合索引：按用户和未读状态查询
ALTER TABLE `message` ADD INDEX `idx_user_unread` (`user_id`, `is_read`, `create_time`);

-- ============================================
-- 日志表额外索引
-- ============================================

-- 复合索引：按用户和操作时间查询
ALTER TABLE `operation_log` ADD INDEX `idx_user_time` (`user_id`, `operation_time`);

-- 复合索引：按模块和操作类型查询
ALTER TABLE `operation_log` ADD INDEX `idx_module_type` (`operation_module`, `operation_type`, `operation_time`);

