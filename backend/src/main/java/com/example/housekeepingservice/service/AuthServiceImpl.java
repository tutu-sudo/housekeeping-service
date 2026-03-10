package com.example.housekeepingservice.service;

import com.example.housekeepingservice.common.Result;
import com.example.housekeepingservice.common.ResultCode;
import com.example.housekeepingservice.dto.LoginDTO;
import com.example.housekeepingservice.dto.RegisterDTO;
import com.example.housekeepingservice.entity.Customer01;
import com.example.housekeepingservice.mapper.CustomerMapper;
import com.example.housekeepingservice.util.JwtUtil;
import com.example.housekeepingservice.util.PasswordUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final CustomerMapper customerMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    // 构造器注入（推荐写法，无需修改）
    @Autowired
    public AuthServiceImpl(CustomerMapper customerMapper, PasswordUtil passwordUtil, JwtUtil jwtUtil) {
        this.customerMapper = customerMapper;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Result register(RegisterDTO registerDTO) {
        // 检查手机号是否已存在
        if (customerMapper.getByPhone(registerDTO.getPhone()) != null) {
            return Result.error(ResultCode.USER_EXIST.getCode(), ResultCode.USER_EXIST.getMessage());
        }
        // 加密密码
        String encryptPwd = passwordUtil.encrypt(registerDTO.getPassword());
        // 复制属性到实体类
        Customer01 customer01 = new Customer01();
        BeanUtils.copyProperties(registerDTO, customer01);
        customer01.setPassword(encryptPwd);
        // 插入数据库
        int rows = customerMapper.insertCustomer(customer01);
        return rows > 0 ? Result.success() : Result.error(ResultCode.SYSTEM_ERROR.getCode(), "注册失败");
    }

    @Override
    public Result login(LoginDTO loginDTO) {
        // 根据手机号查询用户
        Customer01 customer = customerMapper.getByPhone(loginDTO.getPhone());
        if (customer == null) {
            return Result.error(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMessage());
        }
        // 验证密码
        if (!passwordUtil.verify(loginDTO.getPassword(), customer.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR.getCode(), ResultCode.PASSWORD_ERROR.getMessage());
        }
        // 生成JWT令牌
        String token = jwtUtil.createToken(customer.getId(), "CUSTOMER");
        return Result.success(token);
    }
}