package com.learnspherex.project.repository;
import com.learnspherex.project.entity.ProjectEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ProjectEvaluationRepository extends JpaRepository<ProjectEvaluation, Long> {
    Optional<ProjectEvaluation> findBySubmissionId(Long submissionId);
    long countBySubmissionStudentId(Long studentId);
}