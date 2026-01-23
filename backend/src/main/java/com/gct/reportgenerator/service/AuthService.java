package com.gct.reportgenerator.service;

import com.gct.reportgenerator.dto.ChangePasswordRequest;
import com.gct.reportgenerator.dto.LoginRequest;
import com.gct.reportgenerator.dto.LoginResponse;
import com.gct.reportgenerator.entity.User;
import com.gct.reportgenerator.exception.BusinessException;
import com.gct.reportgenerator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应（包含token）
     * @throws BusinessException 登录失败时抛出
     */
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录尝试, username: {}", request.getUsername());

        // 查找用户
        User user = userRepository.findByUsernameAndEnabled(request.getUsername(), true)
                .orElseThrow(() -> {
                    log.warn("登录失败: 用户不存在或已禁用, username: {}", request.getUsername());
                    return new BusinessException("用户名或密码错误");
                });

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败: 密码错误, username: {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 生成token（暂时使用简单的token，后续可以改为JWT）
        String token = generateToken(user);

        log.info("登录成功, username: {}, role: {}", user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    /**
     * 修改密码
     * 
     * @param request 修改密码请求
     * @throws BusinessException 修改密码失败时抛出
     */
    public void changePassword(ChangePasswordRequest request) {
        log.info("用户修改密码尝试, username: {}", request.getUsername());

        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("修改密码失败: 新密码和确认密码不一致, username: {}", request.getUsername());
            throw new BusinessException("新密码和确认密码不一致");
        }

        // 查找用户
        User user = userRepository.findByUsernameAndEnabled(request.getUsername(), true)
                .orElseThrow(() -> {
                    log.warn("修改密码失败: 用户不存在或已禁用, username: {}", request.getUsername());
                    return new BusinessException("用户不存在或已禁用");
                });

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("修改密码失败: 旧密码错误, username: {}", request.getUsername());
            throw new BusinessException("旧密码错误");
        }

        // 加密并保存新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("修改密码成功, username: {}", request.getUsername());
    }

    /**
     * 生成Token
     * 
     * @param user 用户对象
     * @return token字符串
     */
    private String generateToken(User user) {
        // 暂时使用简单的token格式: userId_timestamp
        // 生产环境应使用JWT
        return String.format("TOKEN_%d_%d", user.getId(), System.currentTimeMillis());
    }
}
