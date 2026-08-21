package com.learnspherex.batch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnspherex.batch.entity.Batch;

public interface BatchRepository extends JpaRepository<Batch,Long>{

    List<Batch> findByTrainerId(Long trainerId);
}
