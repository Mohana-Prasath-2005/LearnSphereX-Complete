package com.learnspherex.course.service;

import com.learnspherex.course.entity.Topic;

import java.util.List;

public interface TopicService {

    Topic createTopic(Topic topic);

    List<Topic> getTopicsByModuleId(Long moduleId);

    Topic getTopicById(Long id);

    Topic updateTopic(Long id, Topic topicDetails);

    void deleteTopic(Long id);
}