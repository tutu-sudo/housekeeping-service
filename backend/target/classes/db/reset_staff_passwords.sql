-- ============================================
-- 重置家政服务人员密码脚本
-- ============================================
-- 用途：重置 staff001 到 staff050 共50个账号的密码
-- 统一密码：staff123
-- BCrypt 哈希值：$2a$10$jy2HP2QTn8NrN7YErFiLgeZNaIig8lROl.qr59vRDEA76.smE0CEi
-- 
-- 执行前请备份数据库！
-- ============================================

USE `housekeeping_service`;

-- ============================================
-- 步骤1：检查当前数据状态
-- ============================================
-- 查看需要重置密码的账号
SELECT '需要重置密码的账号列表' AS info;
SELECT user_id, username, user_type, status 
FROM `user` 
WHERE username LIKE 'staff%' 
  AND username REGEXP '^staff[0-9]{3}$'
  AND user_type = 2
ORDER BY username;

-- 统计需要重置的账号数量
SELECT '需要重置密码的账号数量' AS info;
SELECT COUNT(*) as total_count
FROM `user` 
WHERE username LIKE 'staff%' 
  AND username REGEXP '^staff[0-9]{3}$'
  AND user_type = 2;

-- ============================================
-- 步骤2：重置密码
-- ============================================
-- 更新 staff001 到 staff050 的密码
-- 密码：staff123
-- BCrypt 哈希值：$2a$10$jy2HP2QTn8NrN7YErFiLgeZNaIig8lROl.qr59vRDEA76.smE0CEi
UPDATE `user` 
SET `password` = '$2a$10$jy2HP2QTn8NrN7YErFiLgeZNaIig8lROl.qr59vRDEA76.smE0CEi',
    `update_time` = CURRENT_TIMESTAMP
WHERE username LIKE 'staff%' 
  AND username REGEXP '^staff[0-9]{3}$'
  AND user_type = 2
  AND CAST(SUBSTRING(username, 6) AS UNSIGNED) BETWEEN 1 AND 50;

-- ============================================
-- 步骤3：验证更新结果
-- ============================================
-- 查看已更新的账号
SELECT '已更新密码的账号列表' AS info;
SELECT user_id, username, 
       CASE 
         WHEN password = '$2a$10$jy2HP2QTn8NrN7YErFiLgeZNaIig8lROl.qr59vRDEA76.smE0CEi' 
         THEN '✓ 已更新' 
         ELSE '✗ 未更新' 
       END AS update_status,
       user_type, status, update_time
FROM `user` 
WHERE username LIKE 'staff%' 
  AND username REGEXP '^staff[0-9]{3}$'
  AND user_type = 2
ORDER BY username;

-- 统计已更新的账号数量
SELECT '已更新密码的账号数量' AS info;
SELECT COUNT(*) as updated_count
FROM `user` 
WHERE username LIKE 'staff%' 
  AND username REGEXP '^staff[0-9]{3}$'
  AND user_type = 2
  AND password = '$2a$10$jy2HP2QTn8NrN7YErFiLgeZNaIig8lROl.qr59vRDEA76.smE0CEi';

-- ============================================
-- 步骤4：详细验证（可选）
-- ============================================
-- 如果需要验证每个账号，可以取消下面的注释
/*
-- 验证 staff001 到 staff050 是否都已更新
SELECT 
    CASE 
        WHEN COUNT(*) = 50 THEN '✓ 所有50个账号密码已更新'
        ELSE CONCAT('✗ 只更新了 ', COUNT(*), ' 个账号，期望50个')
    END AS verification_result
FROM `user` 
WHERE username LIKE 'staff%' 
  AND username REGEXP '^staff[0-9]{3}$'
  AND user_type = 2
  AND password = '$2a$10$jy2HP2QTn8NrN7YErFiLgeZNaIig8lROl.qr59vRDEA76.smE0CEi'
  AND CAST(SUBSTRING(username, 6) AS UNSIGNED) BETWEEN 1 AND 50;
*/

-- ============================================
-- 完成提示
-- ============================================
SELECT '密码重置完成！' AS info;
SELECT '所有 staff001 到 staff050 账号的密码已重置为：staff123' AS message;
SELECT '请在前端使用新密码进行登录测试' AS reminder;

