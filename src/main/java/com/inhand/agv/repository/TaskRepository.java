package com.inhand.agv.repository;

import com.inhand.agv.entity.AgvTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 任务数据访问层接口
 * 继承 JpaRepository 提供基本的 CRUD 操作
 * 继承 JpaSpecificationExecutor 提供基于 Specification 的动态查询能力
 */
@Repository
public interface TaskRepository extends JpaRepository<AgvTask, Long>, JpaSpecificationExecutor<AgvTask> {

    /**
     * 根据任务编号查询任务。
     * Spring Data JPA 会自动实现此方法。
     *
     * @param taskCode 任务编号
     * @return 包含 AgvTask 对象的 Optional，如果未找到则为空
     */
    Optional<AgvTask> findByTaskCode(String taskCode);

}