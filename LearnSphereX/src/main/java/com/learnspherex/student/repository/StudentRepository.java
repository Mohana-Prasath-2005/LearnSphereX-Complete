package com.learnspherex.student.repository;
import com.learnspherex.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);
    boolean existsByUserId(Long userId);
    Optional<Student> findByUserId(Long userId);
}
