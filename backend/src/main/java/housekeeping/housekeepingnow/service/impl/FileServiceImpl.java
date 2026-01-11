package housekeeping.housekeepingnow.service.impl;

import housekeeping.housekeepingnow.common.enums.ResultCode;
import housekeeping.housekeepingnow.common.exception.BusinessException;
import housekeeping.housekeepingnow.entity.FileInfo;
import housekeeping.housekeepingnow.mapper.FileInfoMapper;
import housekeeping.housekeepingnow.service.FileService;
import housekeeping.housekeepingnow.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Override
    public FileInfo upload(MultipartFile file, String category, Long relatedId, Long uploadUserId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }
        if (category == null || category.isBlank()) {
            category = "other";
        }

        String filePath = fileUploadUtil.uploadFile(file, category);
        String url = fileUploadUtil.getFileUrl(filePath);

        FileInfo info = new FileInfo();
        info.setFileName(extractFileName(filePath));
        info.setOriginalName(file.getOriginalFilename());
        info.setFilePath(filePath);
        info.setFileUrl(url);
        info.setFileType(file.getContentType());
        info.setFileSize(file.getSize());
        info.setFileCategory(category);
        info.setRelatedId(relatedId);
        info.setUploadUserId(uploadUserId);

        fileInfoMapper.insert(info);
        return fileInfoMapper.selectById(info.getFileId());
    }

    @Override
    public FileInfo getById(Long fileId) {
        FileInfo info = fileInfoMapper.selectById(fileId);
        if (info == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return info;
    }

    @Override
    public List<FileInfo> listByCategoryAndRelatedId(String category, Long relatedId) {
        return fileInfoMapper.selectByCategoryAndRelatedId(category, relatedId);
    }

    @Override
    public void delete(Long fileId) {
        FileInfo info = fileInfoMapper.selectById(fileId);
        if (info == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        // 尝试删除物理文件（失败不抛错，只记录返回值）
        fileUploadUtil.deleteFile(info.getFilePath());
        fileInfoMapper.deleteById(fileId);
    }

    private String extractFileName(String filePath) {
        if (filePath == null) {
            return null;
        }
        int idx = filePath.lastIndexOf('/');
        if (idx >= 0 && idx < filePath.length() - 1) {
            return filePath.substring(idx + 1);
        }
        return filePath;
    }
}


