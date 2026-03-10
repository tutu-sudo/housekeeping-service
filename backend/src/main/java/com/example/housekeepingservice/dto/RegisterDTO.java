package com.example.housekeepingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "真实姓名不能为空")
    private String name;

    private Integer gender = 0;

    // 手动生成getPhone()
    public String getPhone() {
        return phone;
    }

    // 手动生成其他字段的getter（按需补充）
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Integer getGender() {
        return gender;
    }
}