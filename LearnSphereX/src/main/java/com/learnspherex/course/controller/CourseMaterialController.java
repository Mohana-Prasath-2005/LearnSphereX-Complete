package com.learnspherex.course.controller;

import com.learnspherex.course.dto.CourseMaterialDTO;
import com.learnspherex.course.dto.CourseMaterialRequestDTO;
import com.learnspherex.course.entity.CourseMaterial;
import com.learnspherex.course.entity.Topic;
import com.learnspherex.course.mapper.CourseMapper;
import com.learnspherex.course.service.CourseMaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/course-materials")
public class CourseMaterialController {

    private final CourseMaterialService courseMaterialService;
    private final CourseMapper courseMapper;

    public CourseMaterialController(
            CourseMaterialService courseMaterialService,
            CourseMapper courseMapper) {

        this.courseMaterialService = courseMaterialService;
        this.courseMapper = courseMapper;
    }

    // Create a new course material
    @PostMapping
    public ResponseEntity<CourseMaterialDTO> createMaterial(
            @Valid @RequestBody CourseMaterialRequestDTO request) {

        CourseMaterial material = new CourseMaterial();
        material.setTitle(request.getMaterialTitle());
        material.setFileUrl(request.getFileUrl());
        
        if (request.getMaterialType() != null) {
            material.setMaterialType(CourseMaterial.MaterialType.valueOf(request.getMaterialType()));
        }

        if (request.getTopicId() != null) {
            Topic topic = new Topic();
            topic.setId(request.getTopicId());
            material.setTopic(topic);
        }

        CourseMaterial createdMaterial = courseMaterialService.createMaterial(material);

        return new ResponseEntity<>(
                courseMapper.toMaterialDTO(createdMaterial),
                HttpStatus.CREATED
        );
    }

    // Get all materials belonging to a topic
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<CourseMaterialDTO>> getMaterialsByTopicId(
            @PathVariable Long topicId) {

        List<CourseMaterial> materials = courseMaterialService.getMaterialsByTopicId(topicId);

        List<CourseMaterialDTO> response = materials.stream()
                .map(courseMapper::toMaterialDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Get material by ID
    @GetMapping("/{id}")
    public ResponseEntity<CourseMaterialDTO> getMaterialById(
            @PathVariable Long id) {

        CourseMaterial material = courseMaterialService.getMaterialById(id);

        return ResponseEntity.ok(courseMapper.toMaterialDTO(material));
    }

    // Update material
    @PutMapping("/{id}")
    public ResponseEntity<CourseMaterialDTO> updateMaterial(
            @PathVariable Long id,
            @Valid @RequestBody CourseMaterialRequestDTO request) {

        CourseMaterial materialDetails = new CourseMaterial();
        materialDetails.setTitle(request.getMaterialTitle());
        materialDetails.setFileUrl(request.getFileUrl());

        if (request.getMaterialType() != null) {
            materialDetails.setMaterialType(CourseMaterial.MaterialType.valueOf(request.getMaterialType()));
        }

        if (request.getTopicId() != null) {
            Topic topic = new Topic();
            topic.setId(request.getTopicId());
            materialDetails.setTopic(topic);
        }

        CourseMaterial updatedMaterial = courseMaterialService.updateMaterial(id, materialDetails);

        return ResponseEntity.ok(courseMapper.toMaterialDTO(updatedMaterial));
    }

    // Delete material
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable Long id) {

        courseMaterialService.deleteMaterial(id);

        return ResponseEntity.noContent().build();
    }
}