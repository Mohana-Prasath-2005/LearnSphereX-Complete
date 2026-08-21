package com.learnspherex.assignment.service;

import com.learnspherex.assignment.entity.Assignment;
import com.learnspherex.assignment.repository.AssignmentRepository;
import com.learnspherex.course.entity.Course;
import com.learnspherex.course.repository.CourseRepository;
import com.learnspherex.exception.InvalidOperationException;
import com.learnspherex.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    public AssignmentServiceImpl(
            AssignmentRepository assignmentRepository,
            CourseRepository courseRepository) {

        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public Assignment createAssignment(Assignment assignment) {

        if (assignment.getCourse() == null ||
                assignment.getCourse().getId() == null) {

            throw new InvalidOperationException(
                    "Course is required for an assignment"
            );
        }

        Long courseId = assignment.getCourse().getId();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found with id: " + courseId
                        )
                );

        assignment.setCourse(course);

        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAllAssignments() {
                return assignmentRepository.findAllWithCourse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsByCourseId(
            Long courseId) {

        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException(
                    "Course not found with id: " + courseId
            );
        }

        return assignmentRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Assignment getAssignmentById(Long id) {

        return assignmentRepository.findByIdWithCourse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Assignment not found with id: " + id
                        )
                );
    }

    @Override
    public Assignment updateAssignment(
            Long id,
            Assignment assignmentDetails) {

        Assignment existingAssignment =
                getAssignmentById(id);

        existingAssignment.setTitle(
                assignmentDetails.getTitle()
        );

        existingAssignment.setDescription(
                assignmentDetails.getDescription()
        );

        existingAssignment.setDifficulty(
                assignmentDetails.getDifficulty()
        );

        existingAssignment.setDeadline(
                assignmentDetails.getDeadline()
        );

        existingAssignment.setMaxMarks(
                assignmentDetails.getMaxMarks()
        );

        existingAssignment.setStatus(
                assignmentDetails.getStatus()
        );

        if (assignmentDetails.getCourse() != null &&
                assignmentDetails.getCourse().getId() != null) {

            Long courseId =
                    assignmentDetails.getCourse().getId();

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Course not found with id: "
                                            + courseId
                            )
                    );

            existingAssignment.setCourse(course);
        }

        return assignmentRepository.save(existingAssignment);
    }

    @Override
    public void deleteAssignment(Long id) {

        Assignment assignment =
                getAssignmentById(id);

        assignmentRepository.delete(assignment);
    }
}