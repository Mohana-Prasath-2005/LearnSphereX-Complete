package com.learnspherex.course.repository;

import com.learnspherex.course.entity.CourseMaterial;
import com.learnspherex.course.entity.CourseMaterial.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {

    List<CourseMaterial> findByTopicId(Long topicId);

    List<CourseMaterial> findByTopicIdAndMaterialType(Long topicId, MaterialType materialType);
}