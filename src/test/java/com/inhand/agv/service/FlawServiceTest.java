package com.inhand.agv.service;

import com.inhand.agv.common.TableDataInfo;
import com.inhand.agv.dto.FlawDTO;
import com.inhand.agv.dto.FlawFilter;
import com.inhand.agv.entity.AgvFlaw;
import com.inhand.agv.repository.FlawRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FlawServiceTest {

    @Mock
    private FlawRepository flawRepository;

    @InjectMocks
    private FlawService flawService;

    private AgvFlaw sampleFlaw;
    private FlawDTO sampleFlawDTO;

    @BeforeEach
    void setUp() {
        sampleFlaw = new AgvFlaw();
        sampleFlaw.setId(1L);
        sampleFlaw.setDefectCode("DF-001");
        sampleFlaw.setTaskId(100L);
        sampleFlaw.setFlawType("划痕");
        sampleFlaw.setFlawName("车身划痕");
        sampleFlaw.setConfirmed(false);
        sampleFlaw.setUploaded(false);
        sampleFlaw.setLevel("高");
        sampleFlaw.setDeleteFlag(false);
        sampleFlaw.setCreateTime(new Date());

        sampleFlawDTO = new FlawDTO();
        sampleFlawDTO.setId(1L);
        sampleFlawDTO.setDefectCode("DF-001");
        sampleFlawDTO.setTaskId(100L);
        sampleFlawDTO.setFlawType("划痕");
        sampleFlawDTO.setFlawName("车身划痕");
        sampleFlawDTO.setFlawDesc("左侧车身有明显划痕");
        sampleFlawDTO.setLevel("高");
    }

    /**
     * 测试分页查询缺陷列表 - 所有过滤条件
     */
    @Test
    void listFlaws_WithAllFilters() {
        FlawFilter filter = new FlawFilter();
        filter.setDefectCode("DF-001");
        filter.setTaskId(100L);
        filter.setFlawType("划痕");
        filter.setFlawName("车身");
        filter.setConfirmed(false);
        filter.setUploaded(false);
        filter.setLevel("高");
        filter.setPageNum(1);
        filter.setPageSize(10);

        List<AgvFlaw> flaws = Collections.singletonList(sampleFlaw);
        Page<AgvFlaw> page = new PageImpl<>(flaws, PageRequest.of(0, 10), 1);

        when(flawRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        TableDataInfo result = flawService.listFlaws(filter);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRows().size());
        assertEquals("DF-001", ((AgvFlaw) result.getRows().get(0)).getDefectCode());
        verify(flawRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * 测试分页查询缺陷列表 - 没有找到缺陷
     */
    @Test
    void listFlaws_NoFlawsFound() {
        Page<AgvFlaw> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(flawRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        FlawFilter filter = new FlawFilter();
        filter.setPageNum(1);
        filter.setPageSize(10);

        TableDataInfo result = flawService.listFlaws(filter);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRows().isEmpty());
        verify(flawRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * 测试根据ID获取缺陷 - 缺陷存在且未被删除
     */
    @Test
    void getFlaw_ExistsAndNotDeleted_ReturnsFlaw() {
        when(flawRepository.findById(1L)).thenReturn(Optional.of(sampleFlaw));
        AgvFlaw result = flawService.getFlaw(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("DF-001", result.getDefectCode());
        verify(flawRepository, times(1)).findById(1L);
    }

    /**
     * 测试根据ID获取缺陷 - 缺陷不存在
     */
    @Test
    void getFlaw_NotExists_ReturnsNull() {
        when(flawRepository.findById(99L)).thenReturn(Optional.empty());
        AgvFlaw result = flawService.getFlaw(99L);
        assertNull(result);
        verify(flawRepository, times(1)).findById(99L);
    }

    /**
     * 测试根据ID获取缺陷 - 缺陷存在但已被逻辑删除
     */
    @Test
    void getFlaw_ExistsButDeleted_ReturnsNull() {
        AgvFlaw deletedFlaw = new AgvFlaw();
        deletedFlaw.setId(2L);
        deletedFlaw.setDefectCode("DF-002");
        deletedFlaw.setDeleteFlag(true); // 已被逻辑删除

        when(flawRepository.findById(2L)).thenReturn(Optional.of(deletedFlaw));
        AgvFlaw result = flawService.getFlaw(2L);
        assertNull(result);
        verify(flawRepository, times(1)).findById(2L);
    }

    /**
     * 测试添加新缺陷 - 成功
     */
    @Test
    void addFlaw_Success() {
        AgvFlaw savedFlaw = new AgvFlaw();
        savedFlaw.setId(2L);
        savedFlaw.setDefectCode(sampleFlawDTO.getDefectCode());
        savedFlaw.setTaskId(sampleFlawDTO.getTaskId());
        savedFlaw.setFlawType(sampleFlawDTO.getFlawType());
        savedFlaw.setFlawName(sampleFlawDTO.getFlawName());
        savedFlaw.setCreateTime(new Date());
        savedFlaw.setDeleteFlag(false);
        savedFlaw.setShown(false);
        savedFlaw.setConfirmed(false);
        savedFlaw.setUploaded(false);

        when(flawRepository.findByDefectCode(anyString())).thenReturn(Optional.empty()); // 模拟缺陷编号不存在
        when(flawRepository.save(any(AgvFlaw.class))).thenReturn(savedFlaw);

        AgvFlaw result = flawService.addFlaw(sampleFlawDTO);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(sampleFlawDTO.getDefectCode(), result.getDefectCode());
        assertFalse(result.getDeleteFlag());
        assertFalse(result.getShown());
        assertFalse(result.getConfirmed());
        assertFalse(result.getUploaded());
        verify(flawRepository, times(1)).findByDefectCode(sampleFlawDTO.getDefectCode());
        verify(flawRepository, times(1)).save(any(AgvFlaw.class));
    }

    /**
     * 测试添加新缺陷 - 缺陷编号已存在
     */
    @Test
    void addFlaw_DefectCodeExists_ThrowsException() {
        when(flawRepository.findByDefectCode(sampleFlawDTO.getDefectCode()))
                .thenReturn(Optional.of(sampleFlaw)); // 模拟已存在且未删除的缺陷

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.addFlaw(sampleFlawDTO);
        });
        assertEquals("缺陷编号已存在", thrown.getMessage());
        verify(flawRepository, times(1)).findByDefectCode(sampleFlawDTO.getDefectCode());
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试添加新缺陷 - 缺陷编号不能为空
     */
    @Test
    void addFlaw_MissingDefectCode_ThrowsException() {
        sampleFlawDTO.setDefectCode("");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.addFlaw(sampleFlawDTO);
        });
        assertEquals("缺陷编号不能为空", thrown.getMessage());
        verify(flawRepository, never()).findByDefectCode(anyString());
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试添加新缺陷 - 所属任务ID不能为空
     */
    @Test
    void addFlaw_MissingTaskId_ThrowsException() {
        sampleFlawDTO.setTaskId(null);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.addFlaw(sampleFlawDTO);
        });
        assertEquals("所属任务ID不能为空", thrown.getMessage());
        verify(flawRepository, never()).findByDefectCode(anyString());
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试添加新缺陷 - 缺陷类型不能为空
     */
    @Test
    void addFlaw_MissingFlawType_ThrowsException() {
        sampleFlawDTO.setFlawType("");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.addFlaw(sampleFlawDTO);
        });
        assertEquals("缺陷类型不能为空", thrown.getMessage());
        verify(flawRepository, never()).findByDefectCode(anyString());
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试添加新缺陷 - 缺陷名称不能为空
     */
    @Test
    void addFlaw_MissingFlawName_ThrowsException() {
        sampleFlawDTO.setFlawName("");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.addFlaw(sampleFlawDTO);
        });
        assertEquals("缺陷名称不能为空", thrown.getMessage());
        verify(flawRepository, never()).findByDefectCode(anyString());
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }


    /**
     * 测试更新缺陷信息 - 成功，无缺陷编号变更
     */
    @Test
    void updateFlaw_Success_NoDefectCodeChange() {
        AgvFlaw existingFlaw = new AgvFlaw();
        existingFlaw.setId(1L);
        existingFlaw.setDefectCode("DF-001"); // 保持不变
        existingFlaw.setFlawName("旧缺陷名");
        existingFlaw.setDeleteFlag(false);

        FlawDTO updateDTO = new FlawDTO();
        updateDTO.setId(1L);
        updateDTO.setDefectCode("DF-001");
        updateDTO.setTaskId(100L);
        updateDTO.setFlawType("划痕");
        updateDTO.setFlawName("新缺陷名");
        updateDTO.setFlawDesc("更新后的描述");
        updateDTO.setLevel("低");

        when(flawRepository.findById(1L)).thenReturn(Optional.of(existingFlaw));
        when(flawRepository.save(any(AgvFlaw.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgvFlaw result = flawService.updateFlaw(updateDTO);

        assertNotNull(result);
        assertEquals("新缺陷名", result.getFlawName());
        assertEquals("低", result.getLevel());
        verify(flawRepository, times(1)).findById(1L);
        verify(flawRepository, never()).findByDefectCode(anyString()); // 没有改变defectCode，不应调用
        verify(flawRepository, times(1)).save(any(AgvFlaw.class));
    }

    /**
     * 测试更新缺陷信息 - 成功，缺陷编号变更且唯一
     */
    @Test
    void updateFlaw_Success_DefectCodeChangeToUnique() {
        AgvFlaw existingFlaw = new AgvFlaw();
        existingFlaw.setId(1L);
        existingFlaw.setDefectCode("OLD-DF");
        existingFlaw.setFlawName("旧缺陷名");
        existingFlaw.setDeleteFlag(false);

        FlawDTO updateDTO = new FlawDTO();
        updateDTO.setId(1L);
        updateDTO.setDefectCode("NEW-DF"); // 变更缺陷编号
        updateDTO.setTaskId(100L);
        updateDTO.setFlawType("划痕");
        updateDTO.setFlawName("新缺陷名");
        updateDTO.setFlawDesc("更新后的描述");
        updateDTO.setLevel("低");

        when(flawRepository.findById(1L)).thenReturn(Optional.of(existingFlaw));
        when(flawRepository.findByDefectCode("NEW-DF")).thenReturn(Optional.empty()); // 新编号不存在
        when(flawRepository.save(any(AgvFlaw.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgvFlaw result = flawService.updateFlaw(updateDTO);

        assertNotNull(result);
        assertEquals("NEW-DF", result.getDefectCode());
        assertEquals("新缺陷名", result.getFlawName());
        verify(flawRepository, times(1)).findById(1L);
        verify(flawRepository, times(1)).findByDefectCode("NEW-DF");
        verify(flawRepository, times(1)).save(any(AgvFlaw.class));
    }

    /**
     * 测试更新缺陷信息 - ID为空
     */
    @Test
    void updateFlaw_NullId_ThrowsException() {
        sampleFlawDTO.setId(null); // ID为空
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.updateFlaw(sampleFlawDTO);
        });
        assertEquals("缺陷ID不能为空", thrown.getMessage());
        verify(flawRepository, never()).findById(anyLong());
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试更新缺陷信息 - 缺陷不存在
     */
    @Test
    void updateFlaw_NotFound_ThrowsException() {
        when(flawRepository.findById(99L)).thenReturn(Optional.empty()); // 不存在

        sampleFlawDTO.setId(99L);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.updateFlaw(sampleFlawDTO);
        });
        assertEquals("缺陷不存在或已被删除", thrown.getMessage());
        verify(flawRepository, times(1)).findById(99L);
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试更新缺陷信息 - 缺陷已被逻辑删除
     */
    @Test
    void updateFlaw_DeletedFlaw_ThrowsException() {
        AgvFlaw deletedFlaw = new AgvFlaw();
        deletedFlaw.setId(1L);
        deletedFlaw.setDeleteFlag(true); // 已被逻辑删除

        when(flawRepository.findById(1L)).thenReturn(Optional.of(deletedFlaw));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.updateFlaw(sampleFlawDTO);
        });
        assertEquals("缺陷不存在或已被删除", thrown.getMessage());
        verify(flawRepository, times(1)).findById(1L);
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试更新缺陷信息 - 缺陷编号冲突
     */
    @Test
    void updateFlaw_DefectCodeConflict_ThrowsException() {
        AgvFlaw existingFlaw = new AgvFlaw();
        existingFlaw.setId(1L);
        existingFlaw.setDefectCode("OLD-CODE");
        existingFlaw.setDeleteFlag(false);

        AgvFlaw conflictFlaw = new AgvFlaw();
        conflictFlaw.setId(2L); // 不同的ID
        conflictFlaw.setDefectCode("CONFLICT-CODE");
        conflictFlaw.setDeleteFlag(false);

        sampleFlawDTO.setId(1L);
        sampleFlawDTO.setDefectCode("CONFLICT-CODE"); // 更改为冲突的编号

        when(flawRepository.findById(1L)).thenReturn(Optional.of(existingFlaw));
        when(flawRepository.findByDefectCode("CONFLICT-CODE")).thenReturn(Optional.of(conflictFlaw)); // 模拟冲突

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.updateFlaw(sampleFlawDTO);
        });
        assertEquals("缺陷编号已存在", thrown.getMessage());
        verify(flawRepository, times(1)).findById(1L);
        verify(flawRepository, times(1)).findByDefectCode("CONFLICT-CODE");
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试更新缺陷信息 - 缺陷名称不能为空（通过 validateFlaw）
     */
    @Test
    void updateFlaw_MissingFlawName_ThrowsException() {
        AgvFlaw existingFlaw = new AgvFlaw();
        existingFlaw.setId(1L);
        existingFlaw.setDefectCode("DF-001");
        existingFlaw.setDeleteFlag(false);

        sampleFlawDTO.setId(1L);
        sampleFlawDTO.setFlawName(""); // 缺陷名称为空

        when(flawRepository.findById(1L)).thenReturn(Optional.of(existingFlaw));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.updateFlaw(sampleFlawDTO);
        });
        assertEquals("缺陷名称不能为空", thrown.getMessage());
        verify(flawRepository, times(1)).findById(1L);
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试逻辑删除缺陷 - 成功
     */
    @Test
    void delFlaw_Success() {
        AgvFlaw flawToDelete = new AgvFlaw();
        flawToDelete.setId(1L);
        flawToDelete.setDeleteFlag(false);

        when(flawRepository.findById(1L)).thenReturn(Optional.of(flawToDelete));
        when(flawRepository.save(any(AgvFlaw.class))).thenAnswer(invocation -> invocation.getArgument(0));

        flawService.delFlaw(1L);

        assertTrue(flawToDelete.getDeleteFlag()); // 验证deleteFlag被设置为true
        verify(flawRepository, times(1)).findById(1L);
        verify(flawRepository, times(1)).save(flawToDelete);
    }

    /**
     * 测试逻辑删除缺陷 - 缺陷不存在
     */
    @Test
    void delFlaw_NotFound_ThrowsException() {
        when(flawRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.delFlaw(99L);
        });
        assertEquals("缺陷不存在或已被删除", thrown.getMessage());
        verify(flawRepository, times(1)).findById(99L);
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试逻辑删除缺陷 - 缺陷已是删除状态
     */
    @Test
    void delFlaw_AlreadyDeleted_ThrowsException() {
        AgvFlaw deletedFlaw = new AgvFlaw();
        deletedFlaw.setId(1L);
        deletedFlaw.setDeleteFlag(true);

        when(flawRepository.findById(1L)).thenReturn(Optional.of(deletedFlaw));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            flawService.delFlaw(1L);
        });
        assertEquals("缺陷不存在或已被删除", thrown.getMessage());
        verify(flawRepository, times(1)).findById(1L);
        verify(flawRepository, never()).save(any(AgvFlaw.class));
    }

    /**
     * 测试获取任务实时缺陷信息 - 成功获取
     */
    @Test
    void getLiveFlawsByTaskId_ReturnsLiveFlaws() {
        AgvFlaw liveFlaw1 = new AgvFlaw();
        liveFlaw1.setId(1L);
        liveFlaw1.setTaskId(100L);
        liveFlaw1.setConfirmed(false);
        liveFlaw1.setDeleteFlag(false);

        AgvFlaw liveFlaw2 = new AgvFlaw();
        liveFlaw2.setId(2L);
        liveFlaw2.setTaskId(100L);
        liveFlaw2.setConfirmed(false);
        liveFlaw2.setDeleteFlag(false);

        List<AgvFlaw> liveFlaws = Arrays.asList(liveFlaw1, liveFlaw2);
        when(flawRepository.findByTaskIdAndConfirmedAndDeleteFlag(100L, false, false))
                .thenReturn(liveFlaws);

        List<AgvFlaw> result = flawService.getLiveFlawsByTaskId(100L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(flawRepository, times(1)).findByTaskIdAndConfirmedAndDeleteFlag(100L, false, false);
    }

    /**
     * 测试获取任务实时缺陷信息 - 没有找到实时缺陷
     */
    @Test
    void getLiveFlawsByTaskId_NoLiveFlawsFound() {
        when(flawRepository.findByTaskIdAndConfirmedAndDeleteFlag(200L, false, false))
                .thenReturn(Collections.emptyList());

        List<AgvFlaw> result = flawService.getLiveFlawsByTaskId(200L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(flawRepository, times(1)).findByTaskIdAndConfirmedAndDeleteFlag(200L, false, false);
    }

    /**
     * 测试检查所有缺陷是否已确认 - 所有缺陷均已确认
     */
    @Test
    void checkAllFlawsConfirmed_AllConfirmed_ReturnsTrue() {
        AgvFlaw confirmedFlaw1 = new AgvFlaw();
        confirmedFlaw1.setId(1L);
        confirmedFlaw1.setTaskId(100L);
        confirmedFlaw1.setConfirmed(true);
        confirmedFlaw1.setDeleteFlag(false);

        AgvFlaw confirmedFlaw2 = new AgvFlaw();
        confirmedFlaw2.setId(2L);
        confirmedFlaw2.setTaskId(100L);
        confirmedFlaw2.setConfirmed(true);
        confirmedFlaw2.setDeleteFlag(false);

        List<AgvFlaw> flaws = Arrays.asList(confirmedFlaw1, confirmedFlaw2);
        when(flawRepository.findByTaskIdAndDeleteFlag(100L, false)).thenReturn(flaws);

        Boolean result = flawService.checkAllFlawsConfirmed(100L);

        assertTrue(result);
        verify(flawRepository, times(1)).findByTaskIdAndDeleteFlag(100L, false);
    }

    /**
     * 测试检查所有缺陷是否已确认 - 存在未确认的缺陷
     */
    @Test
    void checkAllFlawsConfirmed_SomeNotConfirmed_ReturnsFalse() {
        AgvFlaw confirmedFlaw = new AgvFlaw();
        confirmedFlaw.setId(1L);
        confirmedFlaw.setTaskId(100L);
        confirmedFlaw.setConfirmed(true);
        confirmedFlaw.setDeleteFlag(false);

        AgvFlaw unconfirmedFlaw = new AgvFlaw();
        unconfirmedFlaw.setId(2L);
        unconfirmedFlaw.setTaskId(100L);
        unconfirmedFlaw.setConfirmed(false); // 未确认
        unconfirmedFlaw.setDeleteFlag(false);

        List<AgvFlaw> flaws = Arrays.asList(confirmedFlaw, unconfirmedFlaw);
        when(flawRepository.findByTaskIdAndDeleteFlag(100L, false)).thenReturn(flaws);

        Boolean result = flawService.checkAllFlawsConfirmed(100L);

        assertFalse(result);
        verify(flawRepository, times(1)).findByTaskIdAndDeleteFlag(100L, false);
    }

    /**
     * 测试检查所有缺陷是否已确认 - 任务下没有缺陷
     */
    @Test
    void checkAllFlawsConfirmed_NoActiveFlaws_ReturnsTrue() {
        when(flawRepository.findByTaskIdAndDeleteFlag(300L, false)).thenReturn(Collections.emptyList());

        Boolean result = flawService.checkAllFlawsConfirmed(300L);

        assertTrue(result); // 没有缺陷，则认为所有缺陷都已确认（空集逻辑）
        verify(flawRepository, times(1)).findByTaskIdAndDeleteFlag(300L, false);
    }

    /**
     * 新增测试：测试 copyDtoToEntity 方法，确保所有字段都被覆盖
     */
    @Test
    void updateFlaw_AllDtoFieldsCovered_copyDtoToEntity() {
        AgvFlaw existingFlaw = new AgvFlaw();
        existingFlaw.setId(1L);
        existingFlaw.setDefectCode("ORIGINAL-DF");
        existingFlaw.setDeleteFlag(false);

        FlawDTO fullFlawDTO = new FlawDTO();
        fullFlawDTO.setId(1L);
        fullFlawDTO.setDefectCode("UPDATED-DF");
        fullFlawDTO.setTaskId(200L);
        fullFlawDTO.setRound(2);
        fullFlawDTO.setFlawType("TypeA");
        fullFlawDTO.setFlawName("NameA");
        fullFlawDTO.setFlawDesc("DescA");
        fullFlawDTO.setFlawDistance(10.5);
        fullFlawDTO.setFlawImage("image.jpg");
        fullFlawDTO.setFlawImageUrl("http://example.com/image.jpg");
        fullFlawDTO.setFlawRtsp("rtsp://example.com/stream");
        fullFlawDTO.setShown(true);
        fullFlawDTO.setConfirmed(true);
        fullFlawDTO.setUploaded(true);
        fullFlawDTO.setRemark("RemarkA");
        fullFlawDTO.setLevel("LevelA");

        when(flawRepository.findById(1L)).thenReturn(Optional.of(existingFlaw));
        when(flawRepository.findByDefectCode("UPDATED-DF")).thenReturn(Optional.empty()); // Assume new defect code is unique
        when(flawRepository.save(any(AgvFlaw.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgvFlaw result = flawService.updateFlaw(fullFlawDTO);

        assertNotNull(result);
        assertEquals(fullFlawDTO.getDefectCode(), result.getDefectCode());
        assertEquals(fullFlawDTO.getTaskId(), result.getTaskId());
        assertEquals(fullFlawDTO.getRound(), result.getRound());
        assertEquals(fullFlawDTO.getFlawType(), result.getFlawType());
        assertEquals(fullFlawDTO.getFlawName(), result.getFlawName());
        assertEquals(fullFlawDTO.getFlawDesc(), result.getFlawDesc());
        assertEquals(fullFlawDTO.getFlawDistance(), result.getFlawDistance());
        assertEquals(fullFlawDTO.getFlawImage(), result.getFlawImage());
        assertEquals(fullFlawDTO.getFlawImageUrl(), result.getFlawImageUrl());
        assertEquals(fullFlawDTO.getFlawRtsp(), result.getFlawRtsp());
        assertEquals(fullFlawDTO.getShown(), result.getShown());
        assertEquals(fullFlawDTO.getConfirmed(), result.getConfirmed());
        assertEquals(fullFlawDTO.getUploaded(), result.getUploaded());
        assertEquals(fullFlawDTO.getRemark(), result.getRemark());
        assertEquals(fullFlawDTO.getLevel(), result.getLevel());

        verify(flawRepository, times(1)).findById(1L);
        verify(flawRepository, times(1)).findByDefectCode("UPDATED-DF");
        verify(flawRepository, times(1)).save(any(AgvFlaw.class));
    }

}