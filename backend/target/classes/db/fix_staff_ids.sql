-- ============================================
-- 修复staff_id重新编号脚本
-- ============================================
-- 用途：将现有的staff记录的staff_id重新编号为1-50
-- 适用于已经执行过迁移脚本但staff_id没有正确重新编号的情况
-- 
-- 执行前请备份数据库！
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 步骤1：检查当前数据状态
-- ============================================
SELECT '当前staff表数据（前10条）' AS info;
SELECT staff_id, user_id, name 
FROM staff 
ORDER BY staff_id 
LIMIT 10;

SELECT '当前staff_id范围' AS info;
SELECT MIN(staff_id) as min_staff_id, MAX(staff_id) as max_staff_id, COUNT(*) as total_count
FROM staff;

-- ============================================
-- 步骤2：临时禁用外键检查
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 步骤3：删除所有关联staff_id的外键约束
-- ============================================
ALTER TABLE appointment DROP FOREIGN KEY fk_appointment_staff;
ALTER TABLE staff_service_skill DROP FOREIGN KEY fk_skill_staff;
ALTER TABLE schedule DROP FOREIGN KEY fk_schedule_staff;
ALTER TABLE staff DROP FOREIGN KEY fk_staff_user;

-- ============================================
-- 步骤4：创建临时映射表
-- ============================================
CREATE TEMPORARY TABLE IF NOT EXISTS staff_id_mapping (
    old_staff_id BIGINT,
    new_staff_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (old_staff_id),
    INDEX idx_user_id (user_id)
);

-- 4.1 插入映射关系：按user_id排序，重新编号为1-50
-- 确保staff001-staff004对应staff_id 1-4
SET @staff_seq = 0;
INSERT INTO staff_id_mapping (old_staff_id, new_staff_id, user_id)
SELECT 
    s.staff_id AS old_staff_id,
    (@staff_seq := @staff_seq + 1) AS new_staff_id,
    s.user_id
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
WHERE u.user_type = 2
ORDER BY u.user_id;

-- ============================================
-- 步骤5：更新所有关联staff_id的表
-- ============================================
-- 5.1 更新appointment表
UPDATE appointment a
INNER JOIN staff_id_mapping m ON a.staff_id = m.old_staff_id
SET a.staff_id = m.new_staff_id;

-- 5.2 更新staff_service_skill表
UPDATE staff_service_skill ssk
INNER JOIN staff_id_mapping m ON ssk.staff_id = m.old_staff_id
SET ssk.staff_id = m.new_staff_id;

-- 5.3 更新schedule表
UPDATE schedule sch
INNER JOIN staff_id_mapping m ON sch.staff_id = m.old_staff_id
SET sch.staff_id = m.new_staff_id;

-- ============================================
-- 步骤6：更新staff表
-- ============================================
-- 6.1 先更新到临时ID（避免冲突）
UPDATE staff s
INNER JOIN staff_id_mapping m ON s.staff_id = m.old_staff_id
SET s.staff_id = 10000 + m.new_staff_id;

-- 6.2 再更新到最终ID
UPDATE staff s
INNER JOIN staff_id_mapping m ON s.staff_id = 10000 + m.new_staff_id
SET s.staff_id = m.new_staff_id;

-- ============================================
-- 步骤7：删除临时映射表
-- ============================================
DROP TEMPORARY TABLE IF EXISTS staff_id_mapping;

-- ============================================
-- 步骤8：重新添加所有外键约束
-- ============================================
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

-- ============================================
-- 步骤9：重置AUTO_INCREMENT
-- ============================================
SET @max_staff_id = (SELECT COALESCE(MAX(staff_id), 0) FROM staff);
SET @next_staff_id = @max_staff_id + 1;

SET @sql = CONCAT('ALTER TABLE staff AUTO_INCREMENT = ', @next_staff_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 步骤10：重新启用外键检查
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 步骤11：验证结果
-- ============================================
SELECT '修复后的staff表数据（前10条）' AS info;
SELECT staff_id, user_id, name 
FROM staff 
ORDER BY staff_id 
LIMIT 10;

SELECT '验证：staff_id应该从1开始' AS info;
SELECT MIN(staff_id) as min_staff_id, MAX(staff_id) as max_staff_id, COUNT(*) as total_count
FROM staff;

SELECT '验证：前4个staff记录（应该是staff_id 1-4，对应staff001-004）' AS info;
SELECT s.staff_id, s.user_id, s.name, u.username
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
WHERE u.username IN ('staff001', 'staff002', 'staff003', 'staff004')
ORDER BY s.staff_id;

SELECT '数据完整性检查：是否有staff记录的user_id在user表中不存在' AS info;
SELECT s.staff_id, s.user_id, s.name
FROM staff s
LEFT JOIN user u ON s.user_id = u.user_id
WHERE u.user_id IS NULL;

-- ============================================
-- 完成提示
-- ============================================
SELECT '修复完成！' AS info;
SELECT 'staff_id已重新编号为1-50' AS message;
SELECT 'staff001对应staff_id=1, user_id=1' AS reminder;

