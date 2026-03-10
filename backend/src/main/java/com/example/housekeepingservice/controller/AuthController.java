package com.example.housekeepingservice.controller;

import com.example.housekeepingservice.common.Result;
import com.example.housekeepingservice.dto.LoginDTO;
import com.example.housekeepingservice.dto.RegisterDTO;
import com.example.housekeepingservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 注册接口：返回非泛型Result
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterDTO registerDTO) {
        return authService.register(registerDTO);
    }

    // 登录接口：返回非泛型Result
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO);
    }
}