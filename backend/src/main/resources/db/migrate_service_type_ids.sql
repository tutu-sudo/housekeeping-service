-- ============================================
-- 服务类型ID迁移脚本
-- ============================================
-- 用途：删除service_type表中ID 1-6的旧数据，将ID 7-12重新编号为1-6
-- 同时更新service表中关联的service_type_id
-- 
-- 执行前请备份数据库！
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 步骤1：检查当前数据状态
-- ============================================
-- 查看service_type表当前数据
SELECT '当前service_type表数据' AS info;
SELECT service_type_id, type_name FROM service_type ORDER BY service_type_id;

-- 查看service表关联情况
SELECT '当前service表关联情况' AS info;
SELECT service_type_id, COUNT(*) as service_count 
FROM service 
GROUP BY service_type_id 
ORDER BY service_type_id;

-- ============================================
-- 步骤2：临时禁用外键检查（必须，否则无法更新主键）
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 步骤3：先更新service_type表，将ID 7-12改为临时ID 100-105
-- ============================================
-- 这样可以避免与1-6冲突
UPDATE `service_type` 
SET `service_type_id` = CASE 
    WHEN `service_type_id` = 7 THEN 100
    WHEN `service_type_id` = 8 THEN 101
    WHEN `service_type_id` = 9 THEN 102
    WHEN `service_type_id` = 10 THEN 103
    WHEN `service_type_id` = 11 THEN 104
    WHEN `service_type_id` = 12 THEN 105
    ELSE `service_type_id`
END
WHERE `service_type_id` BETWEEN 7 AND 12;

-- ============================================
-- 步骤4：更新service表中的service_type_id到临时ID
-- ============================================
UPDATE `service` 
SET `service_type_id` = CASE 
    WHEN `service_type_id` = 7 THEN 100
    WHEN `service_type_id` = 8 THEN 101
    WHEN `service_type_id` = 9 THEN 102
    WHEN `service_type_id` = 10 THEN 103
    WHEN `service_type_id` = 11 THEN 104
    WHEN `service_type_id` = 12 THEN 105
    ELSE `service_type_id`
END
WHERE `service_type_id` BETWEEN 7 AND 12;

-- ============================================
-- 步骤5：删除service_type表中ID 1-6的旧数据
-- ============================================
DELETE FROM `service_type` WHERE `service_type_id` BETWEEN 1 AND 6;

-- ============================================
-- 步骤6：将service_type表中临时ID 100-105改为1-6
-- ============================================
UPDATE `service_type` 
SET `service_type_id` = CASE 
    WHEN `service_type_id` = 100 THEN 1
    WHEN `service_type_id` = 101 THEN 2
    WHEN `service_type_id` = 102 THEN 3
    WHEN `service_type_id` = 103 THEN 4
    WHEN `service_type_id` = 104 THEN 5
    WHEN `service_type_id` = 105 THEN 6
    ELSE `service_type_id`
END
WHERE `service_type_id` BETWEEN 100 AND 105;

-- ============================================
-- 步骤7：将service表中的临时ID 100-105改为1-6
-- ============================================
UPDATE `service` 
SET `service_type_id` = CASE 
    WHEN `service_type_id` = 100 THEN 1
    WHEN `service_type_id` = 101 THEN 2
    WHEN `service_type_id` = 102 THEN 3
    WHEN `service_type_id` = 103 THEN 4
    WHEN `service_type_id` = 104 THEN 5
    WHEN `service_type_id` = 105 THEN 6
    ELSE `service_type_id`
END
WHERE `service_type_id` BETWEEN 100 AND 105;

-- 验证中间结果
SELECT 'service表更新到临时ID后的关联情况' AS info;
SELECT service_type_id, COUNT(*) as service_count 
FROM service 
GROUP BY service_type_id 
ORDER BY service_type_id;

-- ============================================
-- 步骤8：重置AUTO_INCREMENT
-- ============================================
ALTER TABLE `service_type` AUTO_INCREMENT = 7;

-- ============================================
-- 步骤9：重新启用外键检查
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 步骤8：验证最终结果
-- ============================================
SELECT '最终service_type表数据' AS info;
SELECT service_type_id, type_name, description, base_price, min_duration 
FROM service_type 
ORDER BY service_type_id;

SELECT '最终service表关联情况' AS info;
SELECT s.service_type_id, st.type_name, COUNT(*) as service_count 
FROM service s
LEFT JOIN service_type st ON s.service_type_id = st.service_type_id
GROUP BY s.service_type_id, st.type_name
ORDER BY s.service_type_id;

-- 验证数据完整性
SELECT '数据完整性检查' AS info;
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 数据一致，所有service的service_type_id都有对应的service_type'
        ELSE CONCAT('✗ 发现 ', COUNT(*), ' 条service记录的service_type_id在service_type表中不存在')
    END AS check_result
FROM service s
LEFT JOIN service_type st ON s.service_type_id = st.service_type_id
WHERE st.service_type_id IS NULL;

