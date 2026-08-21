package com.learnspherex.examination.repository;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
import com.learnspherex.examination.entity.ExamAnswer;
public interface ExamAnswerRepository extends JpaRepository<ExamAnswer,Long> {
 Optional<ExamAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
