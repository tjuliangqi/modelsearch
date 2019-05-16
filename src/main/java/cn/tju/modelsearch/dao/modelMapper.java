package cn.tju.modelsearch.dao;

import cn.tju.modelsearch.domain.ModelSql;
import cn.tju.modelsearch.domain.modifyModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface modelMapper {
    ModelSql selectModelById(@Param("id") String id, @Param("table") String table);
    int insertModel(@Param("modelSql") ModelSql modelSql, @Param("table") String table);
    int updateModel(@Param("modifyModel") modifyModel modifyModel, @Param("table") String table);
    int updateDownload(@Param("downloadTimes")int downloadTimes, @Param("ID") String ID, @Param("table")String table);
    int deleteById(@Param("ID") String ID, @Param("table") String table);
    int selectByClass(@Param("className") String className, @Param("subclassName") String subclassName, @Param("table") String table);
}
