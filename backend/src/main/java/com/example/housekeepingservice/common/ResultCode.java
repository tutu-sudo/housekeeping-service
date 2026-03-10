package com.example.housekeepingservice.common;

/**
 * 状态码枚举（避免硬编码，答辩时易解释）
 */
public enum ResultCode {
    NOT_LOGIN(401, "未登录或Token过期"),
    USER_EXIST(400, "手机号已注册"),
    USER_NOT_FOUND(400, "用户不存在"),
    PASSWORD_ERROR(400, "密码错误"),
    SYSTEM_ERROR(500, "系统异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}