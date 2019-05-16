package cn.tju.modelsearch.dao;

import cn.tju.modelsearch.domain.user;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface userMapper {
    List<user> getUserByEmail(@Param("email") String email, @Param("table") String table);
    List<user> getUserByUserName(@Param("userName") String userName, @Param("table") String table);
    int insertUser(@Param("user") user user);
}
