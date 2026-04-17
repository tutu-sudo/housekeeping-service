package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.Appointment;
import housekeeping.housekeepingnow.mapper.AppointmentMapper;
import housekeeping.housekeepingnow.mapper.ServiceMapper;
import housekeeping.housekeepingnow.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约服务实现类
 */
@Slf4j
@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;
    
    @Autowired
    private ServiceMapper serviceMapper;
    
    @Autowired
    private housekeeping.housekeepingnow.service.PaymentService paymentService;

    @Override
    public Appointment getById(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
        }
        return appointment;
    }

    @Override
    public List<Appointment> listByCustomerId(Long customerId) {
        return appointmentMapper.selectByCustomerId(customerId);
    }

    @Override
    public List<Appointment> listByStaffId(Long staffId) {
        return appointmentMapper.selectByStaffId(staffId);
    }

    @Override
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        // 兼容字段：如果前端传的是contactPhone，则回填到customerPhone
        normalizeContactFields(appointment);

        // 验证customer_id是否存在（外键约束检查）
        if (appointment.getCustomerId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "顾客ID不能为空");
        }

        // 基本时间校验
        validateAppointmentTime(appointment);
        
        // 计算时长和天数
        calculateDurationAndDays(appointment);
        
        // 计算总金额
        calculateTotalAmount(appointment);
        
        // 时间冲突校验
        if (hasTimeConflict(appointment)) {
            throw new BusinessException(ResultCode.APPOINTMENT_TIME_CONFLICT);
        }

        // 默认状态：0-待确认
        if (appointment.getStatus() == null) {
            appointment.setStatus(0);
        }
        
        // 默认结算方式：hourly
        if (appointment.getBillingType() == null || appointment.getBillingType().isEmpty()) {
            appointment.setBillingType("hourly");
        }

        appointmentMapper.insert(appointment);
        return appointment;
    }

    @Override
    @Transactional
    public Appointment updateAppointment(Appointment appointment) {
        Appointment dbAppointment = appointmentMapper.selectById(appointment.getAppointmentId());
        if (dbAppointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
        }

        // 兼容字段：如果前端传的是contactPhone，则回填到customerPhone
        normalizeContactFields(appointment);

        // 只有待确认或已确认的预约允许修改
        if (dbAppointment.getStatus() != 0 && dbAppointment.getStatus() != 1) {
            throw new BusinessException(ResultCode.APPOINTMENT_CANNOT_MODIFY);
        }

        // 合并更新字段：如果新值不为空则使用新值，否则保留原值
        if (appointment.getAppointmentDate() == null) {
            appointment.setAppointmentDate(dbAppointment.getAppointmentDate());
        }
        if (appointment.getStartTime() == null) {
            appointment.setStartTime(dbAppointment.getStartTime());
        }
        if (appointment.getEndTime() == null) {
            appointment.setEndTime(dbAppointment.getEndTime());
        }
        if (appointment.getEndDatetime() == null) {
            appointment.setEndDatetime(dbAppointment.getEndDatetime());
        }
        if (appointment.getStaffId() == null) {
            appointment.setStaffId(dbAppointment.getStaffId());
        }
        if (appointment.getBillingType() == null || appointment.getBillingType().isEmpty()) {
            appointment.setBillingType(dbAppointment.getBillingType() != null ? 
                dbAppointment.getBillingType() : "hourly");
        }

        // 基本时间校验
        validateAppointmentTime(appointment);
        
        // 计算时长和天数
        calculateDurationAndDays(appointment);
        
        // 计算总金额
        calculateTotalAmount(appointment);

        // 时间冲突校验
        if (hasTimeConflict(appointment)) {
            throw new BusinessException(ResultCode.APPOINTMENT_TIME_CONFLICT);
        }

        appointmentMapper.update(appointment);
        return appointmentMapper.selectById(dbAppointment.getAppointmentId());
    }

    /**
     * 兼容处理：customerPhone / contactPhone 二选一
     */
    private void normalizeContactFields(Appointment appointment) {
        if (appointment == null) {
            return;
        }
        if ((appointment.getCustomerPhone() == null || appointment.getCustomerPhone().isBlank())
                && appointment.getContactPhone() != null && !appointment.getContactPhone().isBlank()) {
            appointment.setCustomerPhone(appointment.getContactPhone());
        }
        // 同步contactPhone，确保返回给前端时两者一致（便于兼容老前端）
        if (appointment.getCustomerPhone() != null && !appointment.getCustomerPhone().isBlank()) {
            appointment.setContactPhone(appointment.getCustomerPhone());
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long appointmentId, Integer status) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
        }

        // 简单的状态流转控制：部分非法状态变更在此限制
        if (status == 4 && (appointment.getStatus() == 3 || appointment.getStatus() == 4 || appointment.getStatus() == 5)) {
            // 已完成、已取消或已关闭的订单不能再次取消
            throw new BusinessException(ResultCode.APPOINTMENT_CANNOT_CANCEL);
        }

        // 使用update方法，根据状态设置接单时间/服务开始/结束时间等（目前只记录接单时间）
        Appointment update = new Appointment();
        update.setAppointmentId(appointmentId);
        update.setStatus(status);
        // 从 0 -> 1：服务人员接单，记录接单时间
        if (status == 1 && appointment.getStatus() == 0 && appointment.getAcceptTime() == null) {
            update.setAcceptTime(LocalDateTime.now());
        }
        appointmentMapper.update(update);
    }

    @Override
    @Transactional
    public void markPaid(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
        }
        if (appointment.getPaidTime() != null) {
            return; // 已记录过支付时间，避免重复写入
        }
        Appointment update = new Appointment();
        update.setAppointmentId(appointmentId);
        update.setPaidTime(LocalDateTime.now());
        appointmentMapper.update(update);
    }

    @Override
    public void validatePayable(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
        }
        // 仅待付款状态允许支付
        if (appointment.getStatus() == null || appointment.getStatus() != 1) {
            throw new BusinessException(ResultCode.APPOINTMENT_STATUS_ERROR, "当前状态不允许支付");
        }
        // 必须已经接单，且有接单时间
        if (appointment.getAcceptTime() == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_STATUS_ERROR, "订单尚未接单，无法支付");
        }
        // 10 分钟支付窗口校验
        LocalDateTime deadline = appointment.getAcceptTime().plusMinutes(10);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(deadline)) {
            // 超时：将状态置为已关闭（5），并抛出支付超时错误
            Appointment update = new Appointment();
            update.setAppointmentId(appointmentId);
            update.setStatus(5); // 已拒单/已关闭
            appointmentMapper.update(update);
            throw new BusinessException(ResultCode.APPOINTMENT_TIME_INVALID, "支付超时，订单已关闭");
        }
    }

    @Override
    public boolean hasTimeConflict(Long staffId, LocalDate appointmentDate,
                                   LocalTime startTime, LocalTime endTime, Long excludeAppointmentId) {
        List<Appointment> conflicts = appointmentMapper.checkTimeConflict(
                staffId, appointmentDate, startTime, endTime, excludeAppointmentId);
        return conflicts != null && !conflicts.isEmpty();
    }
    
    /**
     * 检查预约时间冲突（支持跨天预约）
     */
    private boolean hasTimeConflict(Appointment appointment) {
        return hasTimeConflict(
                appointment.getStaffId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getAppointmentId()
        );
    }
    
    /**
     * 验证预约时间
     */
    private void validateAppointmentTime(Appointment appointment) {
        if (appointment.getAppointmentDate() == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_TIME_INVALID);
        }
        
        if (appointment.getStartTime() == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_TIME_INVALID);
        }
        
        // 如果有endDatetime，优先使用endDatetime进行跨天校验
        if (appointment.getEndDatetime() != null) {
            LocalDateTime startDateTime = LocalDateTime.of(
                    appointment.getAppointmentDate(), 
                    appointment.getStartTime()
            );
            if (!appointment.getEndDatetime().isAfter(startDateTime)) {
                throw new BusinessException(ResultCode.APPOINTMENT_TIME_INVALID);
            }
            // 如果提供了endDatetime，自动设置endTime（用于兼容单天预约）
            if (appointment.getEndTime() == null) {
                appointment.setEndTime(appointment.getEndDatetime().toLocalTime());
            }
        } else {
            // 单天预约：使用endTime校验
            if (appointment.getEndTime() == null 
                    || !appointment.getEndTime().isAfter(appointment.getStartTime())) {
                throw new BusinessException(ResultCode.APPOINTMENT_TIME_INVALID);
            }
        }
    }
    
    /**
     * 计算总时长和总天数
     */
    private void calculateDurationAndDays(Appointment appointment) {
        LocalDateTime startDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(), 
                appointment.getStartTime()
        );

        LocalDateTime endDateTime;
        if (appointment.getEndDatetime() != null) {
            endDateTime = appointment.getEndDatetime();
        } else {
            // 单天预约：使用endTime构建endDateTime
            endDateTime = LocalDateTime.of(
                    appointment.getAppointmentDate(), 
                    appointment.getEndTime()
            );
        }

        // 计算总时长（向上取整到整小时，不保留小数）
        Duration duration = Duration.between(startDateTime, endDateTime);
        long totalMinutes = duration.toMinutes();
        // 向上取整到整小时，例如：4.1小时 -> 5小时，6.3小时 -> 7小时
        long roundedHours = totalMinutes <= 0 ? 0 : (totalMinutes + 59) / 60;
        BigDecimal totalHours = BigDecimal.valueOf(roundedHours);
        appointment.setTotalDuration(totalHours);
        
        // 根据结算方式计算总天数
        String billingType = appointment.getBillingType();
        if (billingType == null || billingType.isEmpty()) {
            billingType = "hourly";
        }
        
        if ("daily".equals(billingType)) {
            // 按天结算：不满一天按一天计算
            long days = duration.toDays();
            if (duration.toHours() % 24 > 0 || duration.toMinutes() % (24 * 60) > 0) {
                days += 1; // 不满一天按一天计算
            }
            appointment.setTotalDays((int) Math.max(1, days)); // 至少1天
        } else {
            // 按小时或按次数：不计算天数，或计算实际天数（用于显示）
            long days = duration.toDays();
            if (duration.toHours() % 24 > 0 || duration.toMinutes() % (24 * 60) > 0) {
                days += 1;
            }
            appointment.setTotalDays((int) Math.max(0, days));
        }
    }
    
    /**
     * 计算总金额
     */
    private void calculateTotalAmount(Appointment appointment) {
        if (appointment.getServiceId() == null) {
            // 如果没有服务ID，无法计算金额
            if (appointment.getTotalAmount() == null) {
                appointment.setTotalAmount(BigDecimal.ZERO);
            }
            return;
        }
        
        housekeeping.housekeepingnow.entity.Service service = serviceMapper.selectById(appointment.getServiceId());
        if (service == null || service.getPrice() == null) {
            if (appointment.getTotalAmount() == null) {
                appointment.setTotalAmount(BigDecimal.ZERO);
            }
            return;
        }

        BigDecimal servicePrice = service.getPrice();
        // 按小时结算：价格 × 计费时长（小时）
        BigDecimal duration = appointment.getTotalDuration();
        if (duration == null || duration.compareTo(BigDecimal.ZERO) <= 0) {
            duration = BigDecimal.ONE; // 默认1小时
        }

        // 使用服务配置的预估时长作为“最低服务时长”
        Integer estimatedDuration = service.getEstimatedDuration();
        int minHours = (estimatedDuration != null && estimatedDuration > 0) ? estimatedDuration : 1;

        // 最终计费小时数：不低于最低服务时长
        BigDecimal minHoursBD = BigDecimal.valueOf(minHours);
        BigDecimal billableHours = duration.max(minHoursBD);

        // 更新预约记录的总时长为最终计费小时数，确保前端看到的是整小时且不低于最低服务时长
        appointment.setTotalDuration(billableHours);

        BigDecimal totalAmount = servicePrice.multiply(billableHours);

        // 保留2位小数
        appointment.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public List<Appointment> listAppointments(Long customerId, Long staffId, Integer status,
                                              LocalDate startDate, LocalDate endDate) {
        return appointmentMapper.selectList(customerId, staffId, status, startDate, endDate);
    }

    @Override
    @Transactional
    public void autoUpdateAppointmentStatus() {
        // 1. 查询需要更新为"进行中"（状态2）的预约
        // 条件：状态为"已确认"（1），且开始时间已到
        List<Appointment> toStart = appointmentMapper.selectAppointmentsToStart();
        if (toStart != null && !toStart.isEmpty()) {
            List<Long> appointmentIds = toStart.stream()
                    .map(Appointment::getAppointmentId)
                    .collect(java.util.stream.Collectors.toList());
            appointmentMapper.batchUpdateStatus(appointmentIds, 2); // 状态2：进行中
            log.info("自动更新预约状态：{} 个预约已更新为'进行中'", appointmentIds.size());
        }
        
        // 2. 查询需要更新为"已完成"（状态3）的预约
        // 条件：状态为"进行中"（2），且结束时间已过
        List<Appointment> toComplete = appointmentMapper.selectAppointmentsToComplete();
        if (toComplete != null && !toComplete.isEmpty()) {
            List<Long> appointmentIds = toComplete.stream()
                    .map(Appointment::getAppointmentId)
                    .collect(java.util.stream.Collectors.toList());
            appointmentMapper.batchUpdateStatus(appointmentIds, 3); // 状态3：已完成
            log.info("自动更新预约状态：{} 个预约已更新为'已完成'", appointmentIds.size());
        }
    }

    @Override
    @Transactional
    public void deleteUnpaidAppointments() {
        // 查询10分钟未支付的预约
        List<Appointment> unpaidAppointments = appointmentMapper.selectUnpaidAppointmentsToDelete();
        if (unpaidAppointments != null && !unpaidAppointments.isEmpty()) {
            int deletedCount = 0;
            for (Appointment appointment : unpaidAppointments) {
                try {
                    // 先删除支付记录（因为外键约束）
                    paymentService.deleteByAppointmentId(appointment.getAppointmentId());
                    // 再删除预约记录
                    appointmentMapper.deleteById(appointment.getAppointmentId());
                    deletedCount++;
                    log.info("删除未支付订单：预约ID={}, 创建时间={}", 
                            appointment.getAppointmentId(), appointment.getCreateTime());
                } catch (Exception e) {
                    log.error("删除未支付订单失败，预约ID={}", appointment.getAppointmentId(), e);
                }
            }
            log.info("删除未支付订单任务完成：共删除 {} 个订单", deletedCount);
        }
    }
}


