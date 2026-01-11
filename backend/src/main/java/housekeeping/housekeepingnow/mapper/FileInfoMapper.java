package housekeeping.housekeepingnow.mapper;

import housekeeping.housekeepingnow.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileInfoMapper {

    FileInfo selectById(@Param("fileId") Long fileId);

    List<FileInfo> selectByCategoryAndRelatedId(@Param("fileCategory") String fileCategory,
                                                @Param("relatedId") Long relatedId);

    int insert(FileInfo fileInfo);

    int deleteById(@Param("fileId") Long fileId);
}


