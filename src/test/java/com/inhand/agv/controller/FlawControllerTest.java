package com.inhand.agv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inhand.agv.common.AjaxResult;
import com.inhand.agv.common.TableDataInfo;
import com.inhand.agv.dto.FlawDTO;
import com.inhand.agv.dto.FlawFilter;
import com.inhand.agv.entity.AgvFlaw;
import com.inhand.agv.service.FlawService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FlawControllerTest {

    @Mock
    private FlawService flawService;

    @InjectMocks
    private FlawController flawController;

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
     * 测试获取缺陷列表 - 成功
     */
    @Test
    void listFlaw_Success_ReturnsTableDataInfo() {
        // 准备测试数据
        FlawFilter filter = new FlawFilter();
        List<AgvFlaw> flawList = Collections.singletonList(sampleFlaw);
        TableDataInfo tableDataInfo = new TableDataInfo(flawList.size(), flawList);

        // Mock Service层行为
        when(flawService.listFlaws(any(FlawFilter.class))).thenReturn(tableDataInfo);

        // 直接调用Controller方法
        TableDataInfo result = flawController.listFlaw(filter);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertFalse(result.getRows().isEmpty());
        assertEquals("DF-001", ((AgvFlaw) result.getRows().get(0)).getDefectCode());

        // 验证Service方法是否被调用
        verify(flawService, times(1)).listFlaws(any(FlawFilter.class));
    }

    /**
     * 测试获取缺陷列表 - 无结果
     */
    @Test
    void listFlaw_NoResults_ReturnsEmptyTableDataInfo() {
        // 准备测试数据
        FlawFilter filter = new FlawFilter();
        List<AgvFlaw> emptyList = Collections.emptyList();
        TableDataInfo emptyTableDataInfo = new TableDataInfo(0, emptyList);

        // Mock Service层行为
        when(flawService.listFlaws(any(FlawFilter.class))).thenReturn(emptyTableDataInfo);

        // 直接调用Controller方法
        TableDataInfo result = flawController.listFlaw(filter);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRows().isEmpty());

        // 验证Service方法是否被调用
        verify(flawService, times(1)).listFlaws(any(FlawFilter.class));
    }

    /**
     * 测试获取缺陷列表 - 服务层抛出运行时异常 (直接调用Controller方法时，异常会直接抛出)
     */
    @Test
    void listFlaw_ServiceThrowsRuntimeException_ThrowsRuntimeException() {
        // 准备测试数据
        FlawFilter filter = new FlawFilter();

        // Mock Service层行为
        when(flawService.listFlaws(any(FlawFilter.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // 直接调用Controller方法并验证是否抛出异常
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            flawController.listFlaw(filter);
        });

        // 验证异常信息
        assertTrue(thrown.getMessage().contains("数据库连接失败"));

        // 验证Service方法是否被调用
        verify(flawService, times(1)).listFlaws(any(FlawFilter.class));
    }

    /**
     * 测试根据ID获取缺陷详情 - 成功
     */
    @Test
    void getFlaw_Success_ReturnsFlaw() {
        Long flawId = 1L;
        when(flawService.getFlaw(flawId)).thenReturn(sampleFlaw);

        AjaxResult result = flawController.getFlaw(flawId);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("删除任务成功", result.getMsg());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof AgvFlaw);
        assertEquals(flawId, ((AgvFlaw) result.getData()).getId());
        assertEquals("DF-001", ((AgvFlaw) result.getData()).getDefectCode());

        verify(flawService, times(1)).getFlaw(flawId);
    }

    /**
     * 测试根据ID获取缺陷详情 - 缺陷不存在
     */
    @Test
    void getFlaw_NotFound_ReturnsError() {
        Long flawId = 99L;
        when(flawService.getFlaw(flawId)).thenReturn(null);

        AjaxResult result = flawController.getFlaw(flawId);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("缺陷不存在或已被删除", result.getMsg());
        assertNull(result.getData());

        verify(flawService, times(1)).getFlaw(flawId);
    }

    /**
     * 测试添加缺陷 - 成功
     */
    @Test
    void addFlaw_Success() {
        when(flawService.addFlaw(any(FlawDTO.class))).thenReturn(sampleFlaw);

        AjaxResult result = flawController.addFlaw(sampleFlawDTO);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("创建缺陷成功", result.getMsg());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof AgvFlaw);
        assertEquals("DF-001", ((AgvFlaw) result.getData()).getDefectCode());

        verify(flawService, times(1)).addFlaw(any(FlawDTO.class));
    }

    /**
     * 测试添加缺陷 - 输入校验失败
     */
    @Test
    void addFlaw_InvalidInput_ReturnsError() {
        FlawDTO invalidDto = new FlawDTO();
        invalidDto.setTaskId(100L);
        invalidDto.setFlawName("Test Flaw");

        when(flawService.addFlaw(any(FlawDTO.class)))
                .thenThrow(new IllegalArgumentException("缺陷编号不能为空"));

        AjaxResult result = flawController.addFlaw(invalidDto);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("缺陷编号不能为空", result.getMsg());
        assertNull(result.getData());

        verify(flawService, times(1)).addFlaw(any(FlawDTO.class));
    }

    /**
     * 测试更新缺陷 - 成功
     */
    @Test
    void updateFlaw_Success() {
        when(flawService.updateFlaw(any(FlawDTO.class))).thenReturn(sampleFlaw);

        AjaxResult result = flawController.updateFlaw(sampleFlawDTO);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("更新缺陷成功", result.getMsg());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof AgvFlaw);
        assertEquals(1L, ((AgvFlaw) result.getData()).getId());

        verify(flawService, times(1)).updateFlaw(any(FlawDTO.class));
    }

    /**
     * 测试更新缺陷 - 输入校验失败或业务逻辑失败
     */
    @Test
    void updateFlaw_InvalidInput_ReturnsError() {
        FlawDTO invalidDto = new FlawDTO();
        invalidDto.setId(1L);
        invalidDto.setDefectCode("DF-001");
        invalidDto.setTaskId(100L);
        invalidDto.setFlawName("");

        when(flawService.updateFlaw(any(FlawDTO.class)))
                .thenThrow(new IllegalArgumentException("缺陷名称不能为空"));

        AjaxResult result = flawController.updateFlaw(invalidDto);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("缺陷名称不能为空", result.getMsg());
        assertNull(result.getData());

        verify(flawService, times(1)).updateFlaw(any(FlawDTO.class));
    }

    /**
     * 测试逻辑删除缺陷 - 成功
     */
    @Test
    void delFlaw_Success() {
        Long flawId = 1L;
        when(flawService.delFlaw(flawId)).thenReturn(sampleFlaw);

        AjaxResult result = flawController.delFlaw(flawId);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("删除缺陷成功", result.getMsg());
        assertNotNull(result.getData()); // delFlaw返回的是被删除的实体
        assertTrue(result.getData() instanceof AgvFlaw);
        assertEquals(flawId, ((AgvFlaw) result.getData()).getId());


        verify(flawService, times(1)).delFlaw(flawId);
    }

    /**
     * 测试逻辑删除缺陷 - 缺陷不存在或已删除
     */
    @Test
    void delFlaw_InvalidId_ReturnsError() {
        Long flawId = 99L;
        when(flawService.delFlaw(flawId))
                .thenThrow(new IllegalArgumentException("缺陷不存在或已被删除"));

        AjaxResult result = flawController.delFlaw(flawId);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("缺陷不存在或已被删除", result.getMsg());
        assertNull(result.getData());

        verify(flawService, times(1)).delFlaw(flawId);
    }

    /**
     * 测试获取任务实时缺陷信息 - 成功
     */
    @Test
    void liveInfo_Success_ReturnsLiveFlaws() {
        Long taskId = 100L;
        List<AgvFlaw> liveFlaws = Collections.singletonList(sampleFlaw);
        when(flawService.getLiveFlawsByTaskId(taskId)).thenReturn(liveFlaws);

        AjaxResult result = flawController.liveInfo(taskId);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("获取实时缺陷信息成功", result.getMsg());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof List);
        assertFalse(((List<?>) result.getData()).isEmpty());
        assertEquals(1L, ((AgvFlaw) ((List<?>) result.getData()).get(0)).getId());

        verify(flawService, times(1)).getLiveFlawsByTaskId(taskId);
    }

    /**
     * 测试获取任务实时缺陷信息 - 任务ID无效或无实时缺陷
     */
    @Test
    void liveInfo_InvalidTaskId_ReturnsError() {
        Long taskId = 999L;
        when(flawService.getLiveFlawsByTaskId(taskId))
                .thenThrow(new IllegalArgumentException("任务ID无效"));

        AjaxResult result = flawController.liveInfo(taskId);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("任务ID无效", result.getMsg());
        assertNull(result.getData());

        verify(flawService, times(1)).getLiveFlawsByTaskId(taskId);
    }

    /**
     * 测试检查所有缺陷是否已确认 - 所有缺陷已确认
     */
    @Test
    void checkAllConfirmed_AllConfirmed_ReturnsTrue() {
        Long taskId = 100L;
        when(flawService.checkAllFlawsConfirmed(taskId)).thenReturn(true);

        AjaxResult result = flawController.checkAllConfirmed(taskId);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("检查缺陷确认状态成功", result.getMsg());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof Boolean);
        assertTrue((Boolean) result.getData());

        verify(flawService, times(1)).checkAllFlawsConfirmed(taskId);
    }

    /**
     * 测试检查所有缺陷是否已确认 - 存在未确认缺陷
     */
    @Test
    void checkAllConfirmed_NotAllConfirmed_ReturnsFalse() {
        Long taskId = 100L;
        when(flawService.checkAllFlawsConfirmed(taskId)).thenReturn(false);

        AjaxResult result = flawController.checkAllConfirmed(taskId);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("检查缺陷确认状态成功", result.getMsg());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof Boolean);
        assertFalse((Boolean) result.getData());

        verify(flawService, times(1)).checkAllFlawsConfirmed(taskId);
    }

    /**
     * 测试检查所有缺陷是否已确认 - 任务ID无效
     */
    @Test
    void checkAllConfirmed_InvalidTaskId_ReturnsError() {
        Long taskId = 999L;
        when(flawService.checkAllFlawsConfirmed(taskId))
                .thenThrow(new IllegalArgumentException("任务ID无效"));

        AjaxResult result = flawController.checkAllConfirmed(taskId);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("任务ID无效", result.getMsg());
        assertNull(result.getData());

        verify(flawService, times(1)).checkAllFlawsConfirmed(taskId);
    }
}