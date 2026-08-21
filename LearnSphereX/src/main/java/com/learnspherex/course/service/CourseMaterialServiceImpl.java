package com.learnspherex.course.service;

import com.learnspherex.course.entity.CourseMaterial;
import com.learnspherex.course.entity.Topic;
import com.learnspherex.course.repository.CourseMaterialRepository;
import com.learnspherex.course.repository.TopicRepository;
import com.learnspherex.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseMaterialServiceImpl implements CourseMaterialService {

    private final CourseMaterialRepository courseMaterialRepository;
    private final TopicRepository topicRepository;

    public CourseMaterialServiceImpl(
            CourseMaterialRepository courseMaterialRepository,
            TopicRepository topicRepository) {

        this.courseMaterialRepository = courseMaterialRepository;
        this.topicRepository = topicRepository;
    }

    @Override
    public CourseMaterial createMaterial(CourseMaterial material) {

        if (material.getTopic() == null
                || material.getTopic().getId() == null) {

            throw new ResourceNotFoundException(
                    "Topic is required for the course material"
            );
        }

        Topic topic = topicRepository
                .findById(material.getTopic().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Topic not found with id: "
                                        + material.getTopic().getId()
                        )
                );

        material.setTopic(topic);

        return courseMaterialRepository.save(material);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseMaterial> getMaterialsByTopicId(Long topicId) {

        if (!topicRepository.existsById(topicId)) {
            throw new ResourceNotFoundException(
                    "Topic not found with id: " + topicId
            );
        }

        return courseMaterialRepository.findByTopicId(topicId);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseMaterial getMaterialById(Long id) {

        return courseMaterialRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course material not found with id: " + id
                        )
                );
    }

    @Override
    public CourseMaterial updateMaterial(
            Long id,
            CourseMaterial materialDetails) {

        CourseMaterial existingMaterial = getMaterialById(id);

        existingMaterial.setTitle(
                materialDetails.getTitle()
        );

        existingMaterial.setDescription(
                materialDetails.getDescription()
        );

        existingMaterial.setMaterialType(
                materialDetails.getMaterialType()
        );

        existingMaterial.setFileUrl(
                materialDetails.getFileUrl()
        );

        if (materialDetails.getTopic() != null
                && materialDetails.getTopic().getId() != null) {

            Topic topic = topicRepository
                    .findById(materialDetails.getTopic().getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Topic not found with id: "
                                            + materialDetails.getTopic().getId()
                            )
                    );

            existingMaterial.setTopic(topic);
        }

        return courseMaterialRepository.save(existingMaterial);
    }

    @Override
    public void deleteMaterial(Long id) {

        CourseMaterial existingMaterial = getMaterialById(id);

        courseMaterialRepository.delete(existingMaterial);
    }
}