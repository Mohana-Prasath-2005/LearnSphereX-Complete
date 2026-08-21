package com.learnspherex.course.repository;

import com.learnspherex.course.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long> {
    
    boolean existsByNameIgnoreCase(String name);
    
    Optional<Technology> findByNameIgnoreCase(String name);
}