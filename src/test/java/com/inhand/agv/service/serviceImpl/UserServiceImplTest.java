package com.inhand.agv.service.serviceImpl;

import com.inhand.agv.entity.User;
import com.inhand.agv.repository.UserDao;
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
public class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService; // 注入被测试的UserServiceImpl

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUid(1L);
        sampleUser.setUname("testuser");
        sampleUser.setPassword("rawpassword"); // 模拟数据库中的原始密码
    }

    /**
     * 测试登录服务 - 成功
     */
    @Test
    void loginService_Success() {
        // 模拟 userDao.findByUnameAndPassword 找到用户
        when(userDao.findByUnameAndPassword("testuser", "rawpassword")).thenReturn(sampleUser);

        // 调用服务方法
        User loggedInUser = userService.loginService("testuser", "rawpassword");

        // 验证结果
        assertNotNull(loggedInUser);
        assertEquals(1L, loggedInUser.getUid());
        assertEquals("testuser", loggedInUser.getUname());
        assertEquals("", loggedInUser.getPassword()); // 验证密码已被清空

        // 验证 userDao.findByUnameAndPassword 被调用一次
        verify(userDao, times(1)).findByUnameAndPassword("testuser", "rawpassword");
    }

    /**
     * 测试登录服务 - 失败（账号或密码错误）
     */
    @Test
    void loginService_Failure_InvalidCredentials() {
        // 模拟 userDao.findByUnameAndPassword 未找到用户
        when(userDao.findByUnameAndPassword("wronguser", "wrongpass")).thenReturn(null);

        // 调用服务方法
        User loggedInUser = userService.loginService("wronguser", "wrongpass");

        // 验证结果
        assertNull(loggedInUser); // 预期返回null

        // 验证 userDao.findByUnameAndPassword 被调用一次
        verify(userDao, times(1)).findByUnameAndPassword("wronguser", "wrongpass");
    }

    /**
     * 测试注册服务 - 成功
     */
    @Test
    void registService_Success() {
        User newUser = new User();
        newUser.setUname("newuser");
        newUser.setPassword("newpass");

        User savedUser = new User();
        savedUser.setUid(2L); // 模拟保存后生成ID
        savedUser.setUname("newuser");
        savedUser.setPassword("newpass");

        // 模拟 userDao.findByUname 返回 null (用户名不存在)
        when(userDao.findByUname("newuser")).thenReturn(null);
        // 模拟 userDao.save 返回保存后的用户对象
        when(userDao.save(any(User.class))).thenReturn(savedUser);

        // 调用服务方法
        User registeredUser = userService.registService(newUser);

        // 验证结果
        assertNotNull(registeredUser);
        assertEquals(2L, registeredUser.getUid());
        assertEquals("newuser", registeredUser.getUname());
        assertEquals("", registeredUser.getPassword()); // 验证密码被清空

        // 验证 userDao.findByUname 和 userDao.save 都被调用
        verify(userDao, times(1)).findByUname("newuser");
        verify(userDao, times(1)).save(newUser);
    }

    /**
     * 测试注册服务 - 失败（用户名已存在）
     */
    @Test
    void registService_Failure_UsernameExists() {
        User existingUser = new User();
        existingUser.setUname("existinguser");
        existingUser.setPassword("pass");

        // 模拟 userDao.findByUname 找到已存在的用户
        when(userDao.findByUname("existinguser")).thenReturn(sampleUser); // 返回一个已存在的用户

        // 调用服务方法
        User registeredUser = userService.registService(existingUser);

        // 验证结果
        assertNull(registeredUser); // 预期返回null

        // 验证 userDao.findByUname 被调用，但 userDao.save 不应被调用
        verify(userDao, times(1)).findByUname("existinguser");
        verify(userDao, never()).save(any(User.class));
    }
}