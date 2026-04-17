-- 安全版本：为service和service_type表添加字段（检查字段是否存在）
-- 如果字段已存在，则跳过；如果不存在，则添加

-- ============================================
-- 1. 为service表添加main_category字段
-- ============================================
SET @dbname = DATABASE();
SET @tablename = 'service';
SET @columnname = 'main_category';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT ''字段 main_category 已存在，跳过'' AS message',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(50) DEFAULT NULL COMMENT ''服务大类名称（六大类之一）'' AFTER `service_type_id`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 2. 为service表添加image_url字段
-- ============================================
SET @columnname = 'image_url';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT ''字段 image_url 已存在，跳过'' AS message',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(500) DEFAULT NULL COMMENT ''服务图片URL'' AFTER `main_category`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 3. 为service_type表添加image_url字段
-- ============================================
SET @tablename = 'service_type';
SET @columnname = 'image_url';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT ''字段 image_url 已存在，跳过'' AS message',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` VARCHAR(500) DEFAULT NULL COMMENT ''服务类型图片URL（用于详情页展示）'' AFTER `min_duration`;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================
-- 4. 为service表添加main_category索引
-- ============================================
SET @indexname = 'idx_main_category';
SET @tablename = 'service';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (INDEX_NAME = @indexname)
  ) > 0,
  'SELECT ''索引 idx_main_category 已存在，跳过'' AS message',
  CONCAT('CREATE INDEX `', @indexname, '` ON `', @tablename, '` (`main_category`);')
));
PREPARE createIndexIfNotExists FROM @preparedStatement;
EXECUTE createIndexIfNotExists;
DEALLOCATE PREPARE createIndexIfNotExists;

-- ============================================
-- 验证：查看字段是否添加成功
-- ============================================
SELECT 
    TABLE_NAME AS '表名',
    COLUMN_NAME AS '字段名',
    DATA_TYPE AS '数据类型',
    COLUMN_COMMENT AS '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME IN ('service', 'service_type')
    AND COLUMN_NAME IN ('main_category', 'image_url')
ORDER BY TABLE_NAME, COLUMN_NAME;






