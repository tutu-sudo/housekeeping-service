-- 验证所有字段和索引是否已正确添加

-- ============================================
-- 1. 验证字段是否存在
-- ============================================
SELECT 
    TABLE_NAME AS '表名',
    COLUMN_NAME AS '字段名',
    DATA_TYPE AS '数据类型',
    CHARACTER_MAXIMUM_LENGTH AS '长度',
    COLUMN_COMMENT AS '注释',
    '✅ 已存在' AS '状态'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME IN ('service', 'service_type')
    AND COLUMN_NAME IN ('main_category', 'image_url')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- ============================================
-- 2. 验证索引是否存在
-- ============================================
SELECT 
    TABLE_NAME AS '表名',
    INDEX_NAME AS '索引名',
    COLUMN_NAME AS '字段名',
    '✅ 已存在' AS '状态'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'service'
    AND INDEX_NAME = 'idx_main_category';

-- ============================================
-- 3. 如果索引不存在，执行以下语句创建索引
-- ============================================
-- CREATE INDEX `idx_main_category` ON `service` (`main_category`);






