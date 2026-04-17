package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Company;
import housekeeping.housekeepingnow.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台-公司信息管理
 */
@Tag(name = "后台-公司信息接口")
@RestController
@RequestMapping("/api/admin/companies")
public class AdminCompanyController {

    @Autowired
    private CompanyService companyService;

    @Operation(summary = "公司详情")
    @GetMapping("/{companyId}")
    public Result<Company> getById(@PathVariable Long companyId) {
        return Result.success(companyService.getById(companyId));
    }

    @Operation(summary = "公司列表")
    @GetMapping
    public Result<List<Company>> list() {
        return Result.success(companyService.listCompanies());
    }

    @Operation(summary = "新增公司")
    @PostMapping
    public Result<Company> create(@RequestBody Company company) {
        return Result.success(companyService.createCompany(company));
    }

    @Operation(summary = "更新公司")
    @PutMapping("/{companyId}")
    public Result<Company> update(@PathVariable Long companyId,
                                  @RequestBody Company company) {
        company.setCompanyId(companyId);
        return Result.success(companyService.updateCompany(company));
    }

    @Operation(summary = "删除公司")
    @DeleteMapping("/{companyId}")
    public Result<Void> delete(@PathVariable Long companyId) {
        companyService.deleteCompany(companyId);
        return Result.success();
    }
}


