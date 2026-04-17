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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
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
            // 查询服务信息，用于回传serviceName给前端联动
            Service service = serviceMapper.selectById(serviceId);
            String serviceName = service != null ? service.getServiceName() : null;

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
                    .peek(staff -> {
                        // 在筛选结果中回填该人员的主服务信息，供前端自动联动服务项目
                        staff.setServiceId(serviceId);
                        staff.setServiceName(serviceName);
                    })
                    .collect(Collectors.toList());
        }
        
        // 只根据基本条件筛选：此时也需要为每个服务人员补充一个“主服务”，用于前端联动服务项目
        List<Staff> staffList = staffMapper.selectList(gender, origin, minRating, minWorkExperience, 1);
        if (staffList == null || staffList.isEmpty()) {
            return staffList;
        }

        return staffList.stream()
                .peek(staff -> {
                    // 从 staff_service_skill 表中为该人员选取一条主服务记录（简单取第一条）
                    List<StaffServiceSkill> skills = staffServiceSkillMapper.selectByStaffId(staff.getStaffId());
                    if (skills == null || skills.isEmpty()) {
                        return;
                    }
                    StaffServiceSkill mainSkill = skills.get(0);
                    Long mainServiceId = mainSkill.getServiceId();
                    if (mainServiceId == null) {
                        return;
                    }
                    Service mainService = serviceMapper.selectById(mainServiceId);
                    if (mainService == null) {
                        return;
                    }
                    staff.setServiceId(mainService.getServiceId());
                    staff.setServiceName(mainService.getServiceName());
                })
                .collect(Collectors.toList());
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
    @Transactional(rollbackFor = Exception.class)
    public ServiceType createServiceType(ServiceType serviceType) {
        serviceTypeMapper.insert(serviceType);
        createDefaultServiceForType(serviceType);
        return serviceTypeMapper.selectById(serviceType.getServiceTypeId());
    }

    private void createDefaultServiceForType(ServiceType serviceType) {
        Service defaultService = new Service();
        defaultService.setServiceTypeId(serviceType.getServiceTypeId());
        defaultService.setServiceName(serviceType.getTypeName());
        defaultService.setDescription(serviceType.getDescription());
        defaultService.setPrice(serviceType.getBasePrice() != null
                ? serviceType.getBasePrice()
                : BigDecimal.ZERO);
        defaultService.setEstimatedDuration(serviceType.getMinDuration() != null
                ? serviceType.getMinDuration()
                : 1);
        defaultService.setAvailableStatus(1);
        serviceMapper.insert(defaultService);
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
    public List<Service> listServices(Long serviceTypeId, String mainCategory, Integer availableStatus) {
        return serviceMapper.selectList(serviceTypeId, mainCategory, availableStatus);
    }

    @Override
    public List<Service> getServicesByMainCategory(String mainCategory) {
        return serviceMapper.selectByMainCategory(mainCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Service createService(Service service) {
        // 如果没有传服务类型ID，则根据当前服务信息自动创建一个服务类型
        if (service.getServiceTypeId() == null) {
            ServiceType type = new ServiceType();
            type.setTypeName(service.getServiceName());
            type.setDescription(service.getDescription());
            type.setBasePrice(service.getPrice());
            type.setMinDuration(service.getEstimatedDuration());
            type.setImageUrl(service.getImageUrl()); // 同步图片到服务类型
            serviceTypeMapper.insert(type);
            service.setServiceTypeId(type.getServiceTypeId());
        } else {
            // 校验服务类型存在
            ServiceType existingType = serviceTypeMapper.selectById(service.getServiceTypeId());
            if (existingType == null) {
                throw new BusinessException(ResultCode.SERVICE_TYPE_NOT_FOUND);
            }
            // 如果传了图片，同步更新服务类型的图片
            if (service.getImageUrl() != null && !service.getImageUrl().equals(existingType.getImageUrl())) {
                ServiceType typeUpdate = new ServiceType();
                typeUpdate.setServiceTypeId(service.getServiceTypeId());
                typeUpdate.setImageUrl(service.getImageUrl());
                serviceTypeMapper.update(typeUpdate);
            }
        }
        
        // 校验mainCategory必须是六大类之一
        if (service.getMainCategory() != null && !isValidMainCategory(service.getMainCategory())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "服务大类必须是六大类之一");
        }
        
        // 默认可用状态
        if (service.getAvailableStatus() == null) {
            service.setAvailableStatus(1);
        }
        serviceMapper.insert(service);
        return serviceMapper.selectById(service.getServiceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Service updateService(Service service) {
        Service db = serviceMapper.selectById(service.getServiceId());
        if (db == null) {
            throw new BusinessException(ResultCode.SERVICE_NOT_FOUND);
        }
        Long typeId = db.getServiceTypeId();
        if (service.getServiceTypeId() != null &&
                !service.getServiceTypeId().equals(typeId)) {
            // 暂不支持在编辑时更换服务类型，保持一一对应关系
            throw new BusinessException(ResultCode.SERVICE_TYPE_NOT_FOUND);
        }
        
        // 校验mainCategory必须是六大类之一
        if (service.getMainCategory() != null && !isValidMainCategory(service.getMainCategory())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "服务大类必须是六大类之一");
        }
        
        serviceMapper.update(service);
        
        // 同步更新对应的服务类型详情（名称、描述、价格、时长、图片）
        ServiceType typeUpdate = new ServiceType();
        typeUpdate.setServiceTypeId(typeId);
        typeUpdate.setTypeName(service.getServiceName() != null ? service.getServiceName() : db.getServiceName());
        typeUpdate.setDescription(service.getDescription() != null ? service.getDescription() : db.getDescription());
        typeUpdate.setBasePrice(service.getPrice() != null ? service.getPrice() : db.getPrice());
        typeUpdate.setMinDuration(service.getEstimatedDuration() != null ? service.getEstimatedDuration() : db.getEstimatedDuration());
        typeUpdate.setImageUrl(service.getImageUrl() != null ? service.getImageUrl() : db.getImageUrl());
        serviceTypeMapper.update(typeUpdate);
        
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

    /**
     * 验证mainCategory是否是有效的六大类之一
     */
    private boolean isValidMainCategory(String mainCategory) {
        if (mainCategory == null || mainCategory.trim().isEmpty()) {
            return false;
        }
        List<String> validCategories = Arrays.asList(
                "基础家务服务",
                "专业保洁与养护服务",
                "母婴护理服务",
                "养老护理服务",
                "家电维修与维护服务",
                "特色专项服务"
        );
        return validCategories.contains(mainCategory.trim());
    }
}

