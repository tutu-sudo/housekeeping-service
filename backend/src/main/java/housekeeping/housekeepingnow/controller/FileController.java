package housekeeping.housekeepingnow.controller;

import housekeeping.housekeepingnow.common.result.Result;
import housekeeping.housekeepingnow.entity.FileInfo;
import housekeeping.housekeepingnow.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 通用文件上传接口（头像、证书等）
 */
@Tag(name = "文件管理接口")
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<FileInfo> upload(@RequestPart("file") MultipartFile file,
                                   @RequestParam(required = false, defaultValue = "other") String category,
                                   @RequestParam(required = false) Long relatedId,
                                   @RequestParam(required = false) Long uploadUserId) throws IOException {
        FileInfo info = fileService.upload(file, category, relatedId, uploadUserId);
        return Result.success(info);
    }

    @Operation(summary = "根据ID查询文件信息")
    @GetMapping("/{fileId}")
    public Result<FileInfo> getById(@PathVariable Long fileId) {
        return Result.success(fileService.getById(fileId));
    }

    @Operation(summary = "按分类和业务ID查询文件列表")
    @GetMapping
    public Result<List<FileInfo>> list(@RequestParam String category,
                                       @RequestParam(required = false) Long relatedId) {
        return Result.success(fileService.listByCategoryAndRelatedId(category, relatedId));
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    public Result<Void> delete(@PathVariable Long fileId) {
        fileService.delete(fileId);
        return Result.success();
    }
}


