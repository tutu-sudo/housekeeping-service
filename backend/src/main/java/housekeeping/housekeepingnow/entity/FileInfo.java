package housekeeping.housekeepingnow.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件信息实体类
 */
@Data
public class FileInfo {
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 文件访问URL
     */
    private String fileUrl;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件分类：avatar-头像，certificate-证书，document-文档，other-其他
     */
    private String fileCategory;
    
    /**
     * 关联ID（如用户ID、服务人员ID等）
     */
    private Long relatedId;
    
    /**
     * 上传用户ID
     */
    private Long uploadUserId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

