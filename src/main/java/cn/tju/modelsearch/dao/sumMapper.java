package cn.tju.modelsearch.dao;

import cn.tju.modelsearch.domain.sum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface sumMapper {
    List<sum> selectSumByDate(@Param("date") String date, @Param("table") String table);
    int insertSum(@Param("date") String date, @Param("table") String table);
    int updateSum(@Param("sum") sum sum, @Param("table") String table);
}
