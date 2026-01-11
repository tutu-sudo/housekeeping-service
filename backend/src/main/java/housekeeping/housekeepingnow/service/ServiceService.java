package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.Service;
import housekeeping.housekeepingnow.entity.ServiceType;
import housekeeping.housekeepingnow.entity.Staff;

import java.util.List;

/**
 * 服务查询服务接口
 */
public interface ServiceService {
    
    /**
     * 查询所有服务类型
     */
    List<ServiceType> getAllServiceTypes();
    
    /**
     * 根据服务类型ID查询服务列表
     */
    List<Service> getServicesByTypeId(Long serviceTypeId);
    
    /**
     * 查询所有可用服务
     */
    List<Service> getAvailableServices();
    
    /**
     * 根据服务ID查询服务详情
     */
    Service getServiceById(Long serviceId);
    
    /**
     * 服务人员筛选查询
     */
    List<Staff> searchStaff(Integer gender, String origin, java.math.BigDecimal minRating, 
                            Integer minWorkExperience, Long serviceId);
    
    /**
     * 根据服务ID查询可提供服务的人员
     */
    List<Staff> getStaffByServiceId(Long serviceId);

    /**
     * 后台-查询服务类型列表
     */
    List<ServiceType> listServiceTypes();

    /**
     * 后台-新增服务类型
     */
    ServiceType createServiceType(ServiceType serviceType);

    /**
     * 后台-更新服务类型
     */
    ServiceType updateServiceType(ServiceType serviceType);

    /**
     * 后台-删除服务类型
     */
    void deleteServiceType(Long serviceTypeId);

    /**
     * 后台-查询服务列表（可按类型/上下架筛选）
     */
    List<Service> listServices(Long serviceTypeId, Integer availableStatus);

    /**
     * 后台-新增服务
     */
    Service createService(Service service);

    /**
     * 后台-更新服务
     */
    Service updateService(Service service);

    /**
     * 后台-删除服务
     */
    void deleteService(Long serviceId);

    /**
     * 后台-上下架服务
     */
    void changeServiceAvailability(Long serviceId, Integer availableStatus);
}

