package com.learnspherex.course.service;

import com.learnspherex.course.dto.CourseRequestDTO;
import com.learnspherex.course.dto.CourseResponseDTO;
import com.learnspherex.course.entity.Course;
import com.learnspherex.course.entity.Technology;
import com.learnspherex.course.mapper.CourseMapper;
import com.learnspherex.course.repository.CourseRepository;
import com.learnspherex.course.repository.TechnologyRepository;
import com.learnspherex.exception.DuplicateResourceException;
import com.learnspherex.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final TechnologyRepository technologyRepository;

    public CourseServiceImpl(CourseRepository courseRepository, CourseMapper courseMapper,
                              TechnologyRepository technologyRepository) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.technologyRepository = technologyRepository;
    }

    private List<Technology> resolveTechnologies(List<Long> technologyIds) {
        if (technologyIds == null || technologyIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Technology> technologies = technologyRepository.findAllById(technologyIds);
        if (technologies.size() != technologyIds.size()) {
            throw new ResourceNotFoundException("One or more technology ids were not found");
        }
        return technologies;
    }

    @Override
    @CacheEvict(cacheNames = "courses", allEntries = true)
    public CourseResponseDTO createCourse(CourseRequestDTO request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("Course code already exists: " + request.getCourseCode());
        }

        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .duration(request.getDuration())
                .fee(request.getFee())
                .prerequisites(request.getPrerequisites())
                .learningObjectives(request.getLearningObjectives())
                .status(request.getStatus() != null ? Course.CourseStatus.valueOf(request.getStatus()) : Course.CourseStatus.ACTIVE)
                .technologies(resolveTechnologies(request.getTechnologyIds()))
                .build();

        Course saved = courseRepository.save(course);
        return courseMapper.toCourseResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "courses", key = "'all'")
    public List<CourseResponseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toCourseResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "courses", key = "'id:' + #id")
    public CourseResponseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return courseMapper.toCourseResponseDTO(course);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "courses", key = "'code:' + #courseCode")
    public CourseResponseDTO getCourseByCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with code: " + courseCode));
        return courseMapper.toCourseResponseDTO(course);
    }

    @Override
    @CacheEvict(cacheNames = "courses", allEntries = true)
    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO request) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        existingCourse.setCourseName(request.getCourseName());
        existingCourse.setDescription(request.getDescription());
        existingCourse.setDuration(request.getDuration());
        existingCourse.setFee(request.getFee());
        existingCourse.setPrerequisites(request.getPrerequisites());
        existingCourse.setLearningObjectives(request.getLearningObjectives());
        if (request.getStatus() != null) {
            existingCourse.setStatus(Course.CourseStatus.valueOf(request.getStatus()));
        }
        if (request.getTechnologyIds() != null) {
            existingCourse.setTechnologies(resolveTechnologies(request.getTechnologyIds()));
        }

        Course updated = courseRepository.save(existingCourse);
        return courseMapper.toCourseResponseDTO(updated);
    }

    @Override
    @CacheEvict(cacheNames = "courses", allEntries = true)
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }
}