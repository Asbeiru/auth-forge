package com.zhouzhou.authforge.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: 实现用户查询逻辑
        // 这里暂时返回一个测试用户
        return org.springframework.security.core.userdetails.User
            .withUsername(username)
            .password(passwordEncoder.encode("password"))
            .roles("USER")
            .build();
    }
} 