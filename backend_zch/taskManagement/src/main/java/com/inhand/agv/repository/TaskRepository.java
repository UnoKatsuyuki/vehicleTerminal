package com.inhand.agv.repository;

import com.inhand.agv.domain.AgvTask;
import com.inhand.agv.dto.TaskFilter;

import java.util.List;

public interface TaskRepository {
    AgvTask saveTask(AgvTask task);
    AgvTask findById(Long id);
    List<AgvTask> findAll(TaskFilter filter);
    void deleteById(Long id);
}
