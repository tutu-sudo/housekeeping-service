package com.example.housekeepingservice.service;

import com.example.housekeepingservice.common.Result;
import com.example.housekeepingservice.dto.LoginDTO;
import com.example.housekeepingservice.dto.RegisterDTO;

public interface AuthService {
    // 注册接口返回Result
    Result register(RegisterDTO registerDTO);
    // 登录接口返回Result
    Result login(LoginDTO loginDTO);
}