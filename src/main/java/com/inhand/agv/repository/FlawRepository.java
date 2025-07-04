package com.inhand.agv.repository;

import com.inhand.agv.entity.AgvFlaw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlawRepository extends JpaRepository<AgvFlaw, Long>, JpaSpecificationExecutor<AgvFlaw> {

    /**
     * 根据缺陷编号查询缺陷。
     * Spring Data JPA 会自动实现此方法。
     *
     * @param defectCode 缺陷编号
     * @return 包含 AgvFlaw 对象的 Optional，如果未找到则为空
     */
    Optional<AgvFlaw> findByDefectCode(String defectCode);

    /**
     * 根据任务ID、缺陷编号和逻辑删除标志查询缺陷。
     * 主要用于在某个任务下查找特定缺陷。
     * @param taskId 任务ID
     * @param defectCode 缺陷编号
     * @param deleteFlag 逻辑删除标志，应为false
     * @return 包含 AgvFlaw 对象的 Optional
     */
    Optional<AgvFlaw> findByTaskIdAndDefectCodeAndDeleteFlag(Long taskId, String defectCode, Boolean deleteFlag);

    /**
     * 根据任务ID、确认状态和逻辑删除标志查询缺陷列表。
     * 用于获取某个任务下的实时（未确认且未删除）缺陷。
     * @param taskId 任务ID
     * @param confirmed 确认状态
     * @param deleteFlag 逻辑删除标志
     * @return 缺陷列表
     */
    List<AgvFlaw> findByTaskIdAndConfirmedAndDeleteFlag(Long taskId, Boolean confirmed, Boolean deleteFlag);

    /**
     * 根据任务ID和逻辑删除标志查询缺陷列表。
     * 用于检查某个任务下所有缺陷的确认状态。
     * @param taskId 任务ID
     * @param deleteFlag 逻辑删除标志
     * @return 缺陷列表
     */
    List<AgvFlaw> findByTaskIdAndDeleteFlag(Long taskId, Boolean deleteFlag);
}