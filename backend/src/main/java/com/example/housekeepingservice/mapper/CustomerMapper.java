package com.example.housekeepingservice.mapper;

import com.example.housekeepingservice.entity.Customer01;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 顾客数据库操作接口（MyBatis自动实现，不用写实现类）
 */
@Mapper  // 必须加这个注解，MyBatis才能扫描到
public interface CustomerMapper {
    /**
     * 注册顾客：插入数据到customer01表
     */
    int insertCustomer(Customer01 customer01);

    /**
     * 根据手机号查询顾客（登录/查重用）
     */
    Customer01 getByPhone(@Param("phone") String phone);
}