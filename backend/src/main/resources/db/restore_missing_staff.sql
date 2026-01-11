-- ============================================
-- 恢复缺失的前4个staff记录脚本
-- ============================================
-- 用途：根据原始插入脚本，恢复前4个被删除的staff记录
-- 这些记录对应：王芳、李霞、张梅、刘美
-- 
-- 执行前请备份数据库！
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 步骤1：检查当前状态
-- ============================================
SELECT '当前staff记录数' AS info, COUNT(*) AS count FROM staff;

SELECT '当前user表中staff001-staff004的用户ID' AS info;
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
-- 步骤3：恢复缺失的staff记录
-- ============================================
-- 根据原始插入脚本，这4个staff记录对应：
-- staff001 (王芳) - user_id应该是当前staff001对应的user_id
-- staff002 (李霞) - user_id应该是当前staff002对应的user_id
-- staff003 (张梅) - user_id应该是当前staff003对应的user_id
-- staff004 (刘美) - user_id应该是当前staff004对应的user_id

-- 注意：需要先获取这些user的实际user_id
SET @staff001_user_id = (SELECT user_id FROM user WHERE username = 'staff001');
SET @staff002_user_id = (SELECT user_id FROM user WHERE username = 'staff002');
SET @staff003_user_id = (SELECT user_id FROM user WHERE username = 'staff003');
SET @staff004_user_id = (SELECT user_id FROM user WHERE username = 'staff004');

SELECT '准备恢复的user_id' AS info;
SELECT @staff001_user_id AS staff001_user_id,
       @staff002_user_id AS staff002_user_id,
       @staff003_user_id AS staff003_user_id,
       @staff004_user_id AS staff004_user_id;

-- 检查这些user_id是否已经有staff记录
SELECT '检查这些user_id是否已有staff记录' AS info;
SELECT s.staff_id, s.user_id, s.name, u.username
FROM user u
LEFT JOIN staff s ON u.user_id = s.user_id
WHERE u.username IN ('staff001', 'staff002', 'staff003', 'staff004')
ORDER BY u.user_id;

-- ============================================
-- 步骤4：插入缺失的staff记录
-- ============================================
-- 根据原始脚本，这4个记录是：
-- 1. 王芳 - 日常保洁 - user_id对应staff001
-- 2. 李霞 - 日常保洁 - user_id对应staff002
-- 3. 张梅 - 日常保洁 - user_id对应staff003
-- 4. 刘美 - 日常保洁 - user_id对应staff004

-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 插入缺失的staff记录
-- 注意：staff_id暂时不指定，让数据库自动生成，稍后会重新编号
INSERT INTO `staff` (`user_id`, `name`, `gender`, `birth_date`, `origin`, `id_card`, `avatar`, `resume`, `work_experience`, `hourly_rate`, `rating`, `verification_status`) 
SELECT 
    u.user_id,
    CASE u.username
        WHEN 'staff001' THEN '王芳'
        WHEN 'staff002' THEN '李霞'
        WHEN 'staff003' THEN '张梅'
        WHEN 'staff004' THEN '刘美'
    END AS name,
    CASE u.username
        WHEN 'staff001' THEN 0
        WHEN 'staff002' THEN 0
        WHEN 'staff003' THEN 0
        WHEN 'staff004' THEN 0
    END AS gender,
    CASE u.username
        WHEN 'staff001' THEN '1985-05-15'
        WHEN 'staff002' THEN '1990-03-22'
        WHEN 'staff003' THEN '1988-07-10'
        WHEN 'staff004' THEN '1992-11-05'
    END AS birth_date,
    CASE u.username
        WHEN 'staff001' THEN '河南'
        WHEN 'staff002' THEN '山东'
        WHEN 'staff003' THEN '河北'
        WHEN 'staff004' THEN '安徽'
    END AS origin,
    CASE u.username
        WHEN 'staff001' THEN '410105198505150001'
        WHEN 'staff002' THEN '370102199003220002'
        WHEN 'staff003' THEN '130102198807100003'
        WHEN 'staff004' THEN '340102199211050004'
    END AS id_card,
    NULL AS avatar,
    CASE u.username
        WHEN 'staff001' THEN '从事家政服务10年，经验丰富，擅长日常保洁和深度清洁，工作认真负责，深受客户好评。'
        WHEN 'staff002' THEN '从事家政服务8年，擅长日常保洁、整理收纳，服务态度好，细致耐心。'
        WHEN 'staff003' THEN '从事家政服务9年，擅长日常保洁、家具保养，工作认真，客户满意度高。'
        WHEN 'staff004' THEN '从事家政服务6年，擅长日常保洁、窗户清洁，年轻有活力，服务态度好。'
    END AS resume,
    CASE u.username
        WHEN 'staff001' THEN 10
        WHEN 'staff002' THEN 8
        WHEN 'staff003' THEN 9
        WHEN 'staff004' THEN 6
    END AS work_experience,
    CASE u.username
        WHEN 'staff001' THEN 55.00
        WHEN 'staff002' THEN 52.00
        WHEN 'staff003' THEN 53.00
        WHEN 'staff004' THEN 50.00
    END AS hourly_rate,
    CASE u.username
        WHEN 'staff001' THEN 4.8
        WHEN 'staff002' THEN 4.7
        WHEN 'staff003' THEN 4.6
        WHEN 'staff004' THEN 4.5
    END AS rating,
    1 AS verification_status
FROM user u
WHERE u.username IN ('staff001', 'staff002', 'staff003', 'staff004')
  AND NOT EXISTS (
      SELECT 1 FROM staff s WHERE s.user_id = u.user_id
  );

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 步骤5：验证恢复结果
-- ============================================
SELECT '恢复后的staff记录数' AS info, COUNT(*) AS count FROM staff;

SELECT '恢复后的前4个staff记录' AS info;
SELECT s.staff_id, s.user_id, s.name, u.username
FROM staff s
INNER JOIN user u ON s.user_id = u.user_id
WHERE u.username IN ('staff001', 'staff002', 'staff003', 'staff004')
ORDER BY u.user_id;

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
SELECT '恢复完成！' AS info;
SELECT '如果现在有50个staff记录，可以执行 fix_staff_ids.sql 来重新编号' AS next_step;

