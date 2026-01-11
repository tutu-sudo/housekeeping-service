package housekeeping.housekeepingnow.util;

import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 文件上传工具类
 */
@Component
public class FileUploadUtil {
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    @Value("${file.upload.url-prefix}")
    private String urlPrefix;
    
    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file, String category) throws IOException {
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = IdUtil.simpleUUID() + extension;
        
        // 创建目录结构：年/月/日
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path dirPath = Paths.get(uploadPath, category, dateDir);
        Files.createDirectories(dirPath);
        
        // 保存文件
        Path filePath = dirPath.resolve(fileName);
        Files.write(filePath, file.getBytes());
        
        // 返回相对路径
        return category + "/" + dateDir + "/" + fileName;
    }
    
    /**
     * 获取文件访问URL
     */
    public String getFileUrl(String filePath) {
        return urlPrefix + filePath;
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadPath, filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
}

