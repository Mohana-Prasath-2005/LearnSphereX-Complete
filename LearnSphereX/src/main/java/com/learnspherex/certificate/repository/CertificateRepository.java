package com.learnspherex.certificate.repository;
import com.learnspherex.certificate.entity.Certificate; import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.*;
public interface CertificateRepository extends JpaRepository<Certificate,Long>{Optional<Certificate> findByCertificateId(String id);List<Certificate> findByStudentId(Long studentId);boolean existsByStudentIdAndCourseId(Long studentId,Long courseId);long countByCourseIdAndIssuedDateBetween(Long courseId,LocalDate start,LocalDate end);}
