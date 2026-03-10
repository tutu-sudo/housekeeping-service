package com.example.housekeepingservice.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 服务人员实体类（对应service_staff01表）
 */
@Data
public class ServiceStaff01 {
    private Integer id;          // 主键ID
    private String name;         // 真实姓名
    private Integer gender;      // 性别
    private String idCard;       // 身份证号（唯一）
    private String phone;        // 手机号（登录账号）
    private String password;     // 加密密码
    private Integer workExperience; // 工作年限
    private BigDecimal hourlyWage; // 时薪
    private Integer status;      // 状态：1-可用，0-禁用
    private Date createTime;     // 注册时间
    private Date updateTime;     // 更新时间
}