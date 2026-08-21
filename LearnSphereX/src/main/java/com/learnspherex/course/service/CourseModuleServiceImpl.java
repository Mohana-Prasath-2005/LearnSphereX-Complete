package com.learnspherex.course.service;

import com.learnspherex.course.entity.Course;
import com.learnspherex.course.entity.CourseModule;
import com.learnspherex.course.repository.CourseModuleRepository;
import com.learnspherex.course.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseModuleServiceImpl implements CourseModuleService {

    private final CourseModuleRepository courseModuleRepository;
    private final CourseRepository courseRepository;

    public CourseModuleServiceImpl(
            CourseModuleRepository courseModuleRepository,
            CourseRepository courseRepository) {
        this.courseModuleRepository = courseModuleRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public CourseModule createModule(CourseModule module) {
        if (module.getCourse() == null || module.getCourse().getId() == null) {
            throw new RuntimeException("Course is required for the module");
        }

        Course course = courseRepository.findById(module.getCourse().getId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + module.getCourse().getId()));

        module.setCourse(course);
        return courseModuleRepository.save(module);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseModule> getModulesByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found with id: " + courseId);
        }
        return courseModuleRepository.findByCourseIdOrderByModuleOrderAsc(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseModule getModuleById(Long id) {
        return courseModuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course module not found with id: " + id));
    }

    @Override
    public CourseModule updateModule(Long id, CourseModule moduleDetails) {
        CourseModule existingModule = getModuleById(id);

        existingModule.setModuleName(moduleDetails.getModuleName());
        existingModule.setDescription(moduleDetails.getDescription());
        existingModule.setModuleOrder(moduleDetails.getModuleOrder());

        if (moduleDetails.getCourse() != null && moduleDetails.getCourse().getId() != null) {
            Course course = courseRepository.findById(moduleDetails.getCourse().getId())
                    .orElseThrow(() -> new RuntimeException("Course not found with id: " + moduleDetails.getCourse().getId()));
            existingModule.setCourse(course);
        }

        return courseModuleRepository.save(existingModule);
    }

    @Override
    public void deleteModule(Long id) {
        CourseModule existingModule = getModuleById(id);
        courseModuleRepository.delete(existingModule);
    }
}