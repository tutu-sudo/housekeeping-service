package housekeeping.housekeepingnow.controller;

import com.alipay.api.AlipayApiException;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.service.AlipayPaymentService;
import housekeeping.housekeepingnow.service.AppointmentService;
import housekeeping.housekeepingnow.service.PaymentService;
import housekeeping.housekeepingnow.config.AlipayConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 支付控制器（支付宝）
 */
@Slf4j
@Tag(name = "支付接口")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private AlipayPaymentService alipayPaymentService;
    
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AlipayConfig alipayConfig;

    /**
     * 创建支付宝支付（POST方式，返回HTML表单）
     * 
     * @param appointmentId 预约ID
     * @param response HTTP响应对象
     */
    @Operation(summary = "创建支付宝支付页面（POST方式，返回HTML表单）")
    @PostMapping("/alipay/create/{appointmentId}")
    public void createAlipayPayment(@PathVariable Long appointmentId, 
                                    HttpServletResponse response) {
        try {
            // 支付前业务校验：仅允许待付款且未超时的订单创建支付
            appointmentService.validatePayable(appointmentId);
            // 调用支付宝服务创建支付页面
            String htmlForm = alipayPaymentService.createPagePay(appointmentId);
            
            // 设置响应头
            response.setContentType("text/html;charset=UTF-8");
            
            // 直接将HTML表单写入响应
            PrintWriter out = response.getWriter();
            out.println(htmlForm);
            out.flush();
            out.close();
            
            log.info("支付宝支付页面创建成功，预约ID：{}", appointmentId);
            
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付页面失败", e);
            throw new BusinessException(ResultCode.PAYMENT_FAILED, "创建支付页面失败：" + e.getMessage());
        } catch (IOException e) {
            log.error("写入响应失败", e);
            throw new BusinessException(ResultCode.PAYMENT_FAILED, "写入响应失败");
        }
    }

    /**
     * 创建支付宝支付链接（GET方式，返回支付URL）
     * 
     * @param appointmentId 预约ID
     * @return 支付链接
     */
    @Operation(summary = "创建支付宝支付链接（GET方式，返回支付URL）")
    @GetMapping("/alipay/create/{appointmentId}")
    public Result<String> createAlipayPaymentUrl(@PathVariable Long appointmentId) {
        try {
            // 支付前业务校验：仅允许待付款且未超时的订单创建支付
            appointmentService.validatePayable(appointmentId);
            String paymentUrl = alipayPaymentService.createPagePayUrl(appointmentId);
            log.info("支付宝支付链接创建成功，预约ID：{}", appointmentId);
            // 使用success(message, data)方法，避免方法重载冲突
            // 因为success(String)和success(T)存在重载冲突，当T=String时会优先匹配success(String)
            return Result.success("success", paymentUrl);
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付链接失败", e);
            throw new BusinessException(ResultCode.PAYMENT_FAILED, "创建支付链接失败：" + e.getMessage());
        }
    }

    /**
     * 支付宝异步通知回调接口
     * 
     * 注意：此接口必须能够被公网访问，如果没有公网IP，需要使用内网穿透工具（如natapp）
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     */
    @Operation(summary = "支付宝异步通知回调接口")
    @PostMapping("/alipay/notify")
    public void alipayNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("收到支付宝异步通知请求");
        
        try {
            // 获取请求参数
            Map<String, String> params = getAllRequestParams(request);
            
            log.info("支付宝异步通知参数：{}", params);
            
            // 验证签名
            boolean signVerified = alipayPaymentService.verifySignature(params);
            if (!signVerified) {
                log.error("支付宝异步通知签名验证失败");
                response.getWriter().write("fail");
                return;
            }
            
            log.info("支付宝异步通知签名验证成功");
            
            // 处理异步通知
            boolean handleResult = alipayPaymentService.handleNotify(params);
            
            if (handleResult) {
                // 处理成功，返回"success"告知支付宝
                response.getWriter().write("success");
                log.info("支付宝异步通知处理成功");
            } else {
                // 处理失败，返回"fail"告知支付宝重新通知
                response.getWriter().write("fail");
                log.error("支付宝异步通知处理失败");
            }
            
        } catch (Exception e) {
            log.error("处理支付宝异步通知异常", e);
            try {
                response.getWriter().write("fail");
            } catch (IOException ioException) {
                log.error("写入响应失败", ioException);
            }
        }
    }

    /**
     * 支付宝同步回调接口（支付完成后的页面跳转）
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     */
    @Operation(summary = "支付宝同步回调接口（支付完成后的页面跳转）")
    @GetMapping("/alipay/return")
    public void alipayReturn(HttpServletRequest request, HttpServletResponse response) {
        log.info("收到支付宝同步回调请求");
        
        try {
            // 获取请求参数
            Map<String, String> params = getAllRequestParams(request);
            
            log.info("支付宝同步回调参数：{}", params);
            
            // 验证签名
            boolean signVerified = alipayPaymentService.verifySignature(params);
            if (!signVerified) {
                log.error("支付宝同步回调签名验证失败");
                // 跳转到支付失败页面
                response.sendRedirect("/payment/fail?reason=签名验证失败");
                return;
            }
            
            log.info("支付宝同步回调签名验证成功");
            
            // 获取订单号和交易状态
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            String tradeNo = params.get("trade_no");
            
            log.info("订单号：{}, 交易状态：{}, 支付宝交易号：{}", outTradeNo, tradeStatus, tradeNo);
            
            // 为了本地/内网环境也能正常更新订单状态，这里复用异步通知的业务处理逻辑
            try {
                boolean handled = alipayPaymentService.handleNotify(params);
                log.info("同步回调中执行业务处理结果：{}", handled);
            } catch (Exception ex) {
                log.error("同步回调中执行业务处理异常（已忽略，不影响页面跳转）", ex);
            }

            // 解析 appointmentId，便于跳回前端后做一次状态刷新
            Long appointmentId = null;
            try {
                if (outTradeNo != null && outTradeNo.startsWith("APPT")) {
                    Long paymentId = Long.parseLong(outTradeNo.substring(4));
                    housekeeping.housekeepingnow.entity.Payment payment = paymentService.getById(paymentId);
                    appointmentId = payment.getAppointmentId();
                }
            } catch (Exception ex) {
                log.warn("同步回调解析 appointmentId 失败（不影响跳转）outTradeNo={}", outTradeNo, ex);
            }

            // 跳转到前端页面（优先使用配置），前端收到后调用 /api/payment/status/{appointmentId} 进行最终确认
            boolean paid = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
            String baseUrl = paid ? alipayConfig.getFrontendSuccessUrl() : alipayConfig.getFrontendFailUrl();
            if (baseUrl == null || baseUrl.isBlank()) {
                // 兜底：仍跳到后端路径（可能被前端代理接管）
                baseUrl = paid ? "/payment/success" : "/payment/fail";
            }
            StringBuilder redirect = new StringBuilder(baseUrl);
            // 追加 query
            String sep = baseUrl.contains("?") ? "&" : "?";
            redirect.append(sep)
                    .append("outTradeNo=").append(urlEncode(outTradeNo))
                    .append("&tradeNo=").append(urlEncode(tradeNo))
                    .append("&tradeStatus=").append(urlEncode(tradeStatus));
            if (appointmentId != null) {
                redirect.append("&appointmentId=").append(appointmentId);
            }
            response.sendRedirect(redirect.toString());
            
        } catch (Exception e) {
            log.error("处理支付宝同步回调异常", e);
            try {
                response.sendRedirect("/payment/fail?reason=" + urlEncode("处理异常"));
            } catch (IOException ioException) {
                log.error("重定向失败", ioException);
            }
        }
    }

    private String urlEncode(String v) {
        if (v == null) return "";
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    /**
     * 查询支付状态（从数据库查询；若本地仍未确认支付成功，则自动向支付宝查询并尝试同步）
     *
     * 说明：
     * - 用户支付成功后关闭页面/回调丢失/异步通知不可达时，本地可能仍停留在待支付(0/null)或支付失败(2)
     * - 此接口会在上述状态下触发一次支付宝主动查询，以提升前端展示的一致性
     *
     * @param appointmentId 预约ID
     * @return 支付信息
     */
    @Operation(summary = "查询支付状态（从数据库查询；若本地未确认成功则自动向支付宝查询并尝试同步）")
    @GetMapping("/status/{appointmentId}")
    public Result<housekeeping.housekeepingnow.entity.Payment> getPaymentStatus(@PathVariable Long appointmentId) {
        log.info("查询支付状态，预约ID：{}", appointmentId);
        housekeeping.housekeepingnow.entity.Payment payment = paymentService.getByAppointmentId(appointmentId);
        if (payment == null) {
            log.warn("未找到支付记录，预约ID：{}", appointmentId);
            return Result.fail("该预约暂无支付记录");
        }
        
        // 如果本地尚未确认支付成功（null/0/2），则主动查询支付宝状态并同步
        // paymentMethod 可能为空（历史数据/异常流程），此处不强依赖，仅排除明确的非支付宝方式
        Integer status = payment.getPaymentStatus();
        String method = payment.getPaymentMethod();
        boolean maybeAlipay = (method == null || "alipay".equalsIgnoreCase(method));
        boolean notConfirmedPaid = (status == null || status == 0 || status == 2);
        if (maybeAlipay && notConfirmedPaid) {
            log.info("检测到支付状态未确认成功（status={}），自动同步支付宝支付状态，预约ID：{}", status, appointmentId);
            try {
                boolean syncSuccess = alipayPaymentService.queryPaymentStatus(appointmentId);
                if (syncSuccess) {
                    // 重新查询支付记录，获取同步后的状态
                    payment = paymentService.getByAppointmentId(appointmentId);
                    log.info("支付状态同步成功，预约ID：{}，更新后状态：{}", appointmentId, payment.getPaymentStatus());
                } else {
                    log.info("支付状态同步未更新（可能订单尚未支付），预约ID：{}", appointmentId);
                }
            } catch (Exception e) {
                log.error("自动同步支付状态失败，预约ID：{}", appointmentId, e);
                // 同步失败不影响返回结果，继续返回数据库中的状态
            }
        }
        
        log.info("查询到支付记录，支付ID：{}, 预约ID：{}, 支付状态：{}, 支付时间：{}", 
                payment.getPaymentId(), payment.getAppointmentId(), 
                payment.getPaymentStatus(), payment.getPaymentTime());
        return Result.success(payment);
    }

    /**
     * 主动查询支付宝支付状态并同步（用于回调失败时手动同步）
     * 
     * @param appointmentId 预约ID
     * @return 查询结果
     */
    @Operation(summary = "主动查询支付宝支付状态并同步（用于回调失败时手动同步）")
    @PostMapping("/sync/{appointmentId}")
    public Result<String> syncPaymentStatus(@PathVariable Long appointmentId) {
        log.info("主动查询并同步支付状态，预约ID：{}", appointmentId);
        
        try {
            boolean success = alipayPaymentService.queryPaymentStatus(appointmentId);
            if (success) {
                log.info("支付状态同步成功，预约ID：{}", appointmentId);
                return Result.success("支付状态同步成功");
            } else {
                log.warn("支付状态同步失败或无需同步，预约ID：{}", appointmentId);
                return Result.fail("支付状态同步失败或订单尚未支付");
            }
        } catch (Exception e) {
            log.error("同步支付状态异常，预约ID：{}", appointmentId, e);
            return Result.fail("同步支付状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有请求参数（用于支付宝回调）
     * 
     * @param request HTTP请求对象
     * @return 参数Map
     */
    private Map<String, String> getAllRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        
        return params;
    }
}

