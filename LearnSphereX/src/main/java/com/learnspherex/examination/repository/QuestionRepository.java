package com.learnspherex.examination.repository;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
import com.learnspherex.examination.entity.Question;
public interface QuestionRepository extends JpaRepository<Question,Long> {


}
