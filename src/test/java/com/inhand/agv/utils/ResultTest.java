package com.inhand.agv.utils;

import com.inhand.agv.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {

    /**
     * 测试无参构造函数
     */
    @Test
    void constructor_NoArgs() {
        Result<Object> result = new Result<>();
        assertNull(result.getCode());
        assertNull(result.getMsg());
        assertNull(result.getData());
    }

    /**
     * 测试带数据参数的构造函数
     */
    @Test
    void constructor_WithData() {
        User testUser = new User();
        testUser.setUname("test");
        Result<User> result = new Result<>(testUser);
        assertNull(result.getCode());
        assertNull(result.getMsg());
        assertEquals(testUser, result.getData());
    }

    /**
     * 测试静态 success() 方法
     */
    @Test
    void success_NoData() {
        Result<Object> result = Result.success();
        assertNotNull(result);
        assertEquals("0", result.getCode());
        assertEquals("成功", result.getMsg());
        assertNull(result.getData());
    }

    /**
     * 测试静态 success(T data) 方法
     */
    @Test
    void success_WithData() {
        User testUser = new User();
        testUser.setUname("test");
        Result<User> result = Result.success(testUser);
        assertNotNull(result);
        assertEquals("0", result.getCode());
        assertEquals("成功", result.getMsg());
        assertEquals(testUser, result.getData());
    }

    /**
     * 测试静态 success(T data, String msg) 方法
     */
    @Test
    void success_WithDataAndMsg() {
        User testUser = new User();
        testUser.setUname("test");
        String customMsg = "操作成功！";
        Result<User> result = Result.success(testUser, customMsg);
        assertNotNull(result);
        assertEquals("0", result.getCode());
        assertEquals(customMsg, result.getMsg());
        assertEquals(testUser, result.getData());
    }

    /**
     * 测试静态 error(String code, String msg) 方法
     */
    @Test
    void error_WithCodeAndMsg() {
        String errorCode = "500";
        String errorMsg = "内部服务器错误";
        Result<Object> result = Result.error(errorCode, errorMsg);
        assertNotNull(result);
        assertEquals(errorCode, result.getCode());
        assertEquals(errorMsg, result.getMsg());
        assertNull(result.getData());
    }

    /**
     * 测试 getters 和 setters
     */
    @Test
    void gettersAndSetters() {
        Result<String> result = new Result<>();
        String testCode = "200";
        String testMsg = "OK";
        String testData = "Some Data";

        result.setCode(testCode);
        result.setMsg(testMsg);
        result.setData(testData);

        assertEquals(testCode, result.getCode());
        assertEquals(testMsg, result.getMsg());
        assertEquals(testData, result.getData());
    }
}