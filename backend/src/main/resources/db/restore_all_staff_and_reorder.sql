-- ============================================
-- 恢复所有缺失的staff记录并重新排序
-- ============================================
-- 用途：基于user表中的所有staff用户，恢复缺失的staff记录
-- 然后重新排序staff_id为1-50
-- 
-- 执行前请备份数据库！
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 步骤1：检查当前状态
-- ============================================
SELECT '=== 步骤1：检查当前状态 ===' AS info;

SELECT 'user表中staff用户总数' AS info, COUNT(*) AS count
FROM user WHERE user_type = 2;

SELECT 'staff表中记录总数' AS info, COUNT(*) AS count
FROM staff;

SELECT '缺少staff记录的user（前10个）' AS info;
SELECT u.user_id, u.username, u.phone, u.email
FROM user u
LEFT JOIN staff s ON u.user_id = s.user_id
WHERE u.user_type = 2 AND s.staff_id IS NULL
ORDER BY u.user_id
LIMIT 10;

-- ============================================
-- 步骤2：临时禁用外键检查
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 步骤3：为所有缺少staff记录的user创建staff记录
-- ============================================
SELECT '=== 步骤3：恢复缺失的staff记录 ===' AS info;

-- 为缺少staff记录的user创建默认staff记录
-- 使用user表中的基本信息，其他字段使用默认值
INSERT INTO staff (
    user_id, 
    name, 
    gender, 
    birth_date, 
    origin, 
    id_card, 
    avatar, 
    resume, 
    work_experience, 
    hourly_rate, 
    rating, 
    verification_status
)
SELECT 
    u.user_id,
    -- 从username提取名称，如果没有则使用默认值
    CASE 
        WHEN u.username LIKE 'staff%' THEN 
            CONCAT('员工', SUBSTRING(u.username, 7))
        ELSE CONCAT('员工', u.user_id)
    END AS name,
    0 AS gender,  -- 默认女性
    '1990-01-01' AS birth_date,  -- 默认日期
    '未知' AS origin,  -- 默认籍贯
    CONCAT('00000019900101000', LPAD(u.user_id, 3, '0')) AS id_card,  -- 生成默认身份证号
    NULL AS avatar,
    CONCAT('从事家政服务，经验丰富，工作认真负责。') AS resume,
    5 AS work_experience,  -- 默认5年
    50.00 AS hourly_rate,  -- 默认50元/小时
    4.5 AS rating,  -- 默认4.5分
    1 AS verification_status  -- 默认已认证
FROM user u
LEFT JOIN staff s ON u.user_id = s.user_id
WHERE u.user_type = 2 AND s.staff_id IS NULL
ORDER BY u.user_id;

SELECT CONCAT('已插入 ', ROW_COUNT(), ' 条缺失的staff记录') AS info;

-- ============================================
-- 步骤4：检查是否有原始数据需要恢复
-- ============================================
-- 如果有原始插入脚本的数据，可以在这里手动更新前4个记录
-- 这里先检查staff001-004是否存在，如果存在但数据不对，可以更新

SELECT '=== 步骤4：检查并更新staff001-004的数据 ===' AS info;

-- 更新staff001 (王芳) - 如果存在
UPDATE staff s
INNER JOIN user u ON s.user_id = u.user_id
SET 
    s.name = '王芳',
    s.gender = 0,
    s.birth_date = '1985-05-15',
    s.origin = '河南',
    s.id_card = '410105198505150001',
    s.resume = '从事家政服务10年，经验丰富，擅长日常保洁和深度清洁，工作认真负责，深受客户好评。',
    s.work_experience = 10,
    s.hourly_rate = 55.00,
    s.rating = 4.8
WHERE u.username = 'staff001';

-- 更新staff002 (李霞)
UPDATE staff s
INNER JOIN user u ON s.user_id = u.user_id
SET 
    s.name = '李霞',
    s.gender = 0,
    s.birth_date = '1990-03-22',
    s.origin = '山东',
    s.id_card = '370102199003220002',
    s.resume = '从事家政服务8年，擅长日常保洁、整理收纳，服务态度好，细致耐心。',
    s.work_experience = 8,
    s.hourly_rate = 52.00,
    s.rating = 4.7
WHERE u.username = 'staff002';

-- 更新staff003 (张梅)
UPDATE staff s
INNER JOIN user u ON s.user_id = u.user_id
SET 
    s.name = '张梅',
    s.gender = 0,
    s.birth_date = '1988-07-10',
    s.origin = '河北',
    s.id_card = '130102198807100003',
    s.resume = '从事家政服务9年，擅长日常保洁、家具保养，工作认真，客户满意度高。',
    s.work_experience = 9,
    s.hourly_rate = 53.00,
    s.rating = 4.6
WHERE u.username = 'staff003';

-- 更新staff004 (刘美)
UPDATE staff s
INNER JOIN user u ON s.user_id = u.user_id
SET 
    s.name = '刘美',
    s.gender = 0,
    s.birth_date = '1992-11-05',
    s.origin = '安徽',
    s.id_card = '340102199211050004',
    s.resume = '从事家政服务6年，擅长日常保洁、窗户清洁，年轻有活力，服务态度好。',
    s.work_experience = 6,
    s.hourly_rate = 50.00,
    s.rating = 4.5
WHERE u.username = 'staff004';

SELECT '已更新staff001-004的数据' AS info;

-- ============================================
-- 步骤5：删除外键约束（准备重新排序）
-- ============================================
SELECT '=== 步骤5：删除外键约束 ===' AS info;
SELECT '注意：如果外键不存在会报错，但可以忽略' AS warning;

ALTER TABLE appointment DROP FOREIGN KEY fk_appointment_staff;
ALTER TABLE staff_service_skill DROP FOREIGN KEY fk_skill_staff;
ALTER TABLE schedule DROP FOREIGN KEY fk_schedule_staff;
ALTER TABLE staff DROP FOREIGN KEY fk_staff_user;

-- ============================================
-- 步骤6：创建临时映射表并重新排序
-- ============================================
SELECT '=== 步骤6：重新排序staff_id ===' AS info;

-- 创建临时映射表
CREATE TEMPORARY TABLE IF NOT EXISTS staff_id_mapping (
    old_staff_id BIGINT,
    new_staff_id BIGINT,
    user_id BIGINT,
    username VARCHAR(50),
    PRIMARY KEY (old_staff_id),
    INDEX idx_user_id (user_id)
);

TRUNCATE TABLE staff_id_mapping;

-- 插入映射关系：确保staff001-004在前4个位置，其他按user_id排序
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

SELECT '映射关系已创建' AS info;
SELECT CONCAT('总共 ', COUNT(*), ' 条记录需要重新排序') AS info FROM staff_id_mapping;

-- ============================================
-- 步骤7：更新所有关联表
-- ============================================
SELECT '=== 步骤7：更新关联表 ===' AS info;

-- 更新appointment表
UPDATE appointment a
INNER JOIN staff_id_mapping m ON a.staff_id = m.old_staff_id
SET a.staff_id = m.new_staff_id;

SELECT CONCAT('已更新appointment表: ', ROW_COUNT(), ' 条记录') AS info;

-- 更新staff_service_skill表
UPDATE staff_service_skill ssk
INNER JOIN staff_id_mapping m ON ssk.staff_id = m.old_staff_id
SET ssk.staff_id = m.new_staff_id;

SELECT CONCAT('已更新staff_service_skill表: ', ROW_COUNT(), ' 条记录') AS info;

-- 更新schedule表
UPDATE schedule sch
INNER JOIN staff_id_mapping m ON sch.staff_id = m.old_staff_id
SET sch.staff_id = m.new_staff_id;

SELECT CONCAT('已更新schedule表: ', ROW_COUNT(), ' 条记录') AS info;

-- ============================================
-- 步骤8：更新staff表
-- ============================================
SELECT '=== 步骤8：更新staff表 ===' AS info;

-- 先更新到临时ID（避免冲突，使用10000+作为临时ID）
UPDATE staff s
INNER JOIN staff_id_mapping m ON s.staff_id = m.old_staff_id
SET s.staff_id = 10000 + m.new_staff_id;

-- 再更新到最终ID
UPDATE staff s
INNER JOIN staff_id_mapping m ON s.staff_id = 10000 + m.new_staff_id
SET s.staff_id = m.new_staff_id;

SELECT '已更新staff表' AS info;

-- ============================================
-- 步骤9：删除临时映射表
-- ============================================
DROP TEMPORARY TABLE IF EXISTS staff_id_mapping;

-- ============================================
-- 步骤10：重新添加外键约束
-- ============================================
SELECT '=== 步骤10：重新添加外键约束 ===' AS info;

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
-- 步骤11：重置AUTO_INCREMENT
-- ============================================
SET @max_staff_id = (SELECT COALESCE(MAX(staff_id), 0) FROM staff);
SET @next_staff_id = @max_staff_id + 1;

SET @sql = CONCAT('ALTER TABLE staff AUTO_INCREMENT = ', @next_staff_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT CONCAT('已重置AUTO_INCREMENT为', @next_staff_id) AS info;

-- ============================================
-- 步骤12：重新启用外键检查
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 步骤13：验证结果
-- ============================================
SELECT '=== 步骤13：验证结果 ===' AS info;

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
SELECT '============================================' AS separator;
SELECT '恢复和重新排序完成！' AS info;
SELECT 'staff_id已重新编号为1-50，按user_id排序' AS message;
SELECT 'staff001对应staff_id=1' AS reminder;
SELECT '============================================' AS separator;

