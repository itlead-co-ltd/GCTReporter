package com.gct.reportgenerator.service;

import com.gct.reportgenerator.dto.UserDTO;
import com.gct.reportgenerator.entity.User;
import com.gct.reportgenerator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 
 * 提供用户管理相关的业务逻辑
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * 获取所有用户列表
     * 
     * @return 用户DTO列表
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("获取所有用户列表");
        List<User> users = userRepository.findAll();
        log.info("获取到{}个用户", users.size());
        
        return users.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 搜索用户
     * 
     * 支持按用户名模糊搜索
     * 
     * @param keyword 搜索关键字
     * @return 用户DTO列表
     */
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String keyword) {
        log.info("搜索用户，关键字: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword.trim());
        log.info("搜索到{}个用户", users.size());
        
        return users.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取用户
     * 
     * @param id 用户ID
     * @return 用户DTO
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.info("获取用户详情，ID: {}", id);
        
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElse(null);
    }
}
