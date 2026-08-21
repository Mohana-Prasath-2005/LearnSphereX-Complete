package com.learnspherex.course.controller;

import com.learnspherex.course.dto.CourseRequestDTO;
import com.learnspherex.course.entity.Course;
import com.learnspherex.course.entity.CourseModule;
import com.learnspherex.course.entity.Topic;
import com.learnspherex.course.entity.CourseMaterial;

import com.learnspherex.course.service.CourseService;
import com.learnspherex.course.service.CourseModuleService;
import com.learnspherex.course.service.TopicService;
import com.learnspherex.course.service.CourseMaterialService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
public class CourseWebController {

    private final CourseService courseService;
    private final CourseModuleService courseModuleService;
    private final TopicService topicService;
    private final CourseMaterialService courseMaterialService;

    public CourseWebController(
            CourseService courseService,
            CourseModuleService courseModuleService,
            TopicService topicService,
            CourseMaterialService courseMaterialService) {

        this.courseService = courseService;
        this.courseModuleService = courseModuleService;
        this.topicService = topicService;
        this.courseMaterialService = courseMaterialService;
    }


    // =========================================================
    // LIST COURSES
    // ADMIN + STUDENT
    // =========================================================

    @GetMapping("/courses")
    public String viewCoursesPage(Model model) {

        model.addAttribute(
                "courses",
                courseService.getAllCourses()
        );

        return "courses";
    }


    // =========================================================
    // ADD COURSE FORM
    // ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/courses/new")
    public String newCourseForm(Model model) {

        model.addAttribute("courseCode", "");
        model.addAttribute("courseName", "");
        model.addAttribute("description", "");
        model.addAttribute("duration", "");
        model.addAttribute("fee", "");
        model.addAttribute("prerequisites", "");
        model.addAttribute("learningObjectives", "");
        model.addAttribute("status", "ACTIVE");
        model.addAttribute("editing", false);

        return "course-form";
    }


    // =========================================================
    // CREATE COURSE
    // ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/courses")
    public String createCourse(
            @RequestParam String courseCode,
            @RequestParam String courseName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String duration,
            @RequestParam BigDecimal fee,
            @RequestParam(required = false) String prerequisites,
            @RequestParam(required = false) String learningObjectives,
            @RequestParam String status,
            Model model) {

        try {

            CourseRequestDTO request =
                    new CourseRequestDTO();

            request.setCourseCode(courseCode);
            request.setCourseName(courseName);
            request.setDescription(description);
            request.setDuration(duration);
            request.setFee(fee);
            request.setPrerequisites(prerequisites);
            request.setLearningObjectives(learningObjectives);
            request.setStatus(status);

            courseService.createCourse(request);

            return "redirect:/courses";

        } catch (RuntimeException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            model.addAttribute(
                    "courseCode",
                    courseCode
            );

            model.addAttribute(
                    "courseName",
                    courseName
            );

            model.addAttribute(
                    "description",
                    description
            );

            model.addAttribute(
                    "duration",
                    duration
            );

            model.addAttribute(
                    "fee",
                    fee
            );

            model.addAttribute(
                    "prerequisites",
                    prerequisites
            );

            model.addAttribute(
                    "learningObjectives",
                    learningObjectives
            );

            model.addAttribute(
                    "status",
                    status
            );

            model.addAttribute(
                    "editing",
                    false
            );

            return "course-form";
        }
    }


    // =========================================================
    // EDIT COURSE FORM
    // ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/courses/{id}/edit")
    public String editCourseForm(
            @PathVariable Long id,
            Model model) {

        /*
         * IMPORTANT:
         * getCourseById() returns a CourseResponse/DTO
         * in your existing CourseService.
         *
         * Therefore use var here instead of Course.
         */
        var course =
                courseService.getCourseById(id);

        model.addAttribute(
                "courseId",
                id
        );

        model.addAttribute(
                "courseCode",
                course.getCourseCode()
        );

        model.addAttribute(
                "courseName",
                course.getCourseName()
        );

        model.addAttribute(
                "description",
                course.getDescription()
        );

        model.addAttribute(
                "duration",
                course.getDuration()
        );

        model.addAttribute(
                "fee",
                course.getFee()
        );

        model.addAttribute(
                "prerequisites",
                course.getPrerequisites()
        );

        model.addAttribute(
                "learningObjectives",
                course.getLearningObjectives()
        );

        model.addAttribute(
                "status",
                course.getStatus()
        );

        model.addAttribute(
                "editing",
                true
        );

        return "course-form";
    }


    // =========================================================
    // UPDATE COURSE
    // ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/courses/{id}/edit")
    public String updateCourse(
            @PathVariable Long id,
            @RequestParam String courseCode,
            @RequestParam String courseName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String duration,
            @RequestParam BigDecimal fee,
            @RequestParam(required = false) String prerequisites,
            @RequestParam(required = false) String learningObjectives,
            @RequestParam String status,
            Model model) {

        try {

            CourseRequestDTO request =
                    new CourseRequestDTO();

            request.setCourseCode(courseCode);
            request.setCourseName(courseName);
            request.setDescription(description);
            request.setDuration(duration);
            request.setFee(fee);
            request.setPrerequisites(prerequisites);
            request.setLearningObjectives(learningObjectives);
            request.setStatus(status);

            courseService.updateCourse(
                    id,
                    request
            );

            return "redirect:/courses/" + id;

        } catch (RuntimeException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            model.addAttribute(
                    "courseId",
                    id
            );

            model.addAttribute(
                    "courseCode",
                    courseCode
            );

            model.addAttribute(
                    "courseName",
                    courseName
            );

            model.addAttribute(
                    "description",
                    description
            );

            model.addAttribute(
                    "duration",
                    duration
            );

            model.addAttribute(
                    "fee",
                    fee
            );

            model.addAttribute(
                    "prerequisites",
                    prerequisites
            );

            model.addAttribute(
                    "learningObjectives",
                    learningObjectives
            );

            model.addAttribute(
                    "status",
                    status
            );

            model.addAttribute(
                    "editing",
                    true
            );

            return "course-form";
        }
    }


    // =========================================================
    // DELETE COURSE
    // ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(
            @PathVariable Long id) {

        courseService.deleteCourse(id);

        return "redirect:/courses";
    }


    // =========================================================
    // COURSE DETAIL
    // ADMIN + STUDENT
    // =========================================================

    @GetMapping("/courses/{id}")
    public String courseDetail(
            @PathVariable Long id,
            Model model) {

        /*
         * Use var because CourseService.getCourseById()
         * returns your existing response/DTO type.
         */
        var course =
                courseService.getCourseById(id);

        model.addAttribute(
                "course",
                course
        );

        return "course-detail";
    }


    // =========================================================
    // ADD MODULE
    // ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/courses/{courseId}/modules")
    public String addModule(
            @PathVariable Long courseId,
            @RequestParam String moduleName,
            @RequestParam(required = false) String description,
            @RequestParam Integer moduleOrder) {

        CourseModule module =
                new CourseModule();

        module.setModuleName(moduleName);
        module.setDescription(description);
        module.setModuleOrder(moduleOrder);

        Course course =
                new Course();

        course.setId(courseId);

        module.setCourse(course);

        courseModuleService.createModule(module);

        return "redirect:/courses/" + courseId;
    }


    // =========================================================
    // ADD TOPIC
    // ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/modules/{moduleId}/topics")
    public String addTopic(
            @PathVariable Long moduleId,
            @RequestParam Long courseId,
            @RequestParam String topicName,
            @RequestParam(required = false) String description,
            @RequestParam Integer topicOrder) {

        Topic topic =
                new Topic();

        topic.setTopicName(topicName);
        topic.setDescription(description);
        topic.setTopicOrder(topicOrder);

        CourseModule module =
                new CourseModule();

        module.setId(moduleId);

        topic.setModule(module);

        topicService.createTopic(topic);

        return "redirect:/courses/" + courseId;
    }


    // =========================================================
    // ADD MATERIAL
    // ADMIN ONLY
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/topics/{topicId}/materials")
    public String addMaterial(
            @PathVariable Long topicId,
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam String materialType,
            @RequestParam String fileUrl) {

        CourseMaterial material =
                new CourseMaterial();

        material.setTitle(title);

        material.setMaterialType(
                CourseMaterial.MaterialType
                        .valueOf(materialType)
        );

        material.setFileUrl(fileUrl);

        Topic topic =
                new Topic();

        topic.setId(topicId);

        material.setTopic(topic);

        courseMaterialService.createMaterial(
                material
        );

        return "redirect:/courses/" + courseId;
    }
}