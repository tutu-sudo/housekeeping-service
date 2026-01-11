package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Service;
import housekeeping.housekeepingnow.entity.ServiceType;
import housekeeping.housekeepingnow.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台-服务管理
 */
@Tag(name = "后台-服务管理接口")
@RestController
@RequestMapping("/api/admin/services")
public class AdminServiceController {

    @Autowired
    private ServiceService serviceService;

    @Operation(summary = "服务类型列表")
    @GetMapping("/types")
    public Result<List<ServiceType>> listServiceTypes() {
        return Result.success(serviceService.listServiceTypes());
    }

    @Operation(summary = "新增服务类型")
    @PostMapping("/types")
    public Result<ServiceType> createServiceType(@RequestBody ServiceType serviceType) {
        return Result.success(serviceService.createServiceType(serviceType));
    }

    @Operation(summary = "更新服务类型")
    @PutMapping("/types/{serviceTypeId}")
    public Result<ServiceType> updateServiceType(@PathVariable Long serviceTypeId,
                                                 @RequestBody ServiceType serviceType) {
        serviceType.setServiceTypeId(serviceTypeId);
        return Result.success(serviceService.updateServiceType(serviceType));
    }

    @Operation(summary = "删除服务类型")
    @DeleteMapping("/types/{serviceTypeId}")
    public Result<Void> deleteServiceType(@PathVariable Long serviceTypeId) {
        serviceService.deleteServiceType(serviceTypeId);
        return Result.success();
    }

    @Operation(summary = "服务列表")
    @GetMapping("/items")
    public Result<List<Service>> listServices(@RequestParam(required = false) Long serviceTypeId,
                                              @RequestParam(required = false) Integer availableStatus) {
        return Result.success(serviceService.listServices(serviceTypeId, availableStatus));
    }

    @Operation(summary = "新增服务")
    @PostMapping("/items")
    public Result<Service> createService(@RequestBody Service service) {
        return Result.success(serviceService.createService(service));
    }

    @Operation(summary = "更新服务")
    @PutMapping("/items/{serviceId}")
    public Result<Service> updateService(@PathVariable Long serviceId,
                                         @RequestBody Service service) {
        service.setServiceId(serviceId);
        return Result.success(serviceService.updateService(service));
    }

    @Operation(summary = "服务上下架")
    @PatchMapping("/items/{serviceId}/status")
    public Result<Void> changeAvailability(@PathVariable Long serviceId,
                                           @RequestParam Integer availableStatus) {
        serviceService.changeServiceAvailability(serviceId, availableStatus);
        return Result.success();
    }

    @Operation(summary = "删除服务")
    @DeleteMapping("/items/{serviceId}")
    public Result<Void> deleteService(@PathVariable Long serviceId) {
        serviceService.deleteService(serviceId);
        return Result.success();
    }
}


