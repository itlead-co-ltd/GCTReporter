package com.gct.reportgenerator.service;

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
     * 用户登出
     * 
     * 清除会话信息并记录登出日志
     * 后续版本将实现Token黑名单机制
     */
    public void logout() {
        // 记录登出操作
        log.info("用户登出操作执行");
        
        // TODO: 后续版本实现Token黑名单机制
        // 将当前Token加入黑名单，确保Token立即失效
        // 示例: tokenBlacklistService.addToBlacklist(currentToken);
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
