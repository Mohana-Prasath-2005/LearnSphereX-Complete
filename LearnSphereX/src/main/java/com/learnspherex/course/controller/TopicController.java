package com.learnspherex.course.controller;

import com.learnspherex.course.dto.TopicDTO;
import com.learnspherex.course.dto.TopicRequestDTO;
import com.learnspherex.course.entity.CourseModule;
import com.learnspherex.course.entity.Topic;
import com.learnspherex.course.mapper.CourseMapper;
import com.learnspherex.course.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;
    private final CourseMapper courseMapper;

    public TopicController(
            TopicService topicService,
            CourseMapper courseMapper) {

        this.topicService = topicService;
        this.courseMapper = courseMapper;
    }

    // Create a new topic
    @PostMapping
    public ResponseEntity<TopicDTO> createTopic(
            @Valid @RequestBody TopicRequestDTO request) {

        Topic topic = new Topic();
        topic.setTopicName(request.getTopicName());
        topic.setDescription(request.getContent());
        topic.setTopicOrder(request.getTopicOrder());

        if (request.getModuleId() != null) {
            CourseModule module = new CourseModule();
            module.setId(request.getModuleId());
            topic.setModule(module);
        }

        Topic createdTopic = topicService.createTopic(topic);

        return new ResponseEntity<>(
                courseMapper.toTopicDTO(createdTopic),
                HttpStatus.CREATED
        );
    }

    // Get all topics belonging to a module
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<TopicDTO>> getTopicsByModuleId(
            @PathVariable Long moduleId) {

        List<Topic> topics = topicService.getTopicsByModuleId(moduleId);

        List<TopicDTO> response = topics.stream()
                .map(courseMapper::toTopicDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Get topic by ID
    @GetMapping("/{id}")
    public ResponseEntity<TopicDTO> getTopicById(
            @PathVariable Long id) {

        Topic topic = topicService.getTopicById(id);

        return ResponseEntity.ok(courseMapper.toTopicDTO(topic));
    }

    // Update topic
    @PutMapping("/{id}")
    public ResponseEntity<TopicDTO> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicRequestDTO request) {

        Topic topicDetails = new Topic();
        topicDetails.setTopicName(request.getTopicName());
        topicDetails.setDescription(request.getContent());
        topicDetails.setTopicOrder(request.getTopicOrder());

        if (request.getModuleId() != null) {
            CourseModule module = new CourseModule();
            module.setId(request.getModuleId());
            topicDetails.setModule(module);
        }

        Topic updatedTopic = topicService.updateTopic(id, topicDetails);

        return ResponseEntity.ok(courseMapper.toTopicDTO(updatedTopic));
    }

    // Delete topic
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable Long id) {

        topicService.deleteTopic(id);

        return ResponseEntity.noContent().build();
    }
}