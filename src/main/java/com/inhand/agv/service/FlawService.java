package com.inhand.agv.service;

import com.inhand.agv.common.TableDataInfo;
import com.inhand.agv.entity.AgvFlaw;
import com.inhand.agv.dto.FlawDTO;
import com.inhand.agv.dto.FlawFilter;
import com.inhand.agv.dto.ValidationResult;
import com.inhand.agv.repository.FlawRepository;
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
public class FlawService {

    @Autowired
    private FlawRepository flawRepository;

    /**
     * 分页查询缺陷列表。
     * @param filter 过滤条件和分页信息
     * @return 缺陷数据列表和总数
     */
    public TableDataInfo listFlaws(FlawFilter filter) {
        Specification<AgvFlaw> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 过滤未被逻辑删除的缺陷
            predicates.add(criteriaBuilder.equal(root.get("deleteFlag"), false));

            if (StringUtils.hasText(filter.getDefectCode())) {
                predicates.add(criteriaBuilder.like(root.get("defectCode"), "%" + filter.getDefectCode() + "%"));
            }
            if (filter.getTaskId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("taskId"), filter.getTaskId()));
            }

            if (StringUtils.hasText(filter.getFlawType())) {
                predicates.add(criteriaBuilder.equal(root.get("flawType"), filter.getFlawType()));
            }

            if (StringUtils.hasText(filter.getFlawName())) {
                predicates.add(criteriaBuilder.like(root.get("flawName"), "%" + filter.getFlawName() + "%"));
            }
            if (filter.getConfirmed() != null) {
                predicates.add(criteriaBuilder.equal(root.get("confirmed"), filter.getConfirmed()));
            }
            if (filter.getUploaded() != null) {
                predicates.add(criteriaBuilder.equal(root.get("uploaded"), filter.getUploaded()));
            }
            if (StringUtils.hasText(filter.getLevel())) {
                predicates.add(criteriaBuilder.equal(root.get("level"), filter.getLevel()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(filter.getPageNum() - 1, filter.getPageSize());
        Page<AgvFlaw> page = flawRepository.findAll(spec, pageable);

        return new TableDataInfo(page.getTotalElements(), page.getContent());
    }

    /**
     * 根据ID获取缺陷详情。
     * @param id 缺陷ID
     * @return 缺陷对象
     */
    public AgvFlaw getFlaw(Long id) {
        Optional<AgvFlaw> flawOptional = flawRepository.findById(id);
        // 检查是否被逻辑删除，如果deleteFlag为true，则认为不存在
        return flawOptional.filter(flaw -> !flaw.getDeleteFlag()).orElse(null);
    }

    /**
     * 添加新缺陷。
     * @param flawDTO 缺陷数据
     * @return 新增的缺陷对象
     * @throws IllegalArgumentException 如果验证失败或缺陷编号已存在
     */
    public AgvFlaw addFlaw(FlawDTO flawDTO) {
        ValidationResult validation = validateFlaw(flawDTO);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getMessage());
        }

        // 检查缺陷编号是否唯一 (仅针对未删除的缺陷)
        if (flawRepository.findByDefectCode(flawDTO.getDefectCode()).filter(d -> !d.getDeleteFlag()).isPresent()) {
            throw new IllegalArgumentException("缺陷编号已存在");
        }

        AgvFlaw flaw = new AgvFlaw();
        // 复制DTO属性到实体
        copyDtoToEntity(flawDTO, flaw);
        flaw.setCreateTime(new Date());
        flaw.setDeleteFlag(false); // 默认未逻辑删除
        flaw.setShown(false); // 默认未弹窗提示
        flaw.setConfirmed(false); // 默认未确认
        flaw.setUploaded(false); // 默认未上传

        return flawRepository.save(flaw);
    }

    /**
     * 更新缺陷信息。
     * @param flawDTO 缺陷数据
     * @return 更新后的缺陷对象
     * @throws IllegalArgumentException 如果缺陷不存在或验证失败
     */
    public AgvFlaw updateFlaw(FlawDTO flawDTO) {
        if (flawDTO.getId() == null) {
            throw new IllegalArgumentException("缺陷ID不能为空");
        }

        Optional<AgvFlaw> existingFlawOptional = flawRepository.findById(flawDTO.getId());
        if (!existingFlawOptional.isPresent() || existingFlawOptional.get().getDeleteFlag()) {
            throw new IllegalArgumentException("缺陷不存在或已被删除");
        }
        AgvFlaw existingFlaw = existingFlawOptional.get();

        ValidationResult validation = validateFlaw(flawDTO);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getMessage());
        }

        // 检查缺陷编号是否唯一且不是当前缺陷的编号
        if (StringUtils.hasText(flawDTO.getDefectCode()) &&
                !flawDTO.getDefectCode().equals(existingFlaw.getDefectCode())) {
            if (flawRepository.findByDefectCode(flawDTO.getDefectCode()).filter(d -> !d.getDeleteFlag()).isPresent()) {
                throw new IllegalArgumentException("缺陷编号已存在");
            }
        }

        // 复制DTO属性到实体
        copyDtoToEntity(flawDTO, existingFlaw);

        return flawRepository.save(existingFlaw);
    }

    /**
     * 逻辑删除缺陷。
     * @param id 缺陷ID
     * @return 被删除的缺陷对象
     * @throws IllegalArgumentException 如果缺陷不存在或无法删除
     */
    public AgvFlaw delFlaw(Long id) {
        Optional<AgvFlaw> flawOptional = flawRepository.findById(id);
        if (!flawOptional.isPresent() || flawOptional.get().getDeleteFlag()) {
            throw new IllegalArgumentException("缺陷不存在或已被删除");
        }
        AgvFlaw flaw = flawOptional.get();

        flaw.setDeleteFlag(true);
        return flawRepository.save(flaw);
    }

    /**
     * 轮询获取任务实时缺陷信息。
     * 接口文档中没有明确返回类型，这里假设返回List<AgvFlaw>，实际可以根据业务调整。
     * 通常实时信息可能涉及更复杂的逻辑，例如从缓存或特定流中获取。
     * 此处根据业务需求，修改为查询未确认且未逻辑删除的缺陷。
     * @param taskId 任务ID
     * @return 缺陷列表
     */
    public List<AgvFlaw> getLiveFlawsByTaskId(Long taskId) {

        return flawRepository.findByTaskIdAndConfirmedAndDeleteFlag(taskId, false, false);
    }

    /**
     * 检查任务下所有缺陷是否已全部确认。
     * 接口文档中没有明确返回类型，这里假设返回Boolean。
     * @param taskId 任务ID
     * @return true 如果所有缺陷都已确认，否则 false
     */
    public Boolean checkAllFlawsConfirmed(Long taskId) {
        // 查找所有属于该任务且未被逻辑删除的缺陷
        List<AgvFlaw> flaws = flawRepository.findByTaskIdAndDeleteFlag(taskId, false);
        // 检查是否有任何一个缺陷的confirmed字段为false
        return flaws.stream().allMatch(AgvFlaw::getConfirmed);
    }

    /**
     * 验证缺陷DTO的必填字段。
     * @param flawDTO 缺陷DTO
     * @return 验证结果
     */
    private ValidationResult validateFlaw(FlawDTO flawDTO) {
        if (flawDTO.getDefectCode() == null || flawDTO.getDefectCode().trim().isEmpty()) {
            return new ValidationResult(false, "缺陷编号不能为空");
        }
        if (flawDTO.getTaskId() == null) {
            return new ValidationResult(false, "所属任务ID不能为空");
        }
        // Changed from getDefectType to getFlawType
        if (flawDTO.getFlawType() == null || flawDTO.getFlawType().trim().isEmpty()) {
            return new ValidationResult(false, "缺陷类型不能为空");
        }
        // Changed from getDefectName to getFlawName
        if (flawDTO.getFlawName() == null || flawDTO.getFlawName().trim().isEmpty()) {
            return new ValidationResult(false, "缺陷名称不能为空");
        }

        return new ValidationResult(true, "验证通过");
    }

    /**
     * 辅助方法：将 FlawDTO 的属性复制到 AgvFlaw 实体。
     * @param dto 源DTO
     * @param entity 目标实体
     */
    private void copyDtoToEntity(FlawDTO dto, AgvFlaw entity) {
        if (dto.getDefectCode() != null) entity.setDefectCode(dto.getDefectCode());
        if (dto.getTaskId() != null) entity.setTaskId(dto.getTaskId());
        if (dto.getRound() != null) entity.setRound(dto.getRound());
        if (dto.getFlawType() != null) entity.setFlawType(dto.getFlawType());
        if (dto.getFlawName() != null) entity.setFlawName(dto.getFlawName());
        if (dto.getFlawDesc() != null) entity.setFlawDesc(dto.getFlawDesc());
        if (dto.getFlawDistance() != null) entity.setFlawDistance(dto.getFlawDistance());
        if (dto.getFlawImage() != null) entity.setFlawImage(dto.getFlawImage());
        if (dto.getFlawImageUrl() != null) entity.setFlawImageUrl(dto.getFlawImageUrl());
        if (dto.getFlawRtsp() != null) entity.setFlawRtsp(dto.getFlawRtsp());
        if (dto.getShown() != null) entity.setShown(dto.getShown());
        if (dto.getConfirmed() != null) entity.setConfirmed(dto.getConfirmed());
        if (dto.getUploaded() != null) entity.setUploaded(dto.getUploaded());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        if (dto.getFlawLength() != null) entity.setFlawLength(dto.getFlawLength());
        if (dto.getFlawArea() != null) entity.setFlawArea(dto.getFlawArea());
        if (dto.getLevel() != null) entity.setLevel(dto.getLevel());
        if (dto.getCountNum() != null) entity.setCountNum(dto.getCountNum());
    }
}