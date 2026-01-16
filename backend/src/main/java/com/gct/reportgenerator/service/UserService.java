package com.gct.reportgenerator.service;

import com.gct.reportgenerator.dto.CreateUserRequest;
import com.gct.reportgenerator.dto.UpdateUserRequest;
import com.gct.reportgenerator.dto.UserDTO;
import com.gct.reportgenerator.entity.User;
import com.gct.reportgenerator.exception.BusinessException;
import com.gct.reportgenerator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取所有用户列表
     * 
     * @return 用户列表
     */
    public List<UserDTO> getAllUsers() {
        log.info("获取所有用户列表");
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取用户
     * 
     * @param id 用户ID
     * @return 用户DTO
     */
    public UserDTO getUserById(Long id) {
        log.info("获取用户详情, ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return UserDTO.fromEntity(user);
    }

    /**
     * 创建用户
     * 
     * @param request 创建用户请求
     * @return 创建的用户DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(CreateUserRequest request) {
        log.info("创建用户, username: {}", request.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户实体 (SQLite workaround: manually set ID)
        Long nextId = userRepository.count() + 1;
        User user = User.builder()
                .id(nextId)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("创建用户成功, ID: {}, username: {}", savedUser.getId(), savedUser.getUsername());
        
        return UserDTO.fromEntity(savedUser);
    }

    /**
     * 更新用户
     * 
     * @param id 用户ID
     * @param request 更新用户请求
     * @return 更新后的用户DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        log.info("更新用户, ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 更新密码（如果提供）
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // 更新角色（如果提供）
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        
        // 更新启用状态（如果提供）
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("更新用户成功, ID: {}, username: {}", updatedUser.getId(), updatedUser.getUsername());
        
        return UserDTO.fromEntity(updatedUser);
    }

    /**
     * 删除用户
     * 
     * @param id 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        log.info("删除用户, ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
        
        userRepository.deleteById(id);
        log.info("删除用户成功, ID: {}", id);
    }

    /**
     * 启用/禁用用户
     * 
     * @param id 用户ID
     * @param enabled 是否启用
     * @return 更新后的用户DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO toggleUserStatus(Long id, Boolean enabled) {
        log.info("切换用户状态, ID: {}, enabled: {}", id, enabled);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        
        log.info("切换用户状态成功, ID: {}, enabled: {}", updatedUser.getId(), updatedUser.getEnabled());
        
        return UserDTO.fromEntity(updatedUser);
    }
}
