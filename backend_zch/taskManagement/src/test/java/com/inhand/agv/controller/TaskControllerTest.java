package com.inhand.agv.controller;

import com.inhand.agv.common.AjaxResult;
import com.inhand.agv.common.TableDataInfo;
import com.inhand.agv.domain.AgvTask;
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
    void getTaskList() {
        // 准备测试数据
        TaskFilter filter = new TaskFilter();
        AgvTask task1 = new AgvTask();
        task1.setId(1L);
        AgvTask task2 = new AgvTask();
        task2.setId(2L);
        List<AgvTask> tasks = Arrays.asList(task1, task2);

        // 模拟Service行为
        when(taskService.listTasks(any(TaskFilter.class))).thenReturn(tasks);

        // 调用Controller方法
        TableDataInfo result = taskController.getTaskList(filter);

        // 验证结果
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRows().size());
    }

    @Test
    void getTaskDetail() {
        // 准备测试数据
        Long taskId = 1L;
        AgvTask task = new AgvTask();
        task.setId(taskId);

        // 模拟Service行为
        when(taskService.getTask(taskId)).thenReturn(task);

        // 调用Controller方法
        AjaxResult result = taskController.getTaskDetail(taskId);

        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals(task, result.getData());
    }

    @Test
    void addTaskSuccess() {
        // 准备测试数据
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskName("测试任务");
        taskDTO.setStartPos("起点A");
        taskDTO.setTaskTrip("100米");
        taskDTO.setExecutor("张三");

        AgvTask savedTask = new AgvTask();
        savedTask.setId(1L);

        // 模拟Service行为
        when(taskService.addTask(any(TaskDTO.class))).thenReturn(savedTask);

        // 调用Controller方法
        AjaxResult result = taskController.addTask(taskDTO);

        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("创建任务成功", result.getMsg());
        assertEquals(savedTask, result.getData());
    }

    @Test
    void addTaskValidationFailed() {
        // 准备测试数据
        TaskDTO taskDTO = new TaskDTO(); // 缺少必填字段

        // 模拟Service行为
        when(taskService.addTask(any(TaskDTO.class)))
                .thenThrow(new IllegalArgumentException("任务名称不能为空"));

        // 调用Controller方法
        AjaxResult result = taskController.addTask(taskDTO);

        // 验证结果
        assertEquals(500, result.getCode());
        assertEquals("任务名称不能为空", result.getMsg());
    }

    @Test
    void updateTaskSuccess() {
        // 准备测试数据
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTaskName("更新后的任务");
        taskDTO.setStartPos("起点B");
        taskDTO.setTaskTrip("200米");
        taskDTO.setExecutor("李四");

        AgvTask updatedTask = new AgvTask();
        updatedTask.setId(1L);

        // 模拟Service行为
        when(taskService.updateTask(any(TaskDTO.class))).thenReturn(updatedTask);

        // 调用Controller方法
        AjaxResult result = taskController.updateTask(taskDTO);

        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("更新任务成功", result.getMsg());
        assertEquals(updatedTask, result.getData());
    }

    @Test
    void deleteTaskSuccess() {
        // 准备测试数据
        Long taskId = 1L;

        // 调用Controller方法
        AjaxResult result = taskController.delTask(taskId);

        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("删除任务成功", result.getMsg());

        // 验证Service方法被调用
        verify(taskService, times(1)).delTask(taskId);
    }
}