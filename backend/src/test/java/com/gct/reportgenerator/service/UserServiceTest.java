package com.gct.reportgenerator.service;

import com.gct.reportgenerator.dto.CreateUserRequest;
import com.gct.reportgenerator.dto.UpdateUserRequest;
import com.gct.reportgenerator.dto.UserDTO;
import com.gct.reportgenerator.entity.User;
import com.gct.reportgenerator.exception.BusinessException;
import com.gct.reportgenerator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService单元测试
 * 
 * @author GCT Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("US005: 用户服务单元测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("$2a$10$encodedPassword")
                .role(User.Role.DESIGNER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("T005-4: 获取所有用户 - 成功场景")
    void getAllUsers_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserDTO> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("T005-4: 根据ID获取用户 - 成功场景")
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserDTO result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("T005-4: 根据ID获取用户 - 用户不存在")
    void getUserById_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getUserById(999L);
        });
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("T005-4: 创建用户 - 成功场景")
    void createUser_Success() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("newuser")
                .password("password123")
                .role(User.Role.VIEWER)
                .enabled(true)
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.createUser(request);

        // Then
        assertNotNull(result);
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("T005-4: 创建用户 - 用户名已存在")
    void createUser_UsernameExists() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("existinguser")
                .password("password123")
                .role(User.Role.VIEWER)
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.createUser(request);
        });
        assertEquals("用户名已存在", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("T005-4: 更新用户 - 成功场景")
    void updateUser_Success() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .password("newpassword")
                .role(User.Role.ADMIN)
                .enabled(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.updateUser(1L, request);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("T005-4: 更新用户 - 仅更新角色")
    void updateUser_OnlyRole() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .role(User.Role.ADMIN)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.updateUser(1L, request);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("T005-4: 删除用户 - 成功场景")
    void deleteUser_Success() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("T005-4: 删除用户 - 用户不存在")
    void deleteUser_UserNotFound() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.deleteUser(999L);
        });
        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("T005-4: 启用/禁用用户 - 成功场景")
    void toggleUserStatus_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.toggleUserStatus(1L, false);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }
}
