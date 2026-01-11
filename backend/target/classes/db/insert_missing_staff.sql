-- ============================================
-- 插入缺失的前4个staff记录
-- ============================================
-- 根据原始插入脚本，插入前4个staff记录
-- 使用当前staff001-staff004的user_id
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 步骤1：获取当前staff001-staff004的user_id
-- ============================================
SELECT '当前staff001-staff004的user_id' AS info;
SELECT user_id, username 
FROM user 
WHERE username IN ('staff001', 'staff002', 'staff003', 'staff004')
ORDER BY username;

-- ============================================
-- 步骤2：检查哪些staff记录缺失
-- ============================================
SELECT '缺失staff记录的user' AS info;
SELECT u.user_id, u.username
FROM user u
LEFT JOIN staff s ON u.user_id = s.user_id
WHERE u.username IN ('staff001', 'staff002', 'staff003', 'staff004')
  AND s.staff_id IS NULL
ORDER BY u.user_id;

-- ============================================
-- 步骤3：插入缺失的staff记录
-- ============================================
-- 根据原始插入脚本的数据，插入前4个staff记录
-- 日常保洁服务人员 (4人) - service_id: 1

-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 插入staff001 (王芳)
INSERT INTO `staff` (`user_id`, `name`, `gender`, `birth_date`, `origin`, `id_card`, `avatar`, `resume`, `work_experience`, `hourly_rate`, `rating`, `verification_status`)
SELECT 
    (SELECT user_id FROM user WHERE username = 'staff001') AS user_id,
    '王芳' AS name,
    0 AS gender,
    '1985-05-15' AS birth_date,
    '河南' AS origin,
    '410105198505150001' AS id_card,
    NULL AS avatar,
    '从事家政服务10年，经验丰富，擅长日常保洁和深度清洁，工作认真负责，深受客户好评。' AS resume,
    10 AS work_experience,
    55.00 AS hourly_rate,
    4.8 AS rating,
    1 AS verification_status
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE user_id = (SELECT user_id FROM user WHERE username = 'staff001'));

-- 插入staff002 (李霞)
INSERT INTO `staff` (`user_id`, `name`, `gender`, `birth_date`, `origin`, `id_card`, `avatar`, `resume`, `work_experience`, `hourly_rate`, `rating`, `verification_status`)
SELECT 
    (SELECT user_id FROM user WHERE username = 'staff002') AS user_id,
    '李霞' AS name,
    0 AS gender,
    '1990-03-22' AS birth_date,
    '山东' AS origin,
    '370102199003220002' AS id_card,
    NULL AS avatar,
    '从事家政服务8年，擅长日常保洁、整理收纳，服务态度好，细致耐心。' AS resume,
    8 AS work_experience,
    52.00 AS hourly_rate,
    4.7 AS rating,
    1 AS verification_status
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE user_id = (SELECT user_id FROM user WHERE username = 'staff002'));

-- 插入staff003 (张梅)
INSERT INTO `staff` (`user_id`, `name`, `gender`, `birth_date`, `origin`, `id_card`, `avatar`, `resume`, `work_experience`, `hourly_rate`, `rating`, `verification_status`)
SELECT 
    (SELECT user_id FROM user WHERE username = 'staff003') AS user_id,
    '张梅' AS name,
    0 AS gender,
    '1988-07-10' AS birth_date,
    '河北' AS origin,
    '130102198807100003' AS id_card,
    NULL AS avatar,
    '从事家政服务9年，擅长日常保洁、家具保养，工作认真，客户满意度高。' AS resume,
    9 AS work_experience,
    53.00 AS hourly_rate,
    4.6 AS rating,
    1 AS verification_status
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE user_id = (SELECT user_id FROM user WHERE username = 'staff003'));

-- 插入staff004 (刘美)
INSERT INTO `staff` (`user_id`, `name`, `gender`, `birth_date`, `origin`, `id_card`, `avatar`, `resume`, `work_experience`, `hourly_rate`, `rating`, `verification_status`)
SELECT 
    (SELECT user_id FROM user WHERE username = 'staff004') AS user_id,
    '刘美' AS name,
    0 AS gender,
    '1992-11-05' AS birth_date,
    '安徽' AS origin,
    '340102199211050004' AS id_card,
    NULL AS avatar,
    '从事家政服务6年，擅长日常保洁、窗户清洁，年轻有活力，服务态度好。' AS resume,
    6 AS work_experience,
    50.00 AS hourly_rate,
    4.5 AS rating,
    1 AS verification_status
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE user_id = (SELECT user_id FROM user WHERE username = 'staff004'));

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 步骤4：验证插入结果
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

