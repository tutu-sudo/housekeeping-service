package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器（用于验证配置）
 * 
 * @author housekeeping
 */
@Tag(name = "测试接口", description = "用于测试系统配置的接口")
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Operation(summary = "健康检查", description = "检查系统是否正常运行")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("系统运行正常");
    }
}

