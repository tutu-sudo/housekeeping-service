-- 快速版本：只添加缺失的字段
-- 根据错误信息，main_category已存在，所以只添加其他字段

-- 1. 添加 service.image_url（如果已存在会报错，可忽略）
ALTER TABLE `service` 
ADD COLUMN `image_url` VARCHAR(500) DEFAULT NULL COMMENT '服务图片URL' AFTER `main_category`;

-- 2. 添加 service_type.image_url（如果已存在会报错，可忽略）
ALTER TABLE `service_type` 
ADD COLUMN `image_url` VARCHAR(500) DEFAULT NULL COMMENT '服务类型图片URL（用于详情页展示）' AFTER `min_duration`;

-- 3. 添加索引（如果已存在会报错，可忽略）
CREATE INDEX `idx_main_category` ON `service` (`main_category`);






