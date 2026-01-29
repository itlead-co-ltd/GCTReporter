package com.gct.reportgenerator.repository;

import com.gct.reportgenerator.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 报表数据访问层
 * 
 * @author GCT Reporter
 * @since 1.0.0
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 根据报表名称查找报表
     * 
     * @param name 报表名称
     * @return 报表对象（如果存在）
     */
    Optional<Report> findByName(String name);

    /**
     * 检查报表名称是否存在
     * 
     * @param name 报表名称
     * @return 是否存在
     */
    boolean existsByName(String name);
}
