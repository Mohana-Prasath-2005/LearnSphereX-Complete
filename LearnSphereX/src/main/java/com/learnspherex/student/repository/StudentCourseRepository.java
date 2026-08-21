package com.learnspherex.student.repository;
import com.learnspherex.student.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    List<StudentCourse> findByStudentId(Long studentId);
    List<StudentCourse> findByBatchId(Long batchId);
    List<StudentCourse> findByCourseId(Long courseId);
    boolean existsByStudentIdAndCourseIdAndBatchId(Long studentId, Long courseId, Long batchId);
    long countByStudentId(Long studentId);
    java.util.Optional<StudentCourse> findFirstByStudentIdAndCourseId(Long studentId, Long courseId);
    long countByStudentIdAndStatus(Long studentId, com.learnspherex.student.entity.EnrollmentStatus status);
    boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, com.learnspherex.student.entity.EnrollmentStatus status);
}
