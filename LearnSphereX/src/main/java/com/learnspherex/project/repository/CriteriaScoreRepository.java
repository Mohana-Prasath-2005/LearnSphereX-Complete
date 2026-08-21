package com.learnspherex.project.repository;
import com.learnspherex.project.entity.CriteriaScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CriteriaScoreRepository extends JpaRepository<CriteriaScore, Long> {
    List<CriteriaScore> findByEvaluationId(Long evaluationId);
}