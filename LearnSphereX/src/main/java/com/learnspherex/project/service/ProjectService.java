package com.learnspherex.project.service;

import com.learnspherex.exception.ResourceNotFoundException;
import com.learnspherex.project.dto.*;
import com.learnspherex.project.entity.Project;
import com.learnspherex.project.repository.ProjectRepository;
import com.learnspherex.course.entity.Course;
import com.learnspherex.course.repository.CourseRepository;
import com.learnspherex.security.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProjectService {
    private final ProjectRepository repository;
    private final CourseRepository courseRepository;
    private final CurrentUserService currentUserService;

    public ProjectService(ProjectRepository repository, CourseRepository courseRepository,
                           CurrentUserService currentUserService) {
        this.repository = repository;
        this.courseRepository = courseRepository;
        this.currentUserService = currentUserService;
    }

    public ProjectResponse create(ProjectRequest r, Authentication authentication) {
        currentUserService.assertOwnerOrRole(authentication, r.trainerId(), "ADMIN");
        Course course = courseRepository.findById(r.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + r.courseId()));
        Project p = Project.builder()
                .title(r.title()).description(r.description()).requirements(r.requirements())
                .technology(r.technology()).deadline(r.deadline()).maximumMarks(r.maximumMarks())
                .createdAt(LocalDateTime.now()).trainerId(r.trainerId()).course(course)
                .status(r.status()).build();
        return toResponse(repository.save(p));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public ProjectResponse update(Long id, ProjectRequest r, Authentication authentication) {
        Project p = getEntity(id);
        currentUserService.assertOwnerOrRole(authentication, p.getTrainerId(), "ADMIN");
        Course course = courseRepository.findById(r.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + r.courseId()));
        p.setTitle(r.title()); p.setDescription(r.description()); p.setRequirements(r.requirements());
        p.setTechnology(r.technology()); p.setDeadline(r.deadline()); p.setMaximumMarks(r.maximumMarks());
        p.setTrainerId(r.trainerId()); p.setCourse(course); p.setStatus(r.status());
        return toResponse(repository.save(p));
    }

    public void delete(Long id, Authentication authentication) {
        Project p = getEntity(id);
        currentUserService.assertOwnerOrRole(authentication, p.getTrainerId(), "ADMIN");
        repository.delete(p);
    }

    public Project getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    private ProjectResponse toResponse(Project p) {
        return new ProjectResponse(p.getId(), p.getTitle(), p.getDescription(),
                p.getRequirements(), p.getTechnology(), p.getDeadline(), p.getMaximumMarks(),
                p.getCreatedAt(), p.getTrainerId(), p.getCourse().getId(),
                p.getCourse().getCourseName(), p.getStatus());
    }
}
