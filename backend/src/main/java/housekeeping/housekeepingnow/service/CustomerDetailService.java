package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.CustomerDetail;

/**
 * 顾客详细信息服务接口
 */
public interface CustomerDetailService {

    /**
     * 根据顾客ID查询
     */
    CustomerDetail getById(Long customerId);

    /**
     * 根据用户ID查询
     */
    CustomerDetail getByUserId(Long userId);

    /**
     * 根据用户ID查询，如果不存在则自动创建
     * 
     * @param userId 用户ID
     * @return 顾客详细信息（如果不存在则创建新记录）
     */
    CustomerDetail getOrCreateByUserId(Long userId);

    /**
     * 创建顾客详细信息
     */
    CustomerDetail createCustomerDetail(CustomerDetail customerDetail);

    /**
     * 更新顾客详细信息
     */
    CustomerDetail updateCustomerDetail(CustomerDetail customerDetail);
}

