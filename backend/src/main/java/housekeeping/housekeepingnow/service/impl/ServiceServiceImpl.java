package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.Service;
import housekeeping.housekeepingnow.entity.ServiceType;
import housekeeping.housekeepingnow.entity.Staff;
import housekeeping.housekeepingnow.entity.StaffServiceSkill;
import housekeeping.housekeepingnow.mapper.ServiceMapper;
import housekeeping.housekeepingnow.mapper.ServiceTypeMapper;
import housekeeping.housekeepingnow.mapper.StaffMapper;
import housekeeping.housekeepingnow.mapper.StaffServiceSkillMapper;
import housekeeping.housekeepingnow.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务查询服务实现类
 */
@Component
public class ServiceServiceImpl implements ServiceService {
    
    @Autowired
    private ServiceTypeMapper serviceTypeMapper;
    
    @Autowired
    private ServiceMapper serviceMapper;
    
    @Autowired
    private StaffMapper staffMapper;
    
    @Autowired
    private StaffServiceSkillMapper staffServiceSkillMapper;
    
    @Override
    public List<ServiceType> getAllServiceTypes() {
        return serviceTypeMapper.selectAll();
    }
    
    @Override
    public List<Service> getServicesByTypeId(Long serviceTypeId) {
        return serviceMapper.selectByServiceTypeId(serviceTypeId);
    }
    
    @Override
    public List<Service> getAvailableServices() {
        return serviceMapper.selectAvailableServices();
    }
    
    @Override
    public Service getServiceById(Long serviceId) {
        return serviceMapper.selectById(serviceId);
    }
    
    @Override
    public List<Staff> searchStaff(Integer gender, String origin, java.math.BigDecimal minRating, 
                                   Integer minWorkExperience, Long serviceId) {
        // 如果指定了服务ID，先查询能提供该服务的服务人员
        if (serviceId != null) {
            List<StaffServiceSkill> skills = staffServiceSkillMapper.selectByServiceId(serviceId);
            List<Long> staffIds = skills.stream()
                    .map(StaffServiceSkill::getStaffId)
                    .collect(Collectors.toList());
            
            if (staffIds.isEmpty()) {
                return List.of();
            }
            
            // 根据服务人员ID和其他条件筛选
            List<Staff> allStaff = staffMapper.selectList(gender, origin, minRating, minWorkExperience, 1);
            return allStaff.stream()
                    .filter(staff -> staffIds.contains(staff.getStaffId()))
                    .collect(Collectors.toList());
        }
        
        // 只根据基本条件筛选
        return staffMapper.selectList(gender, origin, minRating, minWorkExperience, 1);
    }
    
    @Override
    public List<Staff> getStaffByServiceId(Long serviceId) {
        List<StaffServiceSkill> skills = staffServiceSkillMapper.selectByServiceId(serviceId);
        List<Long> staffIds = skills.stream()
                .map(StaffServiceSkill::getStaffId)
                .collect(Collectors.toList());
        
        if (staffIds.isEmpty()) {
            return List.of();
        }
        
        // 查询服务人员详细信息
        return staffIds.stream()
                .map(staffMapper::selectById)
                .filter(staff -> staff != null && staff.getVerificationStatus() == 1)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceType> listServiceTypes() {
        return serviceTypeMapper.selectAll();
    }

    @Override
    public ServiceType createServiceType(ServiceType serviceType) {
        serviceTypeMapper.insert(serviceType);
        return serviceTypeMapper.selectById(serviceType.getServiceTypeId());
    }

    @Override
    public ServiceType updateServiceType(ServiceType serviceType) {
        ServiceType db = serviceTypeMapper.selectById(serviceType.getServiceTypeId());
        if (db == null) {
            throw new BusinessException(ResultCode.SERVICE_TYPE_NOT_FOUND);
        }
        serviceTypeMapper.update(serviceType);
        return serviceTypeMapper.selectById(serviceType.getServiceTypeId());
    }

    @Override
    public void deleteServiceType(Long serviceTypeId) {
        ServiceType db = serviceTypeMapper.selectById(serviceTypeId);
        if (db == null) {
            throw new BusinessException(ResultCode.SERVICE_TYPE_NOT_FOUND);
        }
        serviceTypeMapper.deleteById(serviceTypeId);
    }

    @Override
    public List<Service> listServices(Long serviceTypeId, Integer availableStatus) {
        return serviceMapper.selectList(serviceTypeId, availableStatus);
    }

    @Override
    public Service createService(Service service) {
        // 校验服务类型存在
        if (service.getServiceTypeId() == null ||
                serviceTypeMapper.selectById(service.getServiceTypeId()) == null) {
            throw new BusinessException(ResultCode.SERVICE_TYPE_NOT_FOUND);
        }
        // 默认可用状态
        if (service.getAvailableStatus() == null) {
            service.setAvailableStatus(1);
        }
        serviceMapper.insert(service);
        return serviceMapper.selectById(service.getServiceId());
    }

    @Override
    public Service updateService(Service service) {
        Service db = serviceMapper.selectById(service.getServiceId());
        if (db == null) {
            throw new BusinessException(ResultCode.SERVICE_NOT_FOUND);
        }
        if (service.getServiceTypeId() != null &&
                serviceTypeMapper.selectById(service.getServiceTypeId()) == null) {
            throw new BusinessException(ResultCode.SERVICE_TYPE_NOT_FOUND);
        }
        serviceMapper.update(service);
        return serviceMapper.selectById(service.getServiceId());
    }

    @Override
    public void deleteService(Long serviceId) {
        Service db = serviceMapper.selectById(serviceId);
        if (db == null) {
            throw new BusinessException(ResultCode.SERVICE_NOT_FOUND);
        }
        serviceMapper.deleteById(serviceId);
    }

    @Override
    public void changeServiceAvailability(Long serviceId, Integer availableStatus) {
        Service db = serviceMapper.selectById(serviceId);
        if (db == null) {
            throw new BusinessException(ResultCode.SERVICE_NOT_FOUND);
        }
        Service update = new Service();
        update.setServiceId(serviceId);
        update.setAvailableStatus(availableStatus);
        serviceMapper.update(update);
    }
}

