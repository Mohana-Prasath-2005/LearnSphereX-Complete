package com.learnspherex.project.service;

import com.learnspherex.exception.BadRequestException;
import com.learnspherex.exception.DuplicateResourceException;
import com.learnspherex.exception.ResourceNotFoundException;
import com.learnspherex.project.dto.*;
import com.learnspherex.project.entity.*;
import com.learnspherex.project.repository.*;
import com.learnspherex.security.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EvaluationCriteriaService {
    private final EvaluationCriteriaRepository repository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public EvaluationCriteriaService(EvaluationCriteriaRepository repository, ProjectRepository projectRepository,
                                      CurrentUserService currentUserService) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    public CriteriaResponse create(Long projectId, CriteriaRequest r, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        currentUserService.assertOwnerOrRole(authentication, project.getTrainerId(), "ADMIN");

        List<EvaluationCriteria> existingCriteria = repository.findByProjectId(projectId);

        boolean exists = existingCriteria.stream()
                .anyMatch(c -> c.getCriteriaName().equalsIgnoreCase(r.criteriaName()));
        if (exists) throw new DuplicateResourceException("Criteria already exists for this project");

        int existingTotal = existingCriteria.stream().mapToInt(EvaluationCriteria::getMaximumMarks).sum();
        if (existingTotal + r.maximumMarks() > project.getMaximumMarks()) {
            throw new BadRequestException("Criteria weights (" + (existingTotal + r.maximumMarks())
                    + ") would exceed the project's maximum marks (" + project.getMaximumMarks() + ")");
        }

        EvaluationCriteria c = EvaluationCriteria.builder()
                .project(project).criteriaName(r.criteriaName())
                .maximumMarks(r.maximumMarks()).description(r.description()).build();
        return toResponse(repository.save(c));
    }

    @Transactional(readOnly = true)
    public List<CriteriaResponse> findByProject(Long projectId) {
        if (!projectRepository.existsById(projectId))
            throw new ResourceNotFoundException("Project not found: " + projectId);
        return repository.findByProjectId(projectId).stream().map(this::toResponse).toList();
    }

    public CriteriaResponse update(Long id, CriteriaRequest r, Authentication authentication) {
        EvaluationCriteria c = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Criteria not found: " + id));
        currentUserService.assertOwnerOrRole(authentication, c.getProject().getTrainerId(), "ADMIN");

        int othersTotal = repository.findByProjectId(c.getProject().getId()).stream()
                .filter(other -> !other.getId().equals(id))
                .mapToInt(EvaluationCriteria::getMaximumMarks)
                .sum();
        if (othersTotal + r.maximumMarks() > c.getProject().getMaximumMarks()) {
            throw new BadRequestException("Criteria weights (" + (othersTotal + r.maximumMarks())
                    + ") would exceed the project's maximum marks (" + c.getProject().getMaximumMarks() + ")");
        }

        c.setCriteriaName(r.criteriaName());
        c.setMaximumMarks(r.maximumMarks());
        c.setDescription(r.description());
        return toResponse(repository.save(c));
    }

    public void delete(Long id, Authentication authentication) {
        EvaluationCriteria c = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Criteria not found: " + id));
        currentUserService.assertOwnerOrRole(authentication, c.getProject().getTrainerId(), "ADMIN");
        repository.delete(c);
    }

    public EvaluationCriteria getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Criteria not found: " + id));
    }

    private CriteriaResponse toResponse(EvaluationCriteria c) {
        return new CriteriaResponse(c.getId(), c.getProject().getId(),
                c.getCriteriaName(), c.getMaximumMarks(), c.getDescription());
    }
}
