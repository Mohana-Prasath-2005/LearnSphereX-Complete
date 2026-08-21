package com.learnspherex.student.service;

import com.learnspherex.exception.DuplicateResourceException;
import com.learnspherex.exception.ResourceNotFoundException;
import com.learnspherex.security.CurrentUserService;
import com.learnspherex.student.dto.*;
import com.learnspherex.student.entity.Student;
import com.learnspherex.student.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {
    private final StudentRepository repository;
    private final CurrentUserService currentUserService;

    public StudentService(StudentRepository repository, CurrentUserService currentUserService) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    public StudentResponse create(StudentRequest request, Authentication authentication) {
        currentUserService.assertOwnerOrRole(authentication, request.userId(), "ADMIN");
        if (repository.findByStudentCode(request.studentCode()).isPresent())
            throw new DuplicateResourceException("Student code already exists: " + request.studentCode());
        if (repository.existsByUserId(request.userId()))
            throw new DuplicateResourceException("A student already exists for userId: " + request.userId());

        Student s = Student.builder()
                .userId(request.userId())
                .studentCode(request.studentCode())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .qualification(request.qualification())
                .college(request.college())
                .address(request.address())
                .joiningDate(request.joiningDate())
                .status(request.status())
                .build();

        return toResponse(repository.save(s));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id, Authentication authentication) {
        Student s = getEntity(id);
        currentUserService.assertOwnerOrRole(authentication, s.getUserId(), "ADMIN", "TRAINER");
        return toResponse(s);
    }

    public StudentResponse update(Long id, StudentRequest request, Authentication authentication) {
        Student s = getEntity(id);
        currentUserService.assertOwnerOrRole(authentication, s.getUserId(), "ADMIN");
        repository.findByStudentCode(request.studentCode())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new DuplicateResourceException("Student code already exists"); });

        s.setUserId(request.userId());
        s.setStudentCode(request.studentCode());
        s.setDateOfBirth(request.dateOfBirth());
        s.setGender(request.gender());
        s.setQualification(request.qualification());
        s.setCollege(request.college());
        s.setAddress(request.address());
        s.setJoiningDate(request.joiningDate());
        s.setStatus(request.status());
        return toResponse(repository.save(s));
    }

    public void delete(Long id, Authentication authentication) {
        Student s = getEntity(id);
        currentUserService.assertOwnerOrRole(authentication, s.getUserId(), "ADMIN");
        repository.delete(s);
    }

    public Student getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    private StudentResponse toResponse(Student s) {
        return new StudentResponse(s.getId(), s.getUserId(), s.getStudentCode(),
                s.getDateOfBirth(), s.getGender(), s.getQualification(),
                s.getCollege(), s.getAddress(), s.getJoiningDate(), s.getStatus());
    }
}
