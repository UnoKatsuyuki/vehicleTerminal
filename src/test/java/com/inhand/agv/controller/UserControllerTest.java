package com.inhand.agv.controller;

import com.inhand.agv.entity.User;
import com.inhand.agv.service.UserService;
import com.inhand.agv.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController; // 注入被测试的UserController

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUid(1L);
        sampleUser.setUname("testuser");
        sampleUser.setPassword(""); // 登录成功后密码会被置空
    }

    /**
     * 测试登录功能 - 成功
     */
    @Test
    void loginController_Success() {
        // 模拟 userService.loginService 返回一个成功的用户对象
        when(userService.loginService(anyString(), anyString())).thenReturn(sampleUser);

        // 直接调用 Controller 方法
        Result<User> result = userController.loginController("testuser", "password123");

        // 验证结果
        assertNotNull(result);
        assertEquals("0", result.getCode());
        assertEquals("登录成功！", result.getMsg());
        assertNotNull(result.getData());
        assertEquals(sampleUser.getUid(), result.getData().getUid());
        assertEquals(sampleUser.getUname(), result.getData().getUname());
        assertEquals(sampleUser.getPassword(), result.getData().getPassword()); // 验证密码被清空

        // 验证 userService.loginService 方法被调用一次
        verify(userService, times(1)).loginService("testuser", "password123");
    }

    /**
     * 测试登录功能 - 失败（账号或密码错误）
     */
    @Test
    void loginController_Failure_InvalidCredentials() {
        // 模拟 userService.loginService 返回 null，表示登录失败
        when(userService.loginService(anyString(), anyString())).thenReturn(null);

        // 直接调用 Controller 方法
        Result<User> result = userController.loginController("wronguser", "wrongpass");

        // 验证结果
        assertNotNull(result);
        assertEquals("123", result.getCode()); // 错误的code
        assertEquals("账号或密码错误！", result.getMsg());
        assertNull(result.getData());

        // 验证 userService.loginService 方法被调用一次
        verify(userService, times(1)).loginService("wronguser", "wrongpass");
    }

    /**
     * 测试注册功能 - 成功
     */
    @Test
    void registController_Success() {
        User newUserToRegister = new User();
        newUserToRegister.setUname("newuser");
        newUserToRegister.setPassword("newpass");

        // 模拟 userService.registService 返回一个新创建的用户对象
        when(userService.registService(any(User.class))).thenReturn(sampleUser); // 返回sampleUser作为成功注册的示例

        // 直接调用 Controller 方法
        Result<User> result = userController.registController(newUserToRegister);

        // 验证结果
        assertNotNull(result);
        assertEquals("0", result.getCode());
        assertEquals("注册成功！", result.getMsg());
        assertNotNull(result.getData());
        assertEquals(sampleUser.getUid(), result.getData().getUid()); // 验证返回的用户信息
        assertEquals(sampleUser.getUname(), result.getData().getUname());
        assertEquals(sampleUser.getPassword(), result.getData().getPassword()); // 验证密码被清空

        // 验证 userService.registService 方法被调用一次
        verify(userService, times(1)).registService(newUserToRegister);
    }

    /**
     * 测试注册功能 - 失败（用户名已存在）
     */
    @Test
    void registController_Failure_UsernameExists() {
        User existingUser = new User();
        existingUser.setUname("existinguser");
        existingUser.setPassword("pass");

        // 模拟 userService.registService 返回 null，表示注册失败（用户名已存在）
        when(userService.registService(any(User.class))).thenReturn(null);

        // 直接调用 Controller 方法
        Result<User> result = userController.registController(existingUser);

        // 验证结果
        assertNotNull(result);
        assertEquals("456", result.getCode()); // 错误的code
        assertEquals("用户名已存在！", result.getMsg());
        assertNull(result.getData());

        // 验证 userService.registService 方法被调用一次
        verify(userService, times(1)).registService(existingUser);
    }
}