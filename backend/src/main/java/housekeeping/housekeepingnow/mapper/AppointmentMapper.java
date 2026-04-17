package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约记录Mapper接口
 */
@Mapper
public interface AppointmentMapper {
    
    /**
     * 根据预约ID查询
     */
    Appointment selectById(@Param("appointmentId") Long appointmentId);
    
    /**
     * 根据顾客ID查询预约列表
     */
    List<Appointment> selectByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * 根据服务人员ID查询预约列表
     */
    List<Appointment> selectByStaffId(@Param("staffId") Long staffId);
    
    /**
     * 检查时间冲突
     */
    List<Appointment> checkTimeConflict(@Param("staffId") Long staffId,
                                        @Param("appointmentDate") LocalDate appointmentDate,
                                        @Param("startTime") java.time.LocalTime startTime,
                                        @Param("endTime") java.time.LocalTime endTime,
                                        @Param("excludeAppointmentId") Long excludeAppointmentId);
    
    /**
     * 插入预约
     */
    int insert(Appointment appointment);
    
    /**
     * 更新预约
     */
    int update(Appointment appointment);
    
    /**
     * 更新预约状态
     */
    int updateStatus(@Param("appointmentId") Long appointmentId, @Param("status") Integer status);

    /**
     * 批量更新预约状态（用于定时任务）
     */
    int batchUpdateStatus(@Param("appointmentIds") List<Long> appointmentIds, @Param("status") Integer status);

    /**
     * 查询需要自动更新为"进行中"的预约（已确认且开始时间已到）
     */
    List<Appointment> selectAppointmentsToStart();

    /**
     * 查询需要自动更新为"已完成"的预约（进行中且结束时间已过）
     */
    List<Appointment> selectAppointmentsToComplete();

    /**
     * 查询10分钟未支付的预约（用于定时删除）
     * 条件：预约状态为待确认（0）或已确认（1），且创建时间超过10分钟，且支付状态为待支付（0）
     */
    List<Appointment> selectUnpaidAppointmentsToDelete();

    /**
     * 删除预约（物理删除）
     */
    int deleteById(@Param("appointmentId") Long appointmentId);

    /**
     * 预约列表（后台查询）
     */
    List<Appointment> selectList(@Param("customerId") Long customerId,
                                 @Param("staffId") Long staffId,
                                 @Param("status") Integer status,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
}

