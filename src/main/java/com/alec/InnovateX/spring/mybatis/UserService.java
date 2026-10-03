package com.alec.InnovateX.spring.mybatis;

import org.springframework.transaction.annotation.Transactional;

/**
 * 事务与缓存验证的载体：@MapperScan 的代理 Mapper 直接注入使用
 */
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /** 插入后抛异常：@Transactional 回滚演示 */
    @Transactional
    public void insertThenRollback(String username, String email) {
        userMapper.insert(new User(username, email));
        throw new IllegalStateException("触发回滚");
    }

    /**
     * 同一事务内两次相同查询：Spring 的 SqlSessionTemplate 在事务内复用同一个 SqlSession，
     * MyBatis 一级缓存（SqlSession 级）命中——第二次查询不再执行 SQL
     */
    @Transactional
    public User[] twiceSameQueryInTx(Long id) {
        return new User[]{userMapper.selectById(id), userMapper.selectById(id)};
    }

    /**
     * 无事务两次相同查询：每次 Mapper 调用都新建 SqlSession——
     * 一级缓存随会话销毁而失效，两次都真实执行 SQL（这正是"Spring 集成下一级缓存几乎无效"的原因）
     */
    public User[] twiceSameQueryNoTx(Long id) {
        return new User[]{userMapper.selectById(id), userMapper.selectById(id)};
    }
}
