package com.learnspherex.course.controller;

import com.learnspherex.course.dto.CourseMaterialDTO;
import com.learnspherex.course.dto.CourseMaterialRequestDTO;
import com.learnspherex.course.entity.CourseMaterial;
import com.learnspherex.course.entity.Topic;
import com.learnspherex.course.mapper.CourseMapper;
import com.learnspherex.course.service.CourseMaterialService;
import com.learnspherex.common.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/course-materials")
public class CourseMaterialController {

    private final CourseMaterialService courseMaterialService;
    private final CourseMapper courseMapper;
    private final FileStorageService fileStorageService;

    public CourseMaterialController(
            CourseMaterialService courseMaterialService,
            CourseMapper courseMapper,
            FileStorageService fileStorageService) {

        this.courseMaterialService = courseMaterialService;
        this.courseMapper = courseMapper;
        this.fileStorageService = fileStorageService;
    }

    // Upload an actual file for a material (PDF/VIDEO/DOCUMENT) - stored locally,
    // downloadable afterwards via GET /{id}/download.
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseMaterialDTO> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String materialType,
            @RequestParam Long topicId) {

        String relativePath = fileStorageService.store(file, "materials");

        CourseMaterial material = new CourseMaterial();
        material.setTitle(title);
        material.setDescription(description);
        material.setFileUrl(relativePath);
        material.setMaterialType(CourseMaterial.MaterialType.valueOf(materialType));
        Topic topic = new Topic();
        topic.setId(topicId);
        material.setTopic(topic);

        CourseMaterial created = courseMaterialService.createMaterial(material);

        return new ResponseEntity<>(courseMapper.toMaterialDTO(created), HttpStatus.CREATED);
    }

    // LINK materials redirect to the external URL; uploaded materials stream the
    // stored file back with its original name.
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadMaterial(@PathVariable Long id) {

        CourseMaterial material = courseMaterialService.getMaterialById(id);

        if (material.getMaterialType() == CourseMaterial.MaterialType.LINK) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(material.getFileUrl()))
                    .build();
        }

        byte[] bytes = fileStorageService.load(material.getFileUrl());
        String filename = material.getFileUrl().substring(material.getFileUrl().lastIndexOf('/') + 1);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
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