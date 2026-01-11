package housekeeping.housekeepingnow.controller;

import com.alipay.api.AlipayApiException;
import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.service.AlipayPaymentService;
import housekeeping.housekeepingnow.service.PaymentService;
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
            
            // 根据交易状态跳转到不同的页面
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功，跳转到支付成功页面
                // 注意：这里只是同步回调，真正的业务处理应该在异步通知中完成
                response.sendRedirect("/payment/success?outTradeNo=" + outTradeNo + "&tradeNo=" + tradeNo);
            } else {
                // 支付失败或其他状态，跳转到支付失败页面
                response.sendRedirect("/payment/fail?reason=" + tradeStatus);
            }
            
        } catch (Exception e) {
            log.error("处理支付宝同步回调异常", e);
            try {
                response.sendRedirect("/payment/fail?reason=处理异常");
            } catch (IOException ioException) {
                log.error("重定向失败", ioException);
            }
        }
    }

    /**
     * 查询支付状态
     * 
     * @param appointmentId 预约ID
     * @return 支付信息
     */
    @Operation(summary = "查询支付状态")
    @GetMapping("/status/{appointmentId}")
    public Result<housekeeping.housekeepingnow.entity.Payment> getPaymentStatus(@PathVariable Long appointmentId) {
        log.info("查询支付状态，预约ID：{}", appointmentId);
        housekeeping.housekeepingnow.entity.Payment payment = paymentService.getByAppointmentId(appointmentId);
        if (payment == null) {
            log.warn("未找到支付记录，预约ID：{}", appointmentId);
            return Result.fail("该预约暂无支付记录");
        }
        log.info("查询到支付记录，支付ID：{}, 预约ID：{}, 支付状态：{}, 支付时间：{}", 
                payment.getPaymentId(), payment.getAppointmentId(), 
                payment.getPaymentStatus(), payment.getPaymentTime());
        return Result.success(payment);
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

