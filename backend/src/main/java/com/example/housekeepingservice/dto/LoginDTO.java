package com.example.housekeepingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginDTO {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    // 补充getPhone()方法（解决第50行报错）
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // 补充getPassword()方法（避免后续报错）
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}