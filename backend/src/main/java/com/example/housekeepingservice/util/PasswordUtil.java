package com.example.housekeepingservice.util;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * BCrypt密码加密/验证工具（适配Java 17，无需Spring Security）
 */
@Component
public class PasswordUtil {

    /**
     * 密码加密（注册时用：明文→加密字符串）
     * 例：明文123456 → 加密后$2a$10$xxxxxxx（不可逆，只能验证不能解密）
     */
    public String encrypt(String rawPassword) {
        // 10是工作因子，越大越安全，毕设用10足够
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }

    /**
     * 密码验证（登录时用：明文密码 vs 数据库加密密码）
     */
    public boolean verify(String rawPassword, String encryptPassword) {
        return BCrypt.checkpw(rawPassword, encryptPassword);
    }
}