package com.inhand.agv.controller;

import com.inhand.agv.common.AjaxResult;
import com.inhand.agv.common.TableDataInfo;
import com.inhand.agv.entity.AgvTask;
import com.inhand.agv.dto.TaskDTO;
import com.inhand.agv.dto.TaskFilter;
import com.inhand.agv.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agv/task")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @GetMapping("/list")
    public TableDataInfo listTask(TaskFilter filter) {
        return taskService.listTasks(filter);
    }

    @GetMapping("/{id}")
    public AjaxResult getTask(@PathVariable Long id) {
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

    @PutMapping("/start/{id}")
    public AjaxResult startTask(@PathVariable Long id) {
        try {
            AgvTask task = taskService.startTask(id);
            return AjaxResult.success("启动任务成功", task);
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PutMapping("/stop/{id}?isAbort={flag}")
    public AjaxResult endTask(@PathVariable Long id) {
        try {
            AgvTask task = taskService.endTask(id);
            return AjaxResult.success("结束任务成功", task);
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PutMapping("/preUpload/{id}")
    public AjaxResult preUploadTask(@PathVariable Long id) {
        try {
            AgvTask task = taskService.preUploadTask(id);
            return AjaxResult.success("查询待上传任务数据成功", task);
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PutMapping("/upload/{id}")
    public AjaxResult uploadTask(@PathVariable Long id) {
        try {
            AgvTask task = taskService.uploadTask(id);
            return AjaxResult.success("上传巡视结果成功", task);
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
