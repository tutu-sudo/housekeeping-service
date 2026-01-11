-- ============================================
-- 插入缺失的前4个staff记录（修复版）
-- ============================================
-- 根据原始插入脚本，直接插入前4个staff记录
-- 使用当前staff001-staff004的实际user_id
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 步骤1：获取当前staff001-staff004的user_id
-- ============================================
SET @staff001_user_id = (SELECT user_id FROM user WHERE username = 'staff001');
SET @staff002_user_id = (SELECT user_id FROM user WHERE username = 'staff002');
SET @staff003_user_id = (SELECT user_id FROM user WHERE username = 'staff003');
SET @staff004_user_id = (SELECT user_id FROM user WHERE username = 'staff004');

SELECT '准备插入的user_id' AS info;
SELECT 
    @staff001_user_id AS staff001_user_id,
    @staff002_user_id AS staff002_user_id,
    @staff003_user_id AS staff003_user_id,
    @staff004_user_id AS staff004_user_id;

-- ============================================
-- 步骤2：检查这些user_id是否已有staff记录
-- ============================================
SELECT '检查是否已有staff记录' AS info;
SELECT 
    u.user_id,
    u.username,
    s.staff_id,
    s.name
FROM user u
LEFT JOIN staff s ON u.user_id = s.user_id
WHERE u.username IN ('staff001', 'staff002', 'staff003', 'staff004')
ORDER BY u.user_id;

-- ============================================
-- 步骤3：删除已存在的staff记录（如果存在）
-- ============================================
-- 如果这些user_id已经有staff记录，先删除它们
DELETE FROM staff 
WHERE user_id IN (@staff001_user_id, @staff002_user_id, @staff003_user_id, @staff004_user_id);

SELECT '已删除旧记录（如果有）' AS info;

-- ============================================
-- 步骤4：插入前4个staff记录
-- ============================================
-- 根据原始插入脚本的数据
-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 插入staff001 (王芳)
INSERT INTO `staff` (`user_id`, `name`, `gender`, `birth_date`, `origin`, `id_card`, `avatar`, `resume`, `work_experience`, `hourly_rate`, `rating`, `verification_status`) 
VALUES (
    @staff001_user_id,
    '王芳',
    0,
    '1985-05-15',
    '河南',
    '410105198505150001',
    NULL,
    '从事家政服务10年，经验丰富，擅长日常保洁和深度清洁，工作认真负责，深受客户好评。',
    10,
    55.00,
    4.8,
    1
);

-- 插入staff002 (李霞)
INSERT INTO `staff` (`user_id`, `name`, `gender`, `birth_date`, `origin`, `id_card`, `avatar`, `resume`, `work_experience`, `hourly_rate`, `rating`, `verification_status`) 
VALUES (
    @staff002_user_id,
    '李霞',
    0,
    '1990-03-22',
    '山东',
    '370102199003220002',
    NULL,
    '从事家政服务8年，擅长日常保洁、整理收纳，服务态度好，细致耐心。',
    8,
    52.00,
    4.7,
    1
);

-- 插入staff003 (张梅)
INSERT INTO `staff` (`user_id`, `name`, `gender`, `birth_date`, `origin`, `id_card`, `avatar`, `resume`, `work_experience`, `hourly_rate`, `rating`, `verification_status`) 
VALUES (
    @staff003_user_id,
    '张梅',
    0,
    '1988-07-10',
    '河北',
    '130102198807100003',
    NULL,
    '从事家政服务9年，擅长日常保洁、家具保养，工作认真，客户满意度高。',
    9,
    53.00,
    4.6,
    1
);

-- 插入staff004 (刘美)
INSERT INTO `staff` (`user_id`, `name`, `gender`, `birth_date`, `origin`, `id_card`, `avatar`, `resume`, `work_experience`, `hourly_rate`, `rating`, `verification_status`) 
VALUES (
    @staff004_user_id,
    '刘美',
    0,
    '1992-11-05',
    '安徽',
    '340102199211050004',
    NULL,
    '从事家政服务6年，擅长日常保洁、窗户清洁，年轻有活力，服务态度好。',
    6,
    50.00,
    4.5,
    1
);

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 步骤5：验证插入结果
-- ============================================
SELECT '插入后的staff记录数' AS info, COUNT(*) AS count FROM staff;

SELECT '插入后的前4个staff记录' AS info;
SELECT s.staff_id, s.user_id, s.name, u.username
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
WHERE u.username IN ('staff001', 'staff002', 'staff003', 'staff004')
ORDER BY u.user_id;

SELECT '确认：现在应该有50个staff记录' AS info;
SELECT COUNT(*) AS total_staff FROM staff;

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

