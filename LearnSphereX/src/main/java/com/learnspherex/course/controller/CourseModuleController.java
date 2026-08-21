package com.learnspherex.course.controller;

import com.learnspherex.course.dto.CourseModuleDTO;
import com.learnspherex.course.dto.CourseModuleRequestDTO;
import com.learnspherex.course.entity.Course;
import com.learnspherex.course.entity.CourseModule;
import com.learnspherex.course.mapper.CourseMapper;
import com.learnspherex.course.service.CourseModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/course-modules")
public class CourseModuleController {

    private final CourseModuleService courseModuleService;
    private final CourseMapper courseMapper;

    public CourseModuleController(
            CourseModuleService courseModuleService,
            CourseMapper courseMapper) {

        this.courseModuleService = courseModuleService;
        this.courseMapper = courseMapper;
    }

    // Create a new module
    @PostMapping
    public ResponseEntity<CourseModuleDTO> createModule(
            @Valid @RequestBody CourseModuleRequestDTO request) {

        CourseModule module = new CourseModule();
        module.setModuleName(request.getModuleName());
        module.setDescription(request.getDescription());
        module.setModuleOrder(request.getModuleOrder());

        if (request.getCourseId() != null) {
            Course course = new Course();
            course.setId(request.getCourseId());
            module.setCourse(course);
        }

        CourseModule createdModule = courseModuleService.createModule(module);

        return new ResponseEntity<>(
                courseMapper.toModuleDTO(createdModule),
                HttpStatus.CREATED
        );
    }

    // Get all modules belonging to a course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseModuleDTO>> getModulesByCourseId(
            @PathVariable Long courseId) {

        List<CourseModule> modules = courseModuleService.getModulesByCourseId(courseId);

        List<CourseModuleDTO> response = modules.stream()
                .map(courseMapper::toModuleDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Get module by ID
    @GetMapping("/{id}")
    public ResponseEntity<CourseModuleDTO> getModuleById(
            @PathVariable Long id) {

        CourseModule module = courseModuleService.getModuleById(id);

        return ResponseEntity.ok(courseMapper.toModuleDTO(module));
    }

    // Update module
    @PutMapping("/{id}")
    public ResponseEntity<CourseModuleDTO> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody CourseModuleRequestDTO request) {

        CourseModule moduleDetails = new CourseModule();
        moduleDetails.setModuleName(request.getModuleName());
        moduleDetails.setDescription(request.getDescription());
        moduleDetails.setModuleOrder(request.getModuleOrder());

        if (request.getCourseId() != null) {
            Course course = new Course();
            course.setId(request.getCourseId());
            moduleDetails.setCourse(course);
        }

        CourseModule updatedModule = courseModuleService.updateModule(id, moduleDetails);

        return ResponseEntity.ok(courseMapper.toModuleDTO(updatedModule));
    }

    // Delete module
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(
            @PathVariable Long id) {

        courseModuleService.deleteModule(id);

        return ResponseEntity.noContent().build();
    }
}