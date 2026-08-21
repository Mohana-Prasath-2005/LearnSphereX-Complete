package com.learnspherex.project.repository;
import com.learnspherex.project.entity.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Long> {
    List<EvaluationCriteria> findByProjectId(Long projectId);
}