package com.inhand.agv.service;

import com.inhand.agv.common.TableDataInfo;
import com.inhand.agv.entity.AgvTask;
import com.inhand.agv.dto.TaskDTO;
import com.inhand.agv.dto.TaskFilter;
import com.inhand.agv.dto.ValidationResult;
import com.inhand.agv.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    public TableDataInfo listTasks(TaskFilter filter) {
        // 构建 JPA Specification 进行动态查询
        Specification<AgvTask> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 过滤未被逻辑删除的任务
            predicates.add(criteriaBuilder.equal(root.get("deleteFlag"), false));

            if (StringUtils.hasText(filter.getTaskCode())) {
                predicates.add(criteriaBuilder.like(root.get("taskCode"), "%" + filter.getTaskCode() + "%"));
            }
            if (StringUtils.hasText(filter.getCreator())) {
                predicates.add(criteriaBuilder.equal(root.get("creator"), filter.getCreator()));
            }
            if (StringUtils.hasText(filter.getExecutor())) {
                predicates.add(criteriaBuilder.equal(root.get("executor"), filter.getExecutor()));
            }
            if (StringUtils.hasText(filter.getTaskStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("taskStatus"), filter.getTaskStatus()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 处理分页信息
        Integer pageNum = filter.getPageNum();
        Integer pageSize = filter.getPageSize();

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        // PageRequest 的页码从 0 开始，所以需要 pageNum - 1
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<AgvTask> pagedResult = taskRepository.findAll(spec, pageable);

        return new TableDataInfo(pagedResult.getTotalElements(), pagedResult.getContent());
    }

    public AgvTask getTask(Long id) {
        // findById返回Optional，需要处理可能不存在的情况
        return taskRepository.findById(id).orElse(null);
    }

    public AgvTask addTask(TaskDTO taskDTO) {
        ValidationResult validation = validateTask(taskDTO); // 必填字段验证
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getMessage());
        }

        if (taskRepository.findByTaskCode(taskDTO.getTaskCode()).isPresent()) {
            throw new IllegalArgumentException("任务编号 '" + taskDTO.getTaskCode() + "' 已存在，请使用其他编号。");
        }

        AgvTask task = new AgvTask();
        // 设置DTO中的字段
        task.setTaskCode(taskDTO.getTaskCode());
        task.setTaskName(taskDTO.getTaskName());
        task.setStartPos(taskDTO.getStartPos());
        task.setTaskTrip(taskDTO.getTaskTrip());
        task.setCreator(taskDTO.getCreator());
        task.setExecutor(taskDTO.getExecutor());

        task.setCreateTime(new Date()); // 设置创建时间
        task.setTaskStatus("待巡视"); // 新增任务默认状态
        task.setUploaded(false); // 默认未上传
        task.setDeleteFlag(false); // 默认未删除

        // 保存后，JPA会返回带有生成ID的实体
        AgvTask savedTask = taskRepository.save(task);

        // 如果任务编号依赖于ID，可以在保存后再次更新并保存
        if (savedTask.getTaskCode() == null || savedTask.getTaskCode().isEmpty()) {
            savedTask.setTaskCode("TASK-" + String.format("%04d", savedTask.getId()));
            taskRepository.save(savedTask); // 再次保存以更新任务编号
        }

        return savedTask;
    }

    public AgvTask updateTask(TaskDTO taskDTO) {
        if (taskDTO.getId() == null) {
            throw new IllegalArgumentException("更新任务时ID不能为空");
        }

        // 使用Optional来避免空指针异常
        Optional<AgvTask> existingTaskOptional = taskRepository.findById(taskDTO.getId());
        if (!existingTaskOptional.isPresent()) {
            throw new IllegalArgumentException("任务不存在");
        }
        AgvTask existingTask = existingTaskOptional.get();

        ValidationResult validation = validateTask(taskDTO); // 必填字段验证
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getMessage());
        }

        if (!existingTask.getTaskCode().equals(taskDTO.getTaskCode())) {
            // 如果任务编号被修改，则检查新的任务编号是否被除当前任务外的其他任务占用
            Optional<AgvTask> conflictTask = taskRepository.findByTaskCode(taskDTO.getTaskCode());
            if (conflictTask.isPresent() && !conflictTask.get().getId().equals(existingTask.getId())) {
                throw new IllegalArgumentException("任务编号 '" + taskDTO.getTaskCode() + "' 已被其他任务使用，请使用其他编号。");
            }
        }

        // 只能修改待巡视状态的任务
        if (!"待巡视".equals(existingTask.getTaskStatus())) {
            throw new IllegalArgumentException("只有待巡视状态的任务可以修改");
        }

        existingTask.setTaskCode(taskDTO.getTaskCode());
        existingTask.setTaskName(taskDTO.getTaskName());
        existingTask.setStartPos(taskDTO.getStartPos());
        existingTask.setTaskTrip(taskDTO.getTaskTrip());
        existingTask.setCreator(taskDTO.getCreator());
        existingTask.setExecutor(taskDTO.getExecutor());

        // 保存更新，JPA会根据ID判断是更新操作
        return taskRepository.save(existingTask);
    }

    public void delTask(Long id) {
        Optional<AgvTask> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            throw new IllegalArgumentException("任务不存在");
        }
        AgvTask task = taskOptional.get();

        // 只能删除待巡视状态的任务
        if (!"待巡视".equals(task.getTaskStatus())) {
            throw new IllegalArgumentException("只有待巡视状态的任务可以删除");
        }

        // 软删除，通过设置deleteFlag为true实现
        task.setDeleteFlag(true);
        taskRepository.save(task); // 保存更新后的任务状态
    }

    public AgvTask startTask(Long id) {
        Optional<AgvTask> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            throw new IllegalArgumentException("任务不存在");
        }
        AgvTask task = taskOptional.get();

        if (!"待巡视".equals(task.getTaskStatus())) {
            throw new IllegalArgumentException("只有待巡视状态的任务才能启动");
        }
        task.setTaskStatus("巡视中");
        task.setExecTime(new Date());
        return taskRepository.save(task);
    }

    public AgvTask endTask(Long id) {
        Optional<AgvTask> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            throw new IllegalArgumentException("任务不存在");
        }
        AgvTask task = taskOptional.get();

        if (!"巡视中".equals(task.getTaskStatus())) {
            throw new IllegalArgumentException("只有巡视中的任务才能停止");
        }
        task.setTaskStatus("待上传"); // 结束后通常进入待上传状态
        task.setEndTime(new Date()); // 设置结束时间
        return taskRepository.save(task);
    }

    public AgvTask preUploadTask(Long id) {
        Optional<AgvTask> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            throw new IllegalArgumentException("任务不存在");
        }
        AgvTask task = taskOptional.get();

        if (!"待上传".equals(task.getTaskStatus())) {
            throw new IllegalArgumentException("只有待上传状态的任务才能进行查询操作");
        }

        return taskRepository.save(task);
    }

    public AgvTask uploadTask(Long id) {
        Optional<AgvTask> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            throw new IllegalArgumentException("任务不存在");
        }
        AgvTask task = taskOptional.get();

        if (!"待上传".equals(task.getTaskStatus())) {
            throw new IllegalArgumentException("只有待上传状态的任务才能上传");
        }
        task.setUploaded(true);
        task.setTaskStatus("已完成");
        return taskRepository.save(task);
    }

    public ValidationResult validateTask(TaskDTO taskDTO) {
        if (taskDTO.getTaskCode() == null || taskDTO.getTaskCode().trim().isEmpty()) {
            return new ValidationResult(false, "任务编号不能为空");
        }
        if (taskDTO.getTaskName() == null || taskDTO.getTaskName().trim().isEmpty()) {
            return new ValidationResult(false, "任务名称不能为空");
        }
        if (taskDTO.getStartPos() == null || taskDTO.getStartPos().trim().isEmpty()) {
            return new ValidationResult(false, "起始地点不能为空");
        }
        if (taskDTO.getTaskTrip() == null || taskDTO.getTaskTrip().trim().isEmpty()) {
            return new ValidationResult(false, "任务距离不能为空");
        }
        if (taskDTO.getCreator() == null || taskDTO.getCreator().trim().isEmpty()) {
            return new ValidationResult(false, "创建人不能为空");
        }
        if (taskDTO.getExecutor() == null || taskDTO.getExecutor().trim().isEmpty()) {
            return new ValidationResult(false, "执行人不能为空");
        }
        return new ValidationResult(true, "验证通过");
    }
}