-- ============================================
-- 快速检查关键数据
-- ============================================

USE `housekeeping_service`;

-- 1. user表中staff用户总数
SELECT 'user表中staff用户总数' AS 检查项, COUNT(*) AS 数量
FROM user WHERE user_type = 2;

-- 2. staff表中记录总数
SELECT 'staff表中记录总数' AS 检查项, COUNT(*) AS 数量
FROM staff;

-- 3. user表中staff001-staff050的数量
SELECT 'user表中staff001-staff050数量' AS 检查项, COUNT(*) AS 数量
FROM user 
WHERE username LIKE 'staff%' 
  AND username REGEXP '^staff[0-9]{3}$'
  AND user_type = 2;

-- 4. staff_id的范围
SELECT 'staff_id范围' AS 检查项, 
       MIN(staff_id) AS 最小ID, 
       MAX(staff_id) AS 最大ID, 
       COUNT(*) AS 总数
FROM staff;

-- 5. user_id的范围（staff用户）
SELECT 'user_id范围（staff用户）' AS 检查项,
       MIN(user_id) AS 最小ID,
       MAX(user_id) AS 最大ID,
       COUNT(*) AS 总数
FROM user WHERE user_type = 2;

-- 6. 是否有缺失的staff记录
SELECT '缺少staff记录的user数量' AS 检查项, COUNT(*) AS 数量
FROM user u
LEFT JOIN staff s ON u.user_id = s.user_id
WHERE u.user_type = 2 AND s.staff_id IS NULL;

-- 7. 前10个staff用户的对应关系
SELECT '前10个staff用户对应关系' AS 检查项;
SELECT u.user_id, u.username, s.staff_id, s.name
FROM user u
LEFT JOIN staff s ON u.user_id = s.user_id
WHERE u.user_type = 2
ORDER BY u.user_id
LIMIT 10;

