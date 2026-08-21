package com.learnspherex.trainer.repository;
import com.learnspherex.trainer.entity.TrainerProfile; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TrainerProfileRepository extends JpaRepository<TrainerProfile,Long>{Optional<TrainerProfile> findByUserId(Long userId);}
