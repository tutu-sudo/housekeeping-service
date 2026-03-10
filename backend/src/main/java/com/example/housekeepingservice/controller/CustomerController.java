package com.example.housekeepingservice.controller;

import com.example.housekeepingservice.common.Result;
import com.example.housekeepingservice.entity.Customer01; // 适配01后缀
import com.example.housekeepingservice.service.CustomerService01; // 适配01后缀
import com.example.housekeepingservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomerService01 customerService01; // 适配01后缀的服务类

    @GetMapping("/info")
    public Result getCurrentCustomerInfo(HttpServletRequest request) {
        // 1. 获取并解析Token
        String token = request.getHeader("Token");
        Claims claims = jwtUtil.parseToken(token);
        Integer userId = (Integer) claims.get("userId");

        // 2. 模拟返回用户信息（用Customer01实体类）
        Customer01 customer01 = new Customer01();
        customer01.setId(userId); // 对应Customer01的setId方法
        customer01.setUsername("test001");
        customer01.setPhone("13800138000");
        customer01.setName("测试用户");
        customer01.setGender(1);

        return Result.success(customer01);
    }
}