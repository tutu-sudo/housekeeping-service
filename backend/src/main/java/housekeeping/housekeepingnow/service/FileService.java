package housekeeping.housekeepingnow.service;

import housekeeping.housekeepingnow.entity.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件管理服务
 */
public interface FileService {

    /**
     * 通用文件上传
     */
    FileInfo upload(MultipartFile file, String category, Long relatedId, Long uploadUserId) throws IOException;

    /**
     * 根据ID查询
     */
    FileInfo getById(Long fileId);

    /**
     * 查询某个业务下的文件列表
     */
    List<FileInfo> listByCategoryAndRelatedId(String category, Long relatedId);

    /**
     * 删除文件（含删除物理文件）
     */
    void delete(Long fileId);
}


