-- ============================================
-- 修复服务人员密码脚本
-- ============================================
-- 功能：更新所有服务人员的密码为正确的BCrypt哈希值
-- 密码：staff123
-- 注意：BCrypt每次生成的哈希值都不同，但验证时都能通过
-- 如果这个哈希值不行，请在Java代码中重新生成：
-- cn.hutool.crypto.digest.BCrypt.hashpw("staff123")
-- ============================================

USE `housekeeping_service`;

-- 方法1：更新所有服务人员（user_type=2）的密码为已知的正确哈希值
-- 这个哈希值是通过运行 PasswordHashGeneratorTest 生成的
UPDATE `user`
SET `password` = '$2a$10$Uh4SPMDn7J6sIzNk4hOusejYSjqzkpeE.VAB/HrKy8mIVaFquqJcy'
WHERE `user_type` = 2;

-- 方法2（备选）：如果上面的哈希值还是不行，可以重新生成
-- 注意：BCrypt每次生成的哈希值都不同，但验证时应该都能通过
-- 请在Java代码中使用以下方法生成新的哈希：
-- String password = "staff123";
-- String hashedPassword = cn.hutool.crypto.digest.BCrypt.hashpw(password);
-- System.out.println("新的哈希值: " + hashedPassword);

-- 验证更新结果
SELECT 
    '更新后的服务人员密码' AS `说明`,
    COUNT(*) AS `数量`,
    MIN(`user_id`) AS `最小user_id`,
    MAX(`user_id`) AS `最大user_id`
FROM `user`
WHERE `user_type` = 2
  AND `password` = '$2a$10$Uh4SPMDn7J6sIzNk4hOusejYSjqzkpeE.VAB/HrKy8mIVaFquqJcy';

-- 显示前5个服务人员的用户名和密码哈希（用于验证）
SELECT 
    `user_id`,
    `username`,
    `email`,
    `password`,
    `user_type`,
    `status`
FROM `user`
WHERE `user_type` = 2
ORDER BY `user_id`
LIMIT 5;

-- ============================================
-- 验证user和staff表的关联关系
-- ============================================

-- 检查是否有user没有对应的staff记录（user_type=2的服务人员）
SELECT 
    u.user_id,
    u.username,
    u.email,
    s.staff_id,
    s.name AS staff_name
FROM `user` u
LEFT JOIN `staff` s ON u.user_id = s.user_id
WHERE u.user_type = 2
  AND s.staff_id IS NULL;

-- 检查是否有staff没有对应的user记录
SELECT 
    s.staff_id,
    s.user_id,
    s.name AS staff_name,
    u.username,
    u.user_type
FROM `staff` s
LEFT JOIN `user` u ON s.user_id = u.user_id
WHERE u.user_id IS NULL;

-- 验证关联关系完整性（应该返回0行）
SELECT 
    '关联关系检查' AS `检查项`,
    COUNT(*) AS `问题数量`
FROM (
    SELECT u.user_id
    FROM `user` u
    LEFT JOIN `staff` s ON u.user_id = s.user_id
    WHERE u.user_type = 2
      AND s.staff_id IS NULL
    
    UNION ALL
    
    SELECT s.staff_id
    FROM `staff` s
    LEFT JOIN `user` u ON s.user_id = u.user_id
    WHERE u.user_id IS NULL
) AS issues;

