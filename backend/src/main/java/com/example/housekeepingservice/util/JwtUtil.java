package com.example.housekeepingservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 适配Java 17的JWT工具类（jjwt 0.11.5）
 */
@Component
public class JwtUtil {
    // 密钥：至少32个字符（256位），毕设随便写，足够用
    private final String secretKeyStr = "housekeeping2024biye1234567890abcdef";
    // Token过期时间：24小时（毫秒）
    private final long expireTime = 24 * 60 * 60 * 1000L;

    /**
     * 生成加密密钥（适配新API）
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKeyStr.getBytes());
    }

    /**
     * 创建Token
     * @param userId 用户ID
     * @param role 用户角色（如CUSTOMER）
     * @return JWT Token字符串
     */
    public String createToken(Integer userId, String role) {
        // 构建Token的载荷（自定义数据）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        // 生成Token
        return Jwts.builder()
                .setClaims(claims) // 设置载荷
                .setExpiration(new Date(System.currentTimeMillis() + expireTime)) // 设置过期时间
                .signWith(getSecretKey()) // 签名（新API方式，无jaxb依赖）
                .compact();
    }

    /**
     * 解析Token，获取载荷信息
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey()) // 设置签名密钥
                    .build()
                    .parseClaimsJws(token) // 解析Token
                    .getBody();
        } catch (Exception e) {
            // 解析失败（Token过期/篡改）返回null
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean isTokenValid(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        // 检查是否过期
        return !claims.getExpiration().before(new Date());
    }
}