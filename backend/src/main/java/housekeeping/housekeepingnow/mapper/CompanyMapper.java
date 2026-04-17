package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.Company;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公司信息Mapper
 */
@Mapper
public interface CompanyMapper {

    /**
     * 根据ID查询
     */
    Company selectById(@Param("companyId") Long companyId);

    /**
     * 查询所有公司
     */
    List<Company> selectAll();

    /**
     * 插入公司
     */
    int insert(Company company);

    /**
     * 更新公司
     */
    int update(Company company);

    /**
     * 删除公司
     */
    int deleteById(@Param("companyId") Long companyId);
}


