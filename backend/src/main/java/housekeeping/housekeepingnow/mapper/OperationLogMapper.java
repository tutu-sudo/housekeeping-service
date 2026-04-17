package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper {

    int insert(OperationLog log);
}


