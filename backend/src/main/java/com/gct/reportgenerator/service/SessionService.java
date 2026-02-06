package com.gct.reportgenerator.service;

import com.gct.reportgenerator.dto.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Session管理服务
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private static final String USER_SESSION_KEY = "USER_SESSION";

    /**
     * 保存用户会话信息
     * 
     * @param request HttpServletRequest对象
     * @param userSession 用户会话信息
     */
    public void saveSession(HttpServletRequest request, UserSession userSession) {
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_SESSION_KEY, userSession);
        log.info("保存用户会话成功, userId: {}, username: {}, sessionId: {}", 
            userSession.getUserId(), userSession.getUsername(), session.getId());
    }

    /**
     * 获取用户会话信息
     * 
     * @param request HttpServletRequest对象
     * @return 用户会话信息，如果不存在返回null
     */
    public UserSession getSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (UserSession) session.getAttribute(USER_SESSION_KEY);
    }

    /**
     * 移除用户会话信息
     * 
     * @param request HttpServletRequest对象
     */
    public void removeSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            UserSession userSession = (UserSession) session.getAttribute(USER_SESSION_KEY);
            if (userSession != null) {
                log.info("移除用户会话, userId: {}, username: {}, sessionId: {}", 
                    userSession.getUserId(), userSession.getUsername(), sessionId);
            }
            session.invalidate();
        }
    }

    /**
     * 检查是否已登录
     * 
     * @param request HttpServletRequest对象
     * @return 是否已登录
     */
    public boolean isLoggedIn(HttpServletRequest request) {
        return getSession(request) != null;
    }
}
