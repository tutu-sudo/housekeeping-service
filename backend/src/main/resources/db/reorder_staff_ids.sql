-- ============================================
-- 重新排序staff_id，确保1-50按顺序排列
-- ============================================
-- 用途：将staff_id重新编号为1-50，按user_id排序
-- 确保staff001-004对应staff_id 1-4
-- 
-- 执行前请备份数据库！
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 步骤1：检查当前状态
-- ============================================
SELECT '当前staff表状态（前10条）' AS info;
SELECT staff_id, user_id, name, 
       (SELECT username FROM user WHERE user_id = staff.user_id) AS username
FROM staff 
ORDER BY staff_id 
LIMIT 10;

SELECT '当前staff_id范围' AS info;
SELECT MIN(staff_id) AS min_staff_id, MAX(staff_id) AS max_staff_id, COUNT(*) AS total
FROM staff;

SELECT '检查staff001-004的当前状态' AS info;
SELECT s.staff_id, s.user_id, s.name, u.username
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
WHERE u.username IN ('staff001', 'staff002', 'staff003', 'staff004')
ORDER BY u.user_id;

-- ============================================
-- 步骤2：临时禁用外键检查
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 步骤3：删除所有关联staff_id的外键约束
-- ============================================
-- 注意：如果外键不存在会报错，但可以继续执行（已禁用外键检查）
SELECT '正在删除外键约束...' AS info;
SELECT '注意：如果外键不存在，以下语句会报错，但可以忽略' AS warning;

-- 直接删除外键（如果不存在会报错，但可以忽略）
ALTER TABLE appointment DROP FOREIGN KEY fk_appointment_staff;
ALTER TABLE staff_service_skill DROP FOREIGN KEY fk_skill_staff;
ALTER TABLE schedule DROP FOREIGN KEY fk_schedule_staff;
ALTER TABLE staff DROP FOREIGN KEY fk_staff_user;

-- 对于MySQL 5.7，需要逐个尝试删除
-- 如果外键不存在，会报错，但可以继续执行

-- ============================================
-- 步骤4：创建临时映射表
-- ============================================
CREATE TEMPORARY TABLE IF NOT EXISTS staff_id_mapping (
    old_staff_id BIGINT,
    new_staff_id BIGINT,
    user_id BIGINT,
    username VARCHAR(50),
    PRIMARY KEY (old_staff_id),
    INDEX idx_user_id (user_id)
);

-- 清空临时表
TRUNCATE TABLE staff_id_mapping;

-- 4.1 插入映射关系：确保staff001-004在前4个位置，其他按user_id排序
SET @staff_seq = 0;

-- 先插入staff001-004（确保它们在前4个位置）
INSERT INTO staff_id_mapping (old_staff_id, new_staff_id, user_id, username)
SELECT 
    s.staff_id AS old_staff_id,
    (@staff_seq := @staff_seq + 1) AS new_staff_id,
    s.user_id,
    u.username
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
WHERE u.user_type = 2 
  AND u.username IN ('staff001', 'staff002', 'staff003', 'staff004')
ORDER BY u.username;

-- 再插入其他staff（按user_id排序）
INSERT INTO staff_id_mapping (old_staff_id, new_staff_id, user_id, username)
SELECT 
    s.staff_id AS old_staff_id,
    (@staff_seq := @staff_seq + 1) AS new_staff_id,
    s.user_id,
    u.username
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
WHERE u.user_type = 2 
  AND u.username NOT IN ('staff001', 'staff002', 'staff003', 'staff004')
ORDER BY u.user_id;

-- 显示映射关系（前10条）
SELECT '映射关系（前10条）' AS info;
SELECT old_staff_id, new_staff_id, user_id, username
FROM staff_id_mapping
ORDER BY new_staff_id
LIMIT 10;

-- ============================================
-- 步骤5：更新所有关联staff_id的表
-- ============================================
-- 5.1 更新appointment表
UPDATE appointment a
INNER JOIN staff_id_mapping m ON a.staff_id = m.old_staff_id
SET a.staff_id = m.new_staff_id;

SELECT '已更新appointment表' AS info;

-- 5.2 更新staff_service_skill表
UPDATE staff_service_skill ssk
INNER JOIN staff_id_mapping m ON ssk.staff_id = m.old_staff_id
SET ssk.staff_id = m.new_staff_id;

SELECT '已更新staff_service_skill表' AS info;

-- 5.3 更新schedule表
UPDATE schedule sch
INNER JOIN staff_id_mapping m ON sch.staff_id = m.old_staff_id
SET sch.staff_id = m.new_staff_id;

SELECT '已更新schedule表' AS info;

-- ============================================
-- 步骤6：更新staff表
-- ============================================
-- 6.1 先更新到临时ID（避免冲突，使用10000+作为临时ID）
UPDATE staff s
INNER JOIN staff_id_mapping m ON s.staff_id = m.old_staff_id
SET s.staff_id = 10000 + m.new_staff_id;

SELECT '已更新staff表到临时ID' AS info;

-- 6.2 再更新到最终ID
UPDATE staff s
INNER JOIN staff_id_mapping m ON s.staff_id = 10000 + m.new_staff_id
SET s.staff_id = m.new_staff_id;

SELECT '已更新staff表到最终ID' AS info;

-- ============================================
-- 步骤7：删除临时映射表
-- ============================================
DROP TEMPORARY TABLE IF EXISTS staff_id_mapping;

-- ============================================
-- 步骤8：重新添加所有外键约束
-- ============================================
-- 先检查外键是否已存在，如果已存在则跳过
ALTER TABLE staff 
ADD CONSTRAINT fk_staff_user 
FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE;

ALTER TABLE appointment 
ADD CONSTRAINT fk_appointment_staff 
FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`) ON DELETE RESTRICT;

ALTER TABLE staff_service_skill 
ADD CONSTRAINT fk_skill_staff 
FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`) ON DELETE CASCADE;

ALTER TABLE schedule 
ADD CONSTRAINT fk_schedule_staff 
FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`) ON DELETE CASCADE;

SELECT '已重新添加外键约束' AS info;

-- ============================================
-- 步骤9：重置AUTO_INCREMENT
-- ============================================
SET @max_staff_id = (SELECT COALESCE(MAX(staff_id), 0) FROM staff);
SET @next_staff_id = @max_staff_id + 1;

SET @sql = CONCAT('ALTER TABLE staff AUTO_INCREMENT = ', @next_staff_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT CONCAT('已重置AUTO_INCREMENT为', @next_staff_id) AS info;

-- ============================================
-- 步骤10：重新启用外键检查
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 步骤11：验证结果
-- ============================================
SELECT '重新排序后的staff表（前10条）' AS info;
SELECT s.staff_id, s.user_id, s.name, u.username
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
ORDER BY s.staff_id
LIMIT 10;

SELECT '验证：staff_id应该从1开始' AS info;
SELECT MIN(staff_id) AS min_staff_id, MAX(staff_id) AS max_staff_id, COUNT(*) AS total_count
FROM staff;

SELECT '验证：前4个staff记录（应该是staff001-004）' AS info;
SELECT s.staff_id, s.user_id, s.name, u.username
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
WHERE s.staff_id BETWEEN 1 AND 4
ORDER BY s.staff_id;

SELECT '验证：staff001-004对应的staff_id' AS info;
SELECT s.staff_id, s.user_id, s.name, u.username
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
WHERE u.username IN ('staff001', 'staff002', 'staff003', 'staff004')
ORDER BY s.staff_id;

SELECT '数据完整性检查' AS info;
SELECT 
    'user表中staff用户数' AS item,
    COUNT(*) AS count
FROM user WHERE user_type = 2
UNION ALL
SELECT 
    'staff表中记录数' AS item,
    COUNT(*) AS count
FROM staff
UNION ALL
SELECT 
    '有staff记录的user数' AS item,
    COUNT(*) AS count
FROM user u
INNER JOIN staff s ON u.user_id = s.user_id
WHERE u.user_type = 2
UNION ALL
SELECT 
    '缺少staff记录的user数' AS item,
    COUNT(*) AS count
FROM user u
LEFT JOIN staff s ON u.user_id = s.user_id
WHERE u.user_type = 2 AND s.staff_id IS NULL;

-- ============================================
-- 完成提示
-- ============================================
SELECT '重新排序完成！' AS info;
SELECT 'staff_id已重新编号为1-50，按user_id排序' AS message;
SELECT 'staff001对应staff_id=1（如果user_id最小）' AS reminder;

