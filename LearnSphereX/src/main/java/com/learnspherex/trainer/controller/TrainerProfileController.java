package com.learnspherex.trainer.controller;
import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.repository.BatchRepository;
import com.learnspherex.trainer.entity.TrainerProfile; import com.learnspherex.trainer.repository.TrainerProfileRepository; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException; import java.util.*;
@RestController @RequestMapping("/api/trainers") @RequiredArgsConstructor
public class TrainerProfileController { private final TrainerProfileRepository repo; private final BatchRepository batchRepository;
 @PostMapping("/{userId}/profile") @ResponseStatus(HttpStatus.CREATED) public TrainerProfile create(@PathVariable Long userId,@RequestBody TrainerProfile p){p.setUserId(userId);return repo.save(p);}
 @GetMapping("/{userId}/profile") public TrainerProfile get(@PathVariable Long userId){return repo.findByUserId(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Trainer profile not found"));}
 @PutMapping("/{userId}/profile") public TrainerProfile update(@PathVariable Long userId,@RequestBody TrainerProfile p){var x=get(userId);x.setSpecialization(p.getSpecialization());x.setBio(p.getBio());x.setQualifications(p.getQualifications());x.setExperienceYears(p.getExperienceYears());return repo.save(x);}
 @GetMapping("/{userId}/batches") public List<Batch> batches(@PathVariable Long userId){return batchRepository.findByTrainerId(userId);}
}
