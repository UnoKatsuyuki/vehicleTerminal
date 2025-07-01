package com.inhand.agv.repository.impl;

import com.inhand.agv.domain.AgvTask;
import com.inhand.agv.dto.TaskFilter;
import com.inhand.agv.repository.TaskRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class TaskRepositoryImpl implements TaskRepository{
    private final ConcurrentHashMap<Long, AgvTask> taskStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public AgvTask saveTask(AgvTask task) {
        if (task.getId() == null) {
            // 新增任务
            task.setId(idGenerator.getAndIncrement());
            task.setCreateTime(new Date());
            task.setTaskStatus("待巡视");
            task.setUploaded(false);
            task.setDeleteFlag(false);
            // 生成任务编号
            task.setTaskCode("TASK-" + String.format("%04d", task.getId()));
        }
        taskStore.put(task.getId(), task);
        return task;
    }

    @Override
    public AgvTask findById(Long id) {
        return taskStore.get(id);
    }

    @Override
    public List<AgvTask> findAll(TaskFilter filter) {
        return taskStore.values().stream()
                .filter(task -> !task.getDeleteFlag())
                .filter(task -> filter.getTaskCode() == null || task.getTaskCode().contains(filter.getTaskCode()))
                .filter(task -> filter.getCreator() == null || task.getCreator().equals(filter.getCreator()))
                .filter(task -> filter.getExecutor() == null || task.getExecutor().equals(filter.getExecutor()))
                .filter(task -> filter.getTaskStatus() == null || task.getTaskStatus().equals(filter.getTaskStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        AgvTask task = taskStore.get(id);
        if (task != null) {
            task.setDeleteFlag(true);
            taskStore.put(id, task);
        }
    }

}
