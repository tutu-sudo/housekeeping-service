package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.Company;

import java.util.List;

/**
 * 公司信息服务接口
 */
public interface CompanyService {

    /**
     * 查询公司详情
     */
    Company getById(Long companyId);

    /**
     * 公司列表
     */
    List<Company> listCompanies();

    /**
     * 新增公司
     */
    Company createCompany(Company company);

    /**
     * 更新公司
     */
    Company updateCompany(Company company);

    /**
     * 删除公司
     */
    void deleteCompany(Long companyId);
}


