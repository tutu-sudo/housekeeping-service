-- ============================================
-- 服务人员用户ID重新编号迁移脚本
-- ============================================
-- 用途：
-- 1. 删除前4个用户（user_id: 1-4，包括admin、user1、tutu、user2）
-- 2. 将staff001-staff050的user_id从5-54重新编号为1-50
-- 3. 同步更新所有关联表的user_id
-- 4. 在末尾添加新的admin管理员用户
-- 
-- 执行前请备份数据库！
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 步骤1：检查当前数据状态
-- ============================================
SELECT '当前user表数据（前10条）' AS info;
SELECT user_id, username, user_type, phone, email 
FROM user 
ORDER BY user_id 
LIMIT 10;

SELECT '当前staff表数据（前10条）' AS info;
SELECT staff_id, user_id, name 
FROM staff 
ORDER BY staff_id 
LIMIT 10;

SELECT '需要删除的用户' AS info;
SELECT user_id, username, user_type 
FROM user 
WHERE user_id BETWEEN 1 AND 4;

SELECT '需要重新编号的staff用户数量' AS info;
SELECT COUNT(*) as total_count
FROM user 
WHERE username LIKE 'staff%' 
  AND username REGEXP '^staff[0-9]{3}$'
  AND user_type = 2;

-- ============================================
-- 步骤2：临时禁用外键检查（必须）
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 步骤3：删除前4个用户及其关联数据
-- ============================================
-- 注意：由于外键约束，需要先删除关联数据

-- 3.1 删除staff表中user_id 1-4的记录
DELETE FROM staff WHERE user_id BETWEEN 1 AND 4;

-- 3.2 删除customer_detail表中user_id 1-4的记录
DELETE FROM customer_detail WHERE user_id BETWEEN 1 AND 4;

-- 3.3 删除其他可能关联user_id的表（根据实际情况调整）
-- 注意：如果appointment、review等表通过staff_id或customer_id关联，不需要直接删除
-- 但如果直接关联user_id，需要处理

-- 3.4 删除user表中user_id 1-4的记录
DELETE FROM user WHERE user_id BETWEEN 1 AND 4;

-- ============================================
-- 步骤4：将staff用户的user_id从5-54重新编号为1-50
-- 同时将staff_id重新编号为1-50
-- ============================================
-- 由于外键约束，需要先删除外键，更新数据，再重新添加外键

-- 4.1 删除所有关联staff_id的外键约束
-- 注意：MySQL可能不支持IF EXISTS，如果外键不存在会报错，可以忽略
ALTER TABLE staff DROP FOREIGN KEY fk_staff_user;
ALTER TABLE appointment DROP FOREIGN KEY fk_appointment_staff;
ALTER TABLE staff_service_skill DROP FOREIGN KEY fk_skill_staff;
ALTER TABLE schedule DROP FOREIGN KEY fk_schedule_staff;

-- 4.2 创建临时映射表（包含user_id和staff_id的映射）
CREATE TEMPORARY TABLE IF NOT EXISTS user_id_mapping (
    old_user_id BIGINT,
    new_user_id BIGINT,
    username VARCHAR(50),
    old_staff_id BIGINT,
    new_staff_id BIGINT,
    PRIMARY KEY (old_user_id),
    INDEX idx_username (username),
    INDEX idx_old_staff_id (old_staff_id)
);

-- 4.3 插入映射关系：user_id 5->1, 6->2, ..., 54->50
-- 同时映射 staff_id：需要根据user_id找到对应的staff_id，然后重新编号
-- 使用变量来生成连续的staff_id（兼容MySQL 5.7）
SET @staff_seq = 0;
INSERT INTO user_id_mapping (old_user_id, new_user_id, username, old_staff_id, new_staff_id)
SELECT 
    u.user_id AS old_user_id,
    u.user_id - 4 AS new_user_id,
    u.username,
    s.staff_id AS old_staff_id,
    (@staff_seq := @staff_seq + 1) AS new_staff_id
FROM user u
INNER JOIN staff s ON u.user_id = s.user_id
WHERE u.username LIKE 'staff%' 
  AND u.username REGEXP '^staff[0-9]{3}$'
  AND u.user_type = 2
  AND u.user_id BETWEEN 5 AND 54
ORDER BY u.user_id;

-- 4.4 先更新所有关联staff_id的表（在更新staff表之前）
-- 使用old_staff_id -> new_staff_id的映射
UPDATE appointment a
INNER JOIN user_id_mapping m ON a.staff_id = m.old_staff_id
SET a.staff_id = m.new_staff_id;

UPDATE staff_service_skill ssk
INNER JOIN user_id_mapping m ON ssk.staff_id = m.old_staff_id
SET ssk.staff_id = m.new_staff_id;

UPDATE schedule sch
INNER JOIN user_id_mapping m ON sch.staff_id = m.old_staff_id
SET sch.staff_id = m.new_staff_id;

-- 4.5 更新staff表：先更新user_id和staff_id到临时值（避免冲突）
-- 使用临时ID：staff_id -> 10000+new_staff_id, user_id -> new_user_id
UPDATE staff s
INNER JOIN user_id_mapping m ON s.staff_id = m.old_staff_id
SET s.staff_id = 10000 + m.new_staff_id,
    s.user_id = m.new_user_id;

-- 4.6 更新staff表：将临时staff_id改为最终staff_id
UPDATE staff s
INNER JOIN user_id_mapping m ON s.staff_id = 10000 + m.new_staff_id
SET s.staff_id = m.new_staff_id;

-- 4.7 更新其他可能直接关联user_id的表（根据实际情况调整）
-- 例如：operation_log表（如果有）
-- UPDATE operation_log ol
-- INNER JOIN user_id_mapping m ON ol.user_id = m.old_user_id
-- SET ol.user_id = m.new_user_id
-- WHERE ol.user_id BETWEEN 5 AND 54;

-- 4.8 更新customer_detail表（如果有相关记录）
UPDATE customer_detail cd
INNER JOIN user_id_mapping m ON cd.user_id = m.old_user_id
SET cd.user_id = m.new_user_id;

-- 4.9 更新user表（最后更新主表）
UPDATE user u
INNER JOIN user_id_mapping m ON u.user_id = m.old_user_id
SET u.user_id = m.new_user_id;

-- 4.10 删除临时映射表
DROP TEMPORARY TABLE IF EXISTS user_id_mapping;

-- 4.11 重新添加所有外键约束
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
-- 步骤5：重置AUTO_INCREMENT
-- ============================================
-- 获取当前最大user_id
SET @max_user_id = (SELECT COALESCE(MAX(user_id), 0) FROM user);
SET @next_user_id = @max_user_id + 1;

-- 重置user表的AUTO_INCREMENT
SET @sql = CONCAT('ALTER TABLE user AUTO_INCREMENT = ', @next_user_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 获取当前最大staff_id
SET @max_staff_id = (SELECT COALESCE(MAX(staff_id), 0) FROM staff);
SET @next_staff_id = @max_staff_id + 1;

-- 重置staff表的AUTO_INCREMENT
SET @sql = CONCAT('ALTER TABLE staff AUTO_INCREMENT = ', @next_staff_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 步骤6：添加新的admin管理员用户
-- ============================================
-- 密码：admin123
-- BCrypt哈希值：$2a$10$ke6FigSBk4b1GB87eMoZeuJO2JV140PFla8TR7YkEdO3yib5VSgrq
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `user_type`, `avatar`, `status`) VALUES
('admin', '$2a$10$ke6FigSBk4b1GB87eMoZeuJO2JV140PFla8TR7YkEdO3yib5VSgrq', 'admin@housekeeping.com', '13800138000', 3, NULL, 1);

-- ============================================
-- 步骤7：重新启用外键检查
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 步骤8：验证结果
-- ============================================
SELECT '迁移后的user表数据（前10条）' AS info;
SELECT user_id, username, user_type, phone, email 
FROM user 
ORDER BY user_id 
LIMIT 10;

SELECT '迁移后的staff表数据（前10条）' AS info;
SELECT staff_id, user_id, name 
FROM staff 
ORDER BY staff_id 
LIMIT 10;

SELECT '验证：staff001的user_id应该是1' AS info;
SELECT user_id, username, user_type 
FROM user 
WHERE username = 'staff001';

SELECT '验证：user_id=1对应的staff信息（应该是staff_id=1）' AS info;
SELECT s.staff_id, s.user_id, s.name, u.username
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
WHERE u.user_id = 1;

SELECT '验证：staff_id应该从1开始' AS info;
SELECT MIN(staff_id) as min_staff_id, MAX(staff_id) as max_staff_id, COUNT(*) as total_count
FROM staff;

SELECT '验证：admin用户' AS info;
SELECT user_id, username, user_type, email, phone 
FROM user 
WHERE username = 'admin';

SELECT '数据完整性检查：是否有staff记录的user_id在user表中不存在' AS info;
SELECT s.staff_id, s.user_id, s.name
FROM staff s
LEFT JOIN user u ON s.user_id = u.user_id
WHERE u.user_id IS NULL;

SELECT '数据完整性检查：是否有user_type=2的用户没有对应的staff记录' AS info;
SELECT u.user_id, u.username, u.user_type
FROM user u
LEFT JOIN staff s ON u.user_id = s.user_id
WHERE u.user_type = 2 AND s.staff_id IS NULL
ORDER BY u.user_id;

SELECT '统计信息' AS info;
SELECT 
    '总用户数' AS `类型`,
    COUNT(*) AS `数量`
FROM user
UNION ALL
SELECT 
    '服务人员数' AS `类型`,
    COUNT(*) AS `数量`
FROM user WHERE user_type = 2
UNION ALL
SELECT 
    '管理员数' AS `类型`,
    COUNT(*) AS `数量`
FROM user WHERE user_type = 3
UNION ALL
SELECT 
    'staff记录数' AS `类型`,
    COUNT(*) AS `数量`
FROM staff;

-- ============================================
-- 完成提示
-- ============================================
SELECT '迁移完成！' AS info;
SELECT 'staff001-staff050的user_id已重新编号为1-50' AS message;
SELECT '新的admin用户已添加到数据库末尾' AS reminder;
SELECT '请使用 admin / admin123 登录管理员账号' AS login_info;

