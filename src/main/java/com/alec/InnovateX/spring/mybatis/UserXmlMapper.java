package com.alec.InnovateX.spring.mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * XML 版 Mapper：SQL 写在 resources/mybatis/UserXmlMapper.xml 里（namespace 指向本接口），
 * 承担动态 SQL（if/where/foreach）与二级缓存（&lt;cache/&gt;）的演示
 */
public interface UserXmlMapper {

    /** 动态 SQL：<where> + <if> 按条件拼装 */
    List<User> selectByCondition(@Param("username") String username, @Param("emailLike") String emailLike);

    /** 动态 SQL：<foreach> 批量插入 */
    int insertBatch(@Param("users") List<User> users);

    /** 二级缓存演示：<cache/> 跨 SqlSession 生效 */
    User selectById(Long id);
}
