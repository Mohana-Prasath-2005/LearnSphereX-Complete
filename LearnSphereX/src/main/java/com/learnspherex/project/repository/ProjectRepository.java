package com.learnspherex.project.repository;
import com.learnspherex.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCourseId(Long courseId);
    List<Project> findByTrainerId(Long trainerId);
}