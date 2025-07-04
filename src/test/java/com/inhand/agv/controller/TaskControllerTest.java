package com.inhand.agv.controller;

import com.inhand.agv.common.AjaxResult;
import com.inhand.agv.common.TableDataInfo;
import com.inhand.agv.entity.AgvTask;
import com.inhand.agv.dto.TaskDTO;
import com.inhand.agv.dto.TaskFilter;
import com.inhand.agv.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @Test
    void listTaskSuccess() {
        // 准备测试数据
        TaskFilter filter = new TaskFilter();
        AgvTask task1 = new AgvTask();
        task1.setId(1L);
        task1.setTaskName("任务A");
        AgvTask task2 = new AgvTask();
        task2.setId(2L);
        task2.setTaskName("任务B");
        List<AgvTask> tasks = Arrays.asList(task1, task2);

        // 创建期望的 TableDataInfo 对象
        TableDataInfo expectedTableDataInfo = new TableDataInfo(2L, tasks); // 总数为2，当前页数据为tasks

        // 模拟Service行为：listTasks现在返回TableDataInfo
        when(taskService.listTasks(any(TaskFilter.class))).thenReturn(expectedTableDataInfo);

        // 调用Controller方法
        TableDataInfo result = taskController.listTask(filter);

        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result.getTotal()); // 验证总数
        assertEquals(2, result.getRows().size()); // 验证返回的行数
        assertEquals(tasks, result.getRows()); // 验证返回的数据内容
        assertEquals(200, result.getCode()); // 验证状态码
        assertEquals("操作成功", result.getMsg()); // 验证消息
        verify(taskService, times(1)).listTasks(filter);
    }

    @Test
    void getTaskSuccess() {
        // 准备测试数据
        Long taskId = 1L;
        AgvTask task = new AgvTask();
        task.setId(taskId);
        task.setTaskName("测试任务");

        // 模拟Service行为
        when(taskService.getTask(taskId)).thenReturn(task);

        // 调用Controller方法
        AjaxResult result = taskController.getTask(taskId);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(task, result.getData());
        verify(taskService, times(1)).getTask(taskId);
    }

    @Test
    void getTaskNotFound() {
        // 准备测试数据
        Long taskId = 99L;

        // 模拟Service行为
        when(taskService.getTask(taskId)).thenReturn(null);

        // 调用Controller方法
        AjaxResult result = taskController.getTask(taskId);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("任务不存在", result.getMsg());
        assertNull(result.getData());
        verify(taskService, times(1)).getTask(taskId);
    }

    @Test
    void addTaskSuccess() {
        // 准备测试数据
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskName("新任务");
        taskDTO.setStartPos("50米");
        taskDTO.setTaskTrip("100米");
        taskDTO.setExecutor("张三");

        AgvTask createdTask = new AgvTask();
        createdTask.setId(1L);
        createdTask.setTaskName("新任务");

        // 模拟Service行为
        when(taskService.addTask(any(TaskDTO.class))).thenReturn(createdTask);

        // 调用Controller方法
        AjaxResult result = taskController.addTask(taskDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("创建任务成功", result.getMsg());
        assertEquals(createdTask, result.getData());
        verify(taskService, times(1)).addTask(taskDTO);
    }

    @Test
    void addTaskInvalidInput() {
        // 准备测试数据，任务名称为空
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskName("");
        taskDTO.setStartPos("50米");
        taskDTO.setTaskTrip("100m");
        taskDTO.setExecutor("张三");

        // 模拟Service行为
        when(taskService.addTask(any(TaskDTO.class)))
                .thenThrow(new IllegalArgumentException("任务名称不能为空"));

        // 调用Controller方法
        AjaxResult result = taskController.addTask(taskDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("任务名称不能为空", result.getMsg());
        verify(taskService, times(1)).addTask(taskDTO);
    }

    @Test
    void updateTaskSuccess() {
        // 准备测试数据
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTaskName("更新后的任务");
        taskDTO.setStartPos("100米");
        taskDTO.setTaskTrip("200米");
        taskDTO.setExecutor("李四");

        AgvTask updatedTask = new AgvTask();
        updatedTask.setId(1L);
        updatedTask.setTaskName("更新后的任务");

        // 模拟Service行为
        when(taskService.updateTask(any(TaskDTO.class))).thenReturn(updatedTask);

        // 调用Controller方法
        AjaxResult result = taskController.updateTask(taskDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("更新任务成功", result.getMsg());
        assertEquals(updatedTask, result.getData());
        verify(taskService, times(1)).updateTask(taskDTO);
    }

    @Test
    void updateTaskNotFound() {
        // 准备测试数据
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(99L); // 不存在的ID
        taskDTO.setTaskName("更新后的任务");

        // 模拟Service行为
        when(taskService.updateTask(any(TaskDTO.class)))
                .thenThrow(new IllegalArgumentException("任务不存在"));

        // 调用Controller方法
        AjaxResult result = taskController.updateTask(taskDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("任务不存在", result.getMsg());
        verify(taskService, times(1)).updateTask(taskDTO);
    }

    @Test
    void deleteTaskSuccess() {
        // 准备测试数据
        Long taskId = 1L;

        // 模拟Service行为（delTask返回void）
        doNothing().when(taskService).delTask(taskId);

        // 调用Controller方法
        AjaxResult result = taskController.delTask(taskId);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("删除任务成功", result.getMsg());
        verify(taskService, times(1)).delTask(taskId);
    }

    @Test
    void deleteTaskFailed() {
        // 准备测试数据
        Long taskId = 1L;

        // 模拟Service行为抛出异常
        doThrow(new IllegalArgumentException("只有待巡视状态的任务可以删除")).when(taskService).delTask(taskId);

        // 调用Controller方法
        AjaxResult result = taskController.delTask(taskId);

        // 验证结果
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("只有待巡视状态的任务可以删除", result.getMsg());
        verify(taskService, times(1)).delTask(taskId);
    }

    @Test
    void startTaskSuccess() {
        Long taskId = 1L;
        AgvTask startedTask = new AgvTask();
        startedTask.setId(taskId);
        startedTask.setTaskStatus("巡视中");

        when(taskService.startTask(taskId)).thenReturn(startedTask);

        AjaxResult result = taskController.startTask(taskId);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("启动任务成功", result.getMsg());
        assertEquals(startedTask, result.getData());
        verify(taskService, times(1)).startTask(taskId);
    }

    @Test
    void startTaskFailed() {
        Long taskId = 1L;
        when(taskService.startTask(taskId)).thenThrow(new IllegalArgumentException("只有待巡视状态的任务才能启动"));

        AjaxResult result = taskController.startTask(taskId);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("只有待巡视状态的任务才能启动", result.getMsg());
        verify(taskService, times(1)).startTask(taskId);
    }

    @Test
    void endTaskSuccess() {
        Long taskId = 1L;
        AgvTask endedTask = new AgvTask();
        endedTask.setId(taskId);
        endedTask.setTaskStatus("待上传");

        when(taskService.endTask(taskId)).thenReturn(endedTask);

        AjaxResult result = taskController.endTask(taskId);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("结束任务成功", result.getMsg());
        assertEquals(endedTask, result.getData());
        verify(taskService, times(1)).endTask(taskId);
    }

    @Test
    void endTaskFailed() {
        Long taskId = 1L;
        when(taskService.endTask(taskId)).thenThrow(new IllegalArgumentException("只有巡视中的任务才能结束"));

        AjaxResult result = taskController.endTask(taskId);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("只有巡视中的任务才能结束", result.getMsg());
        verify(taskService, times(1)).endTask(taskId);
    }

    @Test
    void preUploadTaskSuccess() {
        Long taskId = 1L;
        AgvTask preUploadTask = new AgvTask();
        preUploadTask.setId(taskId);
        preUploadTask.setTaskStatus("待上传");

        when(taskService.preUploadTask(taskId)).thenReturn(preUploadTask);

        AjaxResult result = taskController.preUploadTask(taskId);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("查询待上传任务数据成功", result.getMsg());
        assertEquals(preUploadTask, result.getData());
        verify(taskService, times(1)).preUploadTask(taskId);
    }

    @Test
    void preUploadTaskFailed() {
        Long taskId = 1L;
        when(taskService.preUploadTask(taskId)).thenThrow(new IllegalArgumentException("只有待上传状态的任务才能进行数据查询操作"));

        AjaxResult result = taskController.preUploadTask(taskId);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("只有待上传状态的任务才能进行数据查询操作", result.getMsg());
        verify(taskService, times(1)).preUploadTask(taskId);
    }

    @Test
    void uploadTaskSuccess() {
        Long taskId = 1L;
        AgvTask uploadedTask = new AgvTask();
        uploadedTask.setId(taskId);
        uploadedTask.setTaskStatus("已完成");
        uploadedTask.setUploaded(true);

        when(taskService.uploadTask(taskId)).thenReturn(uploadedTask);

        AjaxResult result = taskController.uploadTask(taskId);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("上传巡视结果成功", result.getMsg());
        assertEquals(uploadedTask, result.getData());
        verify(taskService, times(1)).uploadTask(taskId);
    }

    @Test
    void uploadTaskFailed() {
        Long taskId = 1L;
        when(taskService.uploadTask(taskId)).thenThrow(new IllegalArgumentException("只有待上传状态的任务才能上传"));

        AjaxResult result = taskController.uploadTask(taskId);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("只有待上传状态的任务才能上传", result.getMsg());
        verify(taskService, times(1)).uploadTask(taskId);
    }
}