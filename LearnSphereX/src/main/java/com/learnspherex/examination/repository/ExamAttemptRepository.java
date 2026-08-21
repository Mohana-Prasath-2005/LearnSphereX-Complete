package com.learnspherex.examination.repository;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
import com.learnspherex.examination.entity.ExamAttempt;
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt,Long> {
 List<ExamAttempt> findByStudentIdAndExamId(Long studentId, Long examId);
 Optional<ExamAttempt> findTopByStudentIdAndExamIdOrderByStartedAtDesc(Long studentId, Long examId);
 List<ExamAttempt> findBySubmittedFalse();
 List<ExamAttempt> findByStudentId(Long studentId);
}
