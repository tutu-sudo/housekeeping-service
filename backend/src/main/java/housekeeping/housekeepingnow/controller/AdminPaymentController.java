package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Payment;
import housekeeping.housekeepingnow.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台-支付明细
 */
@Tag(name = "后台-支付明细接口")
@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    @Autowired
    private PaymentService paymentService;

    @Operation(summary = "支付详情")
    @GetMapping("/{paymentId}")
    public Result<Payment> getById(@PathVariable Long paymentId) {
        return Result.success(paymentService.getById(paymentId));
    }

    @Operation(summary = "根据预约查询支付")
    @GetMapping("/appointment/{appointmentId}")
    public Result<Payment> getByAppointment(@PathVariable Long appointmentId) {
        Payment payment = paymentService.getByAppointmentId(appointmentId);
        if (payment == null) {
            return Result.fail("该预约暂无支付记录");
        }
        return Result.success(payment);
    }

    @Operation(summary = "支付记录列表")
    @GetMapping
    public Result<List<Payment>> list(@RequestParam(required = false) Long appointmentId,
                                      @RequestParam(required = false) Integer paymentStatus,
                                      @RequestParam(required = false) String paymentMethod) {
        return Result.success(paymentService.listPayments(appointmentId, paymentStatus, paymentMethod));
    }
}


