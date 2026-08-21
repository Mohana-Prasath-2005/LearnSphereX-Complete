package com.learnspherex.course.service;

import com.learnspherex.course.entity.CourseModule;
import com.learnspherex.course.entity.Topic;
import com.learnspherex.course.repository.CourseModuleRepository;
import com.learnspherex.course.repository.TopicRepository;
import com.learnspherex.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final CourseModuleRepository courseModuleRepository;

    public TopicServiceImpl(
            TopicRepository topicRepository,
            CourseModuleRepository courseModuleRepository) {

        this.topicRepository = topicRepository;
        this.courseModuleRepository = courseModuleRepository;
    }

    @Override
    public Topic createTopic(Topic topic) {

        if (topic.getModule() == null || topic.getModule().getId() == null) {
            throw new ResourceNotFoundException("Course module is required for the topic");
        }

        CourseModule module = courseModuleRepository
                .findById(topic.getModule().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course module not found with id: "
                                        + topic.getModule().getId()
                        )
                );

        topic.setModule(module);

        return topicRepository.save(topic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> getTopicsByModuleId(Long moduleId) {

        if (!courseModuleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException(
                    "Course module not found with id: " + moduleId
            );
        }

        return topicRepository
                .findByModuleIdOrderByTopicOrderAsc(moduleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Topic getTopicById(Long id) {

        return topicRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Topic not found with id: " + id
                        )
                );
    }

    @Override
    public Topic updateTopic(Long id, Topic topicDetails) {

        Topic existingTopic = getTopicById(id);

        existingTopic.setTopicName(topicDetails.getTopicName());
        existingTopic.setDescription(topicDetails.getDescription());
        existingTopic.setTopicOrder(topicDetails.getTopicOrder());

        if (topicDetails.getModule() != null
                && topicDetails.getModule().getId() != null) {

            CourseModule module = courseModuleRepository
                    .findById(topicDetails.getModule().getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Course module not found with id: "
                                            + topicDetails.getModule().getId()
                            )
                    );

            existingTopic.setModule(module);
        }

        return topicRepository.save(existingTopic);
    }

    @Override
    public void deleteTopic(Long id) {

        Topic existingTopic = getTopicById(id);

        topicRepository.delete(existingTopic);
    }
}