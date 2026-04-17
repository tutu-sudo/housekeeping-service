package housekeeping.housekeepingnow.util;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;

/**
 * 生成密码哈希值的测试类
 */
public class PasswordHashGeneratorTest {
    
    @Test
    public void generateStaffPasswordHash() {
        String password = "staff123";
        String hashedPassword = BCrypt.hashpw(password);
        System.out.println("============================================");
        System.out.println("密码: " + password);
        System.out.println("BCrypt 哈希值: " + hashedPassword);
        System.out.println("============================================");
        
        // 验证生成的哈希值是否正确
        boolean matches = BCrypt.checkpw(password, hashedPassword);
        System.out.println("验证结果: " + (matches ? "✓ 正确" : "✗ 错误"));
        System.out.println("============================================");
    }
    
    @Test
    public void generateAdminPasswordHash() {
        String password = "admin123";
        String hashedPassword = BCrypt.hashpw(password);
        System.out.println("============================================");
        System.out.println("密码: " + password);
        System.out.println("BCrypt 哈希值: " + hashedPassword);
        System.out.println("============================================");
        
        // 验证生成的哈希值是否正确
        boolean matches = BCrypt.checkpw(password, hashedPassword);
        System.out.println("验证结果: " + (matches ? "✓ 正确" : "✗ 错误"));
        System.out.println("============================================");
    }
}

