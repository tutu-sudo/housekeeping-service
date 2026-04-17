package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Service;
import housekeeping.housekeepingnow.entity.ServiceType;
import housekeeping.housekeepingnow.entity.Staff;
import housekeeping.housekeepingnow.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务查询控制器
 */
@Slf4j
@Tag(name = "服务查询接口", description = "服务类型浏览、服务查询、服务人员筛选等接口")
@RestController
@RequestMapping("/api/services")
public class ServiceController {
    
    @Autowired
    private ServiceService serviceService;
    
    @Operation(summary = "查询所有服务类型", description = "获取所有服务类型列表")
    @GetMapping("/types")
    public Result<List<ServiceType>> getAllServiceTypes() {
        try {
            log.info("查询所有服务类型");
            List<ServiceType> types = serviceService.getAllServiceTypes();
            log.info("查询到 {} 个服务类型", types != null ? types.size() : 0);
            return Result.success(types);
        } catch (Exception e) {
            log.error("查询服务类型失败", e);
            return Result.fail(500, "获取服务类型列表失败,请稍后重试");
        }
    }
    
    @Operation(summary = "根据服务类型查询服务", description = "根据服务类型ID查询该类型下的所有服务")
    @GetMapping("/type/{serviceTypeId}")
    public Result<List<Service>> getServicesByType(@PathVariable Long serviceTypeId) {
        try {
            log.info("根据服务类型ID查询服务: {}", serviceTypeId);
            List<Service> services = serviceService.getServicesByTypeId(serviceTypeId);
            log.info("查询到 {} 个服务", services != null ? services.size() : 0);
            return Result.success(services);
        } catch (Exception e) {
            log.error("根据服务类型查询服务失败: serviceTypeId={}", serviceTypeId, e);
            return Result.fail(500, "获取服务列表失败,请稍后重试");
        }
    }
    
    @Operation(summary = "查询所有可用服务", description = "查询所有可用状态的服务")
    @GetMapping("/available")
    public Result<List<Service>> getAvailableServices() {
        try {
            log.info("查询所有可用服务");
            List<Service> services = serviceService.getAvailableServices();
            log.info("查询到 {} 个可用服务", services != null ? services.size() : 0);
            if (services == null || services.isEmpty()) {
                log.warn("未查询到可用服务，返回空列表");
            }
            return Result.success(services);
        } catch (Exception e) {
            log.error("查询可用服务失败", e);
            return Result.fail(500, "获取服务列表失败,请稍后重试");
        }
    }
    
    @Operation(summary = "根据大类查询服务", description = "根据服务大类查询该大类下的所有可用服务")
    @GetMapping("/main-category/{mainCategory}")
    public Result<List<Service>> getServicesByMainCategory(@PathVariable String mainCategory) {
        try {
            log.info("根据大类查询服务: {}", mainCategory);
            List<Service> services = serviceService.getServicesByMainCategory(mainCategory);
            log.info("查询到 {} 个服务", services != null ? services.size() : 0);
            return Result.success(services);
        } catch (Exception e) {
            log.error("根据大类查询服务失败: mainCategory={}", mainCategory, e);
            return Result.fail(500, "获取服务列表失败,请稍后重试");
        }
    }
    
    @Operation(summary = "查询服务详情", description = "根据服务ID查询服务详细信息")
    @GetMapping("/{serviceId}")
    public Result<Service> getServiceById(@PathVariable Long serviceId) {
        Service service = serviceService.getServiceById(serviceId);
        return Result.success(service);
    }
    
    @Operation(summary = "服务人员筛选", description = "根据性别、籍贯、评分、服务年限、服务类型等条件筛选服务人员")
    @GetMapping("/staff/search")
    public Result<List<Staff>> searchStaff(
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) java.math.BigDecimal minRating,
            @RequestParam(required = false) Integer minWorkExperience,
            @RequestParam(required = false) Long serviceId) {
        List<Staff> staffList = serviceService.searchStaff(gender, origin, minRating, minWorkExperience, serviceId);
        return Result.success(staffList);
    }
    
    @Operation(summary = "查询可提供服务的人员", description = "根据服务ID查询能提供该服务的服务人员列表")
    @GetMapping("/{serviceId}/staff")
    public Result<List<Staff>> getStaffByServiceId(@PathVariable Long serviceId) {
        List<Staff> staffList = serviceService.getStaffByServiceId(serviceId);
        return Result.success(staffList);
    }
}

