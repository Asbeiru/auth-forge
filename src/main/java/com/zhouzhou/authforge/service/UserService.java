package com.zhouzhou.authforge.service;

import com.zhouzhou.authforge.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.List;

public interface UserService extends UserDetailsService {
    
    /**
     * 查找所有用户
     */
    List<User> findAll();

    /**
     * 根据ID查找用户
     */
    User findById(Long id);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 保存或更新用户
     */
    User save(User user);

    /**
     * 删除用户
     */
    void deleteById(Long id);

    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(String username);
} 