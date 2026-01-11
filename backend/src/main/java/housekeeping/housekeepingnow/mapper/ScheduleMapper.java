package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 排班Mapper
 */
@Mapper
public interface ScheduleMapper {

    /**
     * 根据ID查询
     */
    Schedule selectById(@Param("scheduleId") Long scheduleId);

    /**
     * 排班列表查询
     */
    List<Schedule> selectList(@Param("staffId") Long staffId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("availableStatus") Integer availableStatus);

    /**
     * 时间冲突校验
     */
    List<Schedule> checkTimeConflict(@Param("staffId") Long staffId,
                                     @Param("workDate") LocalDate workDate,
                                     @Param("startTime") LocalTime startTime,
                                     @Param("endTime") LocalTime endTime,
                                     @Param("excludeScheduleId") Long excludeScheduleId);

    /**
     * 插入
     */
    int insert(Schedule schedule);

    /**
     * 更新
     */
    int update(Schedule schedule);

    /**
     * 删除
     */
    int deleteById(@Param("scheduleId") Long scheduleId);
}


