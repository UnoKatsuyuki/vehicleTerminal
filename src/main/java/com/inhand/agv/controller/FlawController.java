package com.inhand.agv.controller;

import com.inhand.agv.common.AjaxResult;
import com.inhand.agv.common.TableDataInfo;
import com.inhand.agv.entity.AgvFlaw;
import com.inhand.agv.dto.FlawDTO;
import com.inhand.agv.dto.FlawFilter;
import com.inhand.agv.service.FlawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agv/flaw")
public class FlawController {

    @Autowired
    private FlawService flawService;

    /**
     * 获取缺陷列表 (支持分页和过滤)
     * GET /agv/flaw/list?pageNum=1&pageSize=10&defectCode=...&flawType=...&flawName=...
     * 注意：过滤参数已从defectXxx更新为flawXxx (除了defectCode)
     */
    @GetMapping("/list")
    public TableDataInfo listFlaw(FlawFilter filter) {
        return flawService.listFlaws(filter);
    }

    /**
     * 根据ID获取缺陷详情
     * GET /agv/flaw/{id}
     */
    @GetMapping("/{id}")
    public AjaxResult getFlaw(@PathVariable Long id) {
        AgvFlaw flaw = flawService.getFlaw(id);
        if (flaw == null) {
            return AjaxResult.error("缺陷不存在或已被删除");
        }
        return AjaxResult.success(flaw);
    }

    /**
     * 添加新缺陷
     * POST /agv/flaw
     */
    @PostMapping
    public AjaxResult addFlaw(@RequestBody FlawDTO flawDTO) {
        try {
            AgvFlaw flaw = flawService.addFlaw(flawDTO);
            return AjaxResult.success("创建缺陷成功", flaw);
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 更新缺陷信息
     * PUT /agv/flaw
     */
    @PutMapping
    public AjaxResult updateFlaw(@RequestBody FlawDTO flawDTO) {
        try {
            AgvFlaw flaw = flawService.updateFlaw(flawDTO);
            return AjaxResult.success("更新缺陷成功", flaw);
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 逻辑删除缺陷
     * DELETE /agv/flaw/{id}
     */
    @DeleteMapping("/{id}")
    public AjaxResult delFlaw(@PathVariable Long id) {
        try {
            AgvFlaw flaw = flawService.delFlaw(id);
            return AjaxResult.success("删除缺陷成功", flaw);
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 轮询获取任务实时缺陷信息
     * GET /agv/flaw/live/{id}
     */
    @GetMapping("/live/{id}")
    public AjaxResult liveInfo(@PathVariable Long taskId) {
        try {
            return AjaxResult.success("获取实时缺陷信息成功", flawService.getLiveFlawsByTaskId(taskId));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 检查任务下所有缺陷是否已全部确认
     * GET /agv/flaw/check/{id}
     */
    @GetMapping("/check/{id}")
    public AjaxResult checkAllConfirmed(@PathVariable Long taskId) {
        try {
            return AjaxResult.success("检查缺陷确认状态成功", flawService.checkAllFlawsConfirmed(taskId));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}