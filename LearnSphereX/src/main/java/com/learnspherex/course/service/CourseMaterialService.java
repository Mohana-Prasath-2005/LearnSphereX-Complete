package com.learnspherex.course.service;

import com.learnspherex.course.entity.CourseMaterial;

import java.util.List;

public interface CourseMaterialService {

    CourseMaterial createMaterial(CourseMaterial material);

    List<CourseMaterial> getMaterialsByTopicId(Long topicId);

    CourseMaterial getMaterialById(Long id);

    CourseMaterial updateMaterial(Long id, CourseMaterial materialDetails);

    void deleteMaterial(Long id);
}