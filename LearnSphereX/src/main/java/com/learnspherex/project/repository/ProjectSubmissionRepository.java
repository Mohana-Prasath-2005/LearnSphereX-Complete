package com.learnspherex.project.repository;
import com.learnspherex.project.entity.ProjectSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ProjectSubmissionRepository extends JpaRepository<ProjectSubmission, Long> {
    List<ProjectSubmission> findByStudentId(Long studentId);
    List<ProjectSubmission> findByProjectId(Long projectId);
    Optional<ProjectSubmission> findByProjectIdAndStudentId(Long projectId, Long studentId);
    long countByStudentId(Long studentId);
}