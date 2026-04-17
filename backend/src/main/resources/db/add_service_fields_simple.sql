-- 简化版本：直接执行，如果字段已存在会报错但可以忽略
-- 适用于：字段可能已存在的情况

-- 为service表添加main_category字段（如果已存在会报错，可忽略）
ALTER TABLE `service` 
ADD COLUMN IF NOT EXISTS `main_category` VARCHAR(50) DEFAULT NULL COMMENT '服务大类名称（六大类之一）' AFTER `service_type_id`;

-- 为service表添加image_url字段（如果已存在会报错，可忽略）
ALTER TABLE `service` 
ADD COLUMN IF NOT EXISTS `image_url` VARCHAR(500) DEFAULT NULL COMMENT '服务图片URL' AFTER `main_category`;

-- 为service_type表添加image_url字段（如果已存在会报错，可忽略）
ALTER TABLE `service_type` 
ADD COLUMN IF NOT EXISTS `image_url` VARCHAR(500) DEFAULT NULL COMMENT '服务类型图片URL（用于详情页展示）' AFTER `min_duration`;

-- 添加索引（如果已存在会报错，可忽略）
CREATE INDEX IF NOT EXISTS `idx_main_category` ON `service` (`main_category`);






