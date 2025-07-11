package com.inhand.agv.service;

import com.inhand.agv.common.TableDataInfo;
import com.inhand.agv.entity.AgvTask;
import com.inhand.agv.dto.TaskDTO;
import com.inhand.agv.dto.TaskFilter;
import com.inhand.agv.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository; // 模拟Repository

    @InjectMocks
    private TaskService taskService; // 注入真实的Service实例

    private AgvTask sampleTask;
    private TaskDTO sampleTaskDTO;

    @BeforeEach
    void setUp() {
        sampleTask = new AgvTask();
        sampleTask.setId(1L);
        sampleTask.setTaskCode("TASK-001");
        sampleTask.setTaskName("测试任务");
        sampleTask.setStartPos("起点");
        sampleTask.setTaskTrip("100m");
        sampleTask.setCreator("测试创建人");
        sampleTask.setExecutor("测试执行人");
        sampleTask.setTaskStatus("待巡视");
        sampleTask.setUploaded(false);
        sampleTask.setDeleteFlag(false);
        sampleTask.setCreateTime(new Date());

        sampleTaskDTO = new TaskDTO();
        sampleTaskDTO.setId(1L);
        sampleTaskDTO.setTaskCode("TASK-001");
        sampleTaskDTO.setTaskName("测试任务");
        sampleTaskDTO.setStartPos("起点");
        sampleTaskDTO.setTaskTrip("100m");
        sampleTaskDTO.setCreator("测试创建人");
        sampleTaskDTO.setExecutor("测试执行人");
    }

    @Test
    void listTasksWithNoFilter() {
        List<AgvTask> tasks = Arrays.asList(sampleTask, new AgvTask());
        Page<AgvTask> page = new PageImpl<>(tasks);

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        TaskFilter filter = new TaskFilter();
        TableDataInfo result = taskService.listTasks(filter);

        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRows().size());
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void listTasksWithAllFilters() {
        TaskFilter filter = new TaskFilter();
        filter.setTaskCode("TASK-001");
        filter.setCreator("测试创建人");
        filter.setExecutor("测试执行人");
        filter.setTaskStatus("待巡视");
        filter.setPageNum(1);
        filter.setPageSize(10);

        List<AgvTask> tasks = Collections.singletonList(sampleTask);
        Page<AgvTask> page = new PageImpl<>(tasks, PageRequest.of(0, 10), 1);

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        TableDataInfo result = taskService.listTasks(filter);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRows().size());
        assertEquals("TASK-001", ((AgvTask) result.getRows().get(0)).getTaskCode());
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getTaskSuccess() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        AgvTask result = taskService.getTask(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        AgvTask result = taskService.getTask(99L);
        assertNull(result);
        verify(taskRepository, times(1)).findById(99L);
    }

    @Test
    void addTaskSuccess() {
        // 由于addTask会生成taskCode，所以模拟保存时返回带有ID和生成taskCode的task
        AgvTask savedTask = new AgvTask();
        savedTask.setId(2L);
        savedTask.setTaskCode("TASK-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())); // 模拟生成
        savedTask.setTaskName(sampleTaskDTO.getTaskName());
        savedTask.setStartPos(sampleTaskDTO.getStartPos());
        savedTask.setTaskTrip(sampleTaskDTO.getTaskTrip());
        savedTask.setCreator(sampleTaskDTO.getCreator());
        savedTask.setExecutor(sampleTaskDTO.getExecutor());
        savedTask.setTaskStatus("待巡视");
        savedTask.setUploaded(false);
        savedTask.setDeleteFlag(false);
        savedTask.setCreateTime(new Date());

        when(taskRepository.save(any(AgvTask.class))).thenReturn(savedTask);
        // 模拟findByTaskCode，因为addTask里有被注释的taskCode重复校验，虽然注释掉了，但为了防止未来取消注释导致未覆盖，这里模拟它不存在
        when(taskRepository.findByTaskCode(anyString())).thenReturn(Optional.empty());


        AgvTask result = taskService.addTask(sampleTaskDTO);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("待巡视", result.getTaskStatus());
        assertFalse(result.getUploaded());
        assertFalse(result.getDeleteFlag());
        assertNotNull(result.getTaskCode()); // 验证taskCode被生成
        verify(taskRepository, times(1)).save(any(AgvTask.class));
    }

    // validateTask的失败路径，通过addTask触发
    @Test
    void addTask_MissingTaskCode() {
        sampleTaskDTO.setTaskCode("");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addTask(sampleTaskDTO);
        });
        assertEquals("任务编号不能为空", thrown.getMessage());
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void addTask_MissingTaskName() {
        sampleTaskDTO.setTaskName("");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addTask(sampleTaskDTO);
        });
        assertEquals("任务名称不能为空", thrown.getMessage());
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void addTask_MissingStartPos() {
        sampleTaskDTO.setStartPos("");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addTask(sampleTaskDTO);
        });
        assertEquals("起始地点不能为空", thrown.getMessage());
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void addTask_MissingTaskTrip() {
        sampleTaskDTO.setTaskTrip("");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addTask(sampleTaskDTO);
        });
        assertEquals("任务距离不能为空", thrown.getMessage());
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void addTask_MissingCreator() {
        sampleTaskDTO.setCreator("");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addTask(sampleTaskDTO);
        });
        assertEquals("创建人不能为空", thrown.getMessage());
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void addTask_MissingExecutor() {
        sampleTaskDTO.setExecutor("");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addTask(sampleTaskDTO);
        });
        assertEquals("执行人不能为空", thrown.getMessage());
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void addTask_TaskCodeAlreadyExists() {

        TaskDTO existingTaskDTO = new TaskDTO();
        existingTaskDTO.setTaskCode("EXISTING-TASK-CODE");
        existingTaskDTO.setTaskName("Existing Task Name");
        existingTaskDTO.setStartPos("Start");
        existingTaskDTO.setTaskTrip("Trip");
        existingTaskDTO.setCreator("Creator");
        existingTaskDTO.setExecutor("Executor");

        AgvTask existingAgvTask = new AgvTask();
        existingAgvTask.setTaskCode("EXISTING-TASK-CODE");
        when(taskRepository.findByTaskCode(existingTaskDTO.getTaskCode())).thenReturn(Optional.of(existingAgvTask));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.addTask(existingTaskDTO);
        });
        assertEquals("任务编号 'EXISTING-TASK-CODE' 已存在，请使用其他编号。", thrown.getMessage());
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void updateTaskSuccess() {
        AgvTask existingTask = new AgvTask();
        existingTask.setId(1L);
        existingTask.setTaskCode("OLD-CODE");
        existingTask.setTaskName("旧任务名");
        existingTask.setTaskStatus("待巡视"); // 待巡视状态才能修改
        existingTask.setDeleteFlag(false); // 未删除

        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setId(1L);
        updateDTO.setTaskCode("NEW-CODE"); // 模拟修改任务编号
        updateDTO.setTaskName("新任务名");
        updateDTO.setStartPos("更新起点");
        updateDTO.setTaskTrip("200m");
        updateDTO.setCreator("更新创建人");
        updateDTO.setExecutor("更新执行人");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.findByTaskCode("NEW-CODE")).thenReturn(Optional.empty()); // 新编号不存在冲突
        when(taskRepository.save(any(AgvTask.class))).thenAnswer(invocation -> invocation.getArgument(0)); // 返回传入的参数

        AgvTask result = taskService.updateTask(updateDTO);

        assertNotNull(result);
        assertEquals("NEW-CODE", result.getTaskCode());
        assertEquals("新任务名", result.getTaskName());
        assertEquals("更新起点", result.getStartPos());
        assertEquals("200m", result.getTaskTrip());
        assertEquals("更新创建人", result.getCreator());
        assertEquals("更新执行人", result.getExecutor());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).findByTaskCode("NEW-CODE");
        verify(taskRepository, times(1)).save(any(AgvTask.class));
    }

    @Test
    void updateTask_NullId() {
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTaskCode("TASK-001");
        updateDTO.setTaskName("Task Name");
        // ID为null
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(updateDTO);
        });
        assertEquals("更新任务时ID不能为空", thrown.getMessage());
        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void updateTask_NotFound() {
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setId(99L);
        updateDTO.setTaskCode("TASK-001");
        updateDTO.setTaskName("Task Name");

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(updateDTO);
        });
        assertEquals("任务不存在", thrown.getMessage());
        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void updateTask_TaskCodeConflict() {
        AgvTask existingTask = new AgvTask();
        existingTask.setId(1L);
        existingTask.setTaskCode("OLD-CODE");
        existingTask.setTaskStatus("待巡视");

        AgvTask conflictTask = new AgvTask();
        conflictTask.setId(2L); // 不同的ID
        conflictTask.setTaskCode("CONFLICT-CODE");

        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setId(1L);
        updateDTO.setTaskCode("CONFLICT-CODE"); // 修改为冲突的编号
        updateDTO.setTaskName("Updated Task");
        updateDTO.setStartPos("Start Pos");
        updateDTO.setTaskTrip("Task Trip");
        updateDTO.setCreator("Creator");
        updateDTO.setExecutor("Executor");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.findByTaskCode("CONFLICT-CODE")).thenReturn(Optional.of(conflictTask)); // 模拟冲突

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(updateDTO);
        });
        assertEquals("任务编号 'CONFLICT-CODE' 已被其他任务使用，请使用其他编号。", thrown.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).findByTaskCode("CONFLICT-CODE");
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void updateTask_InvalidStatus() {
        AgvTask existingTask = new AgvTask();
        existingTask.setId(1L);
        existingTask.setTaskCode("TASK-001");
        existingTask.setTaskName("旧任务名");
        existingTask.setTaskStatus("巡视中"); // 非待巡视状态

        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setId(1L);
        updateDTO.setTaskCode("TASK-001");
        updateDTO.setTaskName("新任务名");
        updateDTO.setStartPos("更新起点");
        updateDTO.setTaskTrip("200m");
        updateDTO.setCreator("更新创建人");
        updateDTO.setExecutor("更新执行人");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(updateDTO);
        });
        assertEquals("只有待巡视状态的任务可以修改", thrown.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).findByTaskCode(anyString());
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    // validateTask的失败路径，通过updateTask触发
    @Test
    void updateTask_MissingTaskName() {
        AgvTask existingTask = new AgvTask();
        existingTask.setId(1L);
        existingTask.setTaskCode("OLD-CODE");
        existingTask.setTaskName("旧任务名");
        existingTask.setTaskStatus("待巡视"); // 待巡视状态才能修改

        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setId(1L);
        updateDTO.setTaskCode("NEW-CODE");
        updateDTO.setTaskName(""); // 任务名称为空

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(updateDTO);
        });
        assertEquals("任务名称不能为空", thrown.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).findByTaskCode(anyString());
        verify(taskRepository, never()).save(any(AgvTask.class));
    }


    @Test
    void delTaskSuccess() {
        AgvTask taskToDelete = new AgvTask();
        taskToDelete.setId(1L);
        taskToDelete.setTaskStatus("待巡视"); // 待巡视状态才能删除
        taskToDelete.setDeleteFlag(false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToDelete));
        when(taskRepository.save(any(AgvTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        taskService.delTask(1L);

        assertTrue(taskToDelete.getDeleteFlag()); // 验证deleteFlag被设置为true
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(taskToDelete);
    }

    @Test
    void delTask_NotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.delTask(99L);
        });
        assertEquals("任务不存在", thrown.getMessage());
        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void delTask_InvalidStatus() {
        AgvTask taskToDelete = new AgvTask();
        taskToDelete.setId(1L);
        taskToDelete.setTaskStatus("巡视中"); // 非待巡视状态

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToDelete));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.delTask(1L);
        });
        assertEquals("只有待巡视状态的任务可以删除", thrown.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void startTaskSuccess() {
        AgvTask taskToStart = new AgvTask();
        taskToStart.setId(1L);
        taskToStart.setTaskStatus("待巡视");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToStart));
        when(taskRepository.save(any(AgvTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgvTask result = taskService.startTask(1L);

        assertNotNull(result);
        assertEquals("巡视中", result.getTaskStatus());
        assertNotNull(result.getExecTime());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(taskToStart);
    }

    @Test
    void startTask_NotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.startTask(99L);
        });
        assertEquals("任务不存在", thrown.getMessage());
        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void startTask_InvalidStatus() {
        AgvTask taskToStart = new AgvTask();
        taskToStart.setId(1L);
        taskToStart.setTaskStatus("巡视中"); // 非待巡视状态

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToStart));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.startTask(1L);
        });
        assertEquals("只有待巡视状态的任务才能启动", thrown.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void endTaskSuccess() {
        AgvTask taskToEnd = new AgvTask();
        taskToEnd.setId(1L);
        taskToEnd.setTaskStatus("巡视中");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToEnd));
        when(taskRepository.save(any(AgvTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgvTask result = taskService.endTask(1L);

        assertNotNull(result);
        assertEquals("待上传", result.getTaskStatus());
        assertNotNull(result.getEndTime());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(taskToEnd);
    }

    @Test
    void endTask_NotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.endTask(99L);
        });
        assertEquals("任务不存在", thrown.getMessage());
        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void endTask_InvalidStatus() {
        AgvTask taskToEnd = new AgvTask();
        taskToEnd.setId(1L);
        taskToEnd.setTaskStatus("待巡视"); // 非巡视中状态

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToEnd));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.endTask(1L);
        });
        assertEquals("只有巡视中的任务才能停止", thrown.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void preUploadTaskSuccess() {
        AgvTask taskToPreUpload = new AgvTask();
        taskToPreUpload.setId(1L);
        taskToPreUpload.setTaskStatus("待上传");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToPreUpload));
        when(taskRepository.save(any(AgvTask.class))).thenAnswer(invocation -> invocation.getArgument(0)); // 尽管preUploadTask没有修改，但为了覆盖save调用

        AgvTask result = taskService.preUploadTask(1L);

        assertNotNull(result);
        assertEquals("待上传", result.getTaskStatus());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(taskToPreUpload); // 确认save被调用，即使没修改
    }

    @Test
    void preUploadTask_NotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.preUploadTask(99L);
        });
        assertEquals("任务不存在", thrown.getMessage());
        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void preUploadTask_InvalidStatus() {
        AgvTask taskToPreUpload = new AgvTask();
        taskToPreUpload.setId(1L);
        taskToPreUpload.setTaskStatus("已完成"); // 非待上传状态

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToPreUpload));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.preUploadTask(1L);
        });
        assertEquals("只有待上传状态的任务才能进行查询操作", thrown.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void uploadTaskSuccess() {
        AgvTask taskToUpload = new AgvTask();
        taskToUpload.setId(1L);
        taskToUpload.setTaskStatus("待上传");
        taskToUpload.setUploaded(false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToUpload));
        when(taskRepository.save(any(AgvTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgvTask result = taskService.uploadTask(1L);

        assertNotNull(result);
        assertEquals("已完成", result.getTaskStatus());
        assertTrue(result.getUploaded());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(taskToUpload);
    }

    @Test
    void uploadTask_NotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.uploadTask(99L);
        });
        assertEquals("任务不存在", thrown.getMessage());
        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }

    @Test
    void uploadTask_InvalidStatus() {
        AgvTask taskToUpload = new AgvTask();
        taskToUpload.setId(1L);
        taskToUpload.setTaskStatus("巡视中"); // 非待上传状态

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToUpload));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            taskService.uploadTask(1L);
        });
        assertEquals("只有待上传状态的任务才能上传", thrown.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(AgvTask.class));
    }
}