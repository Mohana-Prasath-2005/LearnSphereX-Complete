package com.learnspherex.batch.service;

import java.util.List;

import com.learnspherex.batch.entity.Batch;

public interface BatchService {
	Batch createBatch(Batch batch);
	List<Batch> getAllBatches();
	Batch getBatchById(Long id);
	Batch updateBatch(Long id,Batch batch);
	void deleteBatch(Long id);
}
