-- MySQL 5.7 兼容版本：手动检查并添加字段
-- 如果字段已存在，请注释掉对应的 ALTER TABLE 语句

-- ============================================
-- 步骤1：检查字段是否存在
-- ============================================
-- 执行以下查询，查看哪些字段已存在：
SELECT 
    TABLE_NAME AS '表名',
    COLUMN_NAME AS '字段名',
    '已存在' AS '状态'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME IN ('service', 'service_type')
    AND COLUMN_NAME IN ('main_category', 'image_url')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- ============================================
-- 步骤2：根据检查结果，执行以下语句
-- 如果字段已存在，请注释掉对应的 ALTER TABLE 语句
-- ============================================

-- 如果 main_category 不存在，取消下面的注释并执行：
-- ALTER TABLE `service` 
-- ADD COLUMN `main_category` VARCHAR(50) DEFAULT NULL COMMENT '服务大类名称（六大类之一）' AFTER `service_type_id`;

-- 如果 service.image_url 不存在，取消下面的注释并执行：
-- ALTER TABLE `service` 
-- ADD COLUMN `image_url` VARCHAR(500) DEFAULT NULL COMMENT '服务图片URL' AFTER `main_category`;

-- 如果 service_type.image_url 不存在，取消下面的注释并执行：
-- ALTER TABLE `service_type` 
-- ADD COLUMN `image_url` VARCHAR(500) DEFAULT NULL COMMENT '服务类型图片URL（用于详情页展示）' AFTER `min_duration`;

-- ============================================
-- 步骤3：添加索引（如果已存在会报错，可忽略）
-- ============================================
-- 检查索引是否存在：
SELECT 
    TABLE_NAME AS '表名',
    INDEX_NAME AS '索引名',
    '已存在' AS '状态'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'service'
    AND INDEX_NAME = 'idx_main_category';

-- 如果索引不存在，执行：
-- CREATE INDEX `idx_main_category` ON `service` (`main_category`);






