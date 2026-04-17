package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.Company;
import housekeeping.housekeepingnow.mapper.CompanyMapper;
import housekeeping.housekeepingnow.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    @Override
    public Company getById(Long companyId) {
        Company company = companyMapper.selectById(companyId);
        if (company == null) {
            throw new BusinessException(ResultCode.COMPANY_NOT_FOUND);
        }
        return company;
    }

    @Override
    public List<Company> listCompanies() {
        return companyMapper.selectAll();
    }

    @Override
    @Transactional
    public Company createCompany(Company company) {
        companyMapper.insert(company);
        return companyMapper.selectById(company.getCompanyId());
    }

    @Override
    @Transactional
    public Company updateCompany(Company company) {
        Company db = companyMapper.selectById(company.getCompanyId());
        if (db == null) {
            throw new BusinessException(ResultCode.COMPANY_NOT_FOUND);
        }
        companyMapper.update(company);
        return companyMapper.selectById(company.getCompanyId());
    }

    @Override
    @Transactional
    public void deleteCompany(Long companyId) {
        Company db = companyMapper.selectById(companyId);
        if (db == null) {
            throw new BusinessException(ResultCode.COMPANY_NOT_FOUND);
        }
        companyMapper.deleteById(companyId);
    }
}


