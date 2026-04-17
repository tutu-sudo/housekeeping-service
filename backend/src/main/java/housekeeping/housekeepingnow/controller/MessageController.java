package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.annotation.RequireAuth;
import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.Message;
import housekeeping.housekeepingnow.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 站内消息接口（管理员和普通用户通用）
 */
@Tag(name = "站内消息接口")
@RestController
@RequestMapping("/api/messages")
@RequireAuth
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Operation(summary = "查询当前登录用户的消息列表", description = "isRead: 0-未读,1-已读,不传则查询全部")
    @GetMapping
    public Result<List<Message>> listMessages(@RequestParam(required = false) Integer isRead,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Message> list = messageService.listUserMessages(userId, isRead);
        return Result.success(list);
    }

    @Operation(summary = "标记消息为已读")
    @PostMapping("/{messageId}/read")
    public Result<Void> markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
        return Result.success();
    }
}

