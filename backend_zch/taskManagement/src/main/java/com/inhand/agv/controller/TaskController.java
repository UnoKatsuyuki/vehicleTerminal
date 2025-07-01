package com.inhand.agv.controller;

import com.inhand.agv.common.AjaxResult;
import com.inhand.agv.common.TableDataInfo;
import com.inhand.agv.domain.AgvTask;
import com.inhand.agv.dto.TaskDTO;
import com.inhand.agv.dto.TaskFilter;
import com.inhand.agv.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agv/task")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @GetMapping("/list")
    public TableDataInfo getTaskList(TaskFilter filter) {
        List<AgvTask> tasks = taskService.listTasks(filter);
        return new TableDataInfo(tasks.size(), tasks);
    }

    @GetMapping("/{id}")
    public AjaxResult getTaskDetail(@PathVariable Long id) {
        AgvTask task = taskService.getTask(id);
        if (task == null) {
            return AjaxResult.error("任务不存在");
        }
        return AjaxResult.success(task);
    }

    @PostMapping
    public AjaxResult addTask(@RequestBody TaskDTO taskDTO) {
        try {
            AgvTask task = taskService.addTask(taskDTO);
            return AjaxResult.success("创建任务成功", task);
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PutMapping
    public AjaxResult updateTask(@RequestBody TaskDTO taskDTO) {
        try {
            AgvTask task = taskService.updateTask(taskDTO);
            return AjaxResult.success("更新任务成功", task);
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public AjaxResult delTask(@PathVariable Long id) {
        try {
            taskService.delTask(id);
            return AjaxResult.success("删除任务成功");
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
