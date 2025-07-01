package com.inhand.agv.service;

import com.inhand.agv.domain.AgvTask;
import com.inhand.agv.dto.TaskDTO;
import com.inhand.agv.dto.TaskFilter;
import com.inhand.agv.dto.ValidationResult;
import com.inhand.agv.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    public List<AgvTask> listTasks(TaskFilter filter) {
        return taskRepository.findAll(filter);
    }

    public AgvTask getTask(Long id) {
        return taskRepository.findById(id);
    }

    public AgvTask addTask(TaskDTO taskDTO) {
        ValidationResult validation = validateTask(taskDTO);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getMessage());
        }

        AgvTask task = new AgvTask();
        task.setTaskName(taskDTO.getTaskName());
        task.setStartPos(taskDTO.getStartPos());
        task.setTaskTrip(taskDTO.getTaskTrip());
        task.setExecutor(taskDTO.getExecutor());
        task.setCreator("当前登录用户"); // 实际项目中应从安全上下文中获取
        task.setTaskStatus("待巡视");
        task.setCreateTime(new Date());

        return taskRepository.saveTask(task);
    }

    public AgvTask updateTask(TaskDTO taskDTO) {
        ValidationResult validation = validateTask(taskDTO);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getMessage());
        }

        AgvTask existingTask = taskRepository.findById(taskDTO.getId());
        if (existingTask == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 只能修改待巡视状态的任务
        if (!"待巡视".equals(existingTask.getTaskStatus())) {
            throw new IllegalArgumentException("只有待巡视状态的任务可以修改");
        }

        existingTask.setTaskName(taskDTO.getTaskName());
        existingTask.setStartPos(taskDTO.getStartPos());
        existingTask.setTaskTrip(taskDTO.getTaskTrip());
        existingTask.setExecutor(taskDTO.getExecutor());

        return taskRepository.saveTask(existingTask);
    }

    public void delTask(Long id) {
        AgvTask task = taskRepository.findById(id);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 只能删除待巡视状态的任务
        if (!"待巡视".equals(task.getTaskStatus())) {
            throw new IllegalArgumentException("只有待巡视状态的任务可以删除");
        }

        taskRepository.deleteById(id);
    }

    public ValidationResult validateTask(TaskDTO taskDTO) {
        if (taskDTO.getTaskName() == null || taskDTO.getTaskName().trim().isEmpty()) {
            return new ValidationResult(false, "任务名称不能为空");
        }
        if (taskDTO.getStartPos() == null || taskDTO.getStartPos().trim().isEmpty()) {
            return new ValidationResult(false, "起始地点不能为空");
        }
        if (taskDTO.getTaskTrip() == null || taskDTO.getTaskTrip().trim().isEmpty()) {
            return new ValidationResult(false, "任务距离不能为空");
        }
        if (taskDTO.getExecutor() == null || taskDTO.getExecutor().trim().isEmpty()) {
            return new ValidationResult(false, "执行人不能为空");
        }
        return new ValidationResult(true, "验证通过");
    }
}
