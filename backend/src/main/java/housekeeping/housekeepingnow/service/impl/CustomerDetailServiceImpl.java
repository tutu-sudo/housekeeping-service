package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.CustomerDetail;
import housekeeping.housekeepingnow.entity.User;
import housekeeping.housekeepingnow.mapper.CustomerDetailMapper;
import housekeeping.housekeepingnow.mapper.UserMapper;
import housekeeping.housekeepingnow.service.CustomerDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 顾客详细信息服务实现类
 */
@Slf4j
@Service
public class CustomerDetailServiceImpl implements CustomerDetailService {

    @Autowired
    private CustomerDetailMapper customerDetailMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public CustomerDetail getById(Long customerId) {
        CustomerDetail customerDetail = customerDetailMapper.selectById(customerId);
        if (customerDetail == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "顾客信息不存在");
        }
        return customerDetail;
    }

    @Override
    public CustomerDetail getByUserId(Long userId) {
        return customerDetailMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public CustomerDetail getOrCreateByUserId(Long userId) {
        // 先查询是否存在
        CustomerDetail customerDetail = customerDetailMapper.selectByUserId(userId);
        if (customerDetail != null) {
            log.info("找到已存在的顾客信息，customerId={}, userId={}", customerDetail.getCustomerId(), userId);
            return customerDetail;
        }

        // 不存在则创建
        log.info("顾客信息不存在，开始创建，userId={}", userId);
        
        // 查询用户信息，获取基本信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在，无法创建顾客信息");
        }

        // 验证用户类型是否为顾客（user_type=1）
        if (user.getUserType() != null && user.getUserType() != 1) {
            throw new BusinessException(ResultCode.OPERATION_NOT_ALLOWED, 
                    "该用户不是顾客类型（user_type=1），无法创建顾客信息");
        }

        // 创建新的顾客详细信息
        customerDetail = new CustomerDetail();
        customerDetail.setUserId(userId);
        customerDetail.setName(user.getUsername()); // 默认使用用户名作为姓名
        customerDetail.setPoints(0); // 默认积分为0
        customerDetail.setCreateTime(LocalDateTime.now());
        customerDetail.setUpdateTime(LocalDateTime.now());

        // 插入数据库
        customerDetailMapper.insert(customerDetail);
        
        log.info("成功创建顾客信息，customerId={}, userId={}, name={}", 
                customerDetail.getCustomerId(), userId, customerDetail.getName());
        
        return customerDetail;
    }

    @Override
    @Transactional
    public CustomerDetail createCustomerDetail(CustomerDetail customerDetail) {
        // 验证userId是否存在
        User user = userMapper.selectById(customerDetail.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查是否已存在该userId的customer_detail记录
        CustomerDetail existing = customerDetailMapper.selectByUserId(customerDetail.getUserId());
        if (existing != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "该用户已存在顾客信息");
        }

        if (customerDetail.getCreateTime() == null) {
            customerDetail.setCreateTime(LocalDateTime.now());
        }
        if (customerDetail.getUpdateTime() == null) {
            customerDetail.setUpdateTime(LocalDateTime.now());
        }
        if (customerDetail.getPoints() == null) {
            customerDetail.setPoints(0);
        }

        customerDetailMapper.insert(customerDetail);
        return customerDetail;
    }

    @Override
    @Transactional
    public CustomerDetail updateCustomerDetail(CustomerDetail customerDetail) {
        CustomerDetail existing = customerDetailMapper.selectById(customerDetail.getCustomerId());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "顾客信息不存在");
        }

        customerDetail.setUpdateTime(LocalDateTime.now());
        customerDetailMapper.update(customerDetail);
        return customerDetailMapper.selectById(customerDetail.getCustomerId());
    }
}

