package com.alec.InnovateX.spring.mybatis;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 注解 SQL 版 Mapper：接口没有实现类——@MapperScan 扫描后为它注册 MapperFactoryBean（FactoryBean），
 * 运行时生成代理注入容器。故意不开 @CacheNamespace（二级缓存留给 XML 版演示对照）
 */
public interface UserMapper {

    @Insert("insert into mb_user (username, email) values (#{username}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("select id, username, email from mb_user where id = #{id}")
    User selectById(Long id);

    @Select("select id, username, email from mb_user order by id")
    List<User> selectAll();

    @Update("update mb_user set email = #{email} where id = #{id}")
    int updateEmail(@Param("id") Long id, @Param("email") String email);

    @Delete("delete from mb_user where id = #{id}")
    int deleteById(Long id);
}
