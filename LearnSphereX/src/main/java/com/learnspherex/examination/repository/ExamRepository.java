package com.learnspherex.examination.repository;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
import com.learnspherex.examination.entity.Exam;
public interface ExamRepository extends JpaRepository<Exam,Long> {


}
