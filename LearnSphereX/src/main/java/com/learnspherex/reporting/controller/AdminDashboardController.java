package com.learnspherex.reporting.controller;
import com.learnspherex.auth.UserRepository; import com.learnspherex.batch.entity.AttendanceStatus; import com.learnspherex.batch.entity.BatchStatus; import com.learnspherex.batch.repository.AttendanceRepository; import com.learnspherex.batch.repository.BatchRepository; import com.learnspherex.batch.repository.BatchSessionRepository; import com.learnspherex.course.repository.CourseRepository; import com.learnspherex.examination.repository.ExamAttemptRepository; import com.learnspherex.examination.repository.ExamRepository; import com.learnspherex.payment.PaymentRepository; import com.learnspherex.payment.PaymentService; import com.learnspherex.student.repository.StudentRepository; import com.learnspherex.assignment.repository.AssignmentSubmissionRepository; import lombok.RequiredArgsConstructor; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import java.math.*; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/admin/dashboard") @RequiredArgsConstructor
public class AdminDashboardController {
 private final UserRepository users; private final CourseRepository courses; private final BatchRepository batches; private final StudentRepository students;
 private final AssignmentSubmissionRepository submissions; private final ExamRepository exams; private final ExamAttemptRepository examAttempts;
 private final PaymentRepository payments; private final PaymentService paymentService;
 private final AttendanceRepository attendance; private final BatchSessionRepository sessions;

 @GetMapping @PreAuthorize("hasRole('ADMIN')") @Transactional(readOnly = true)
 public Map<String,Object> dashboard(){
  // @Transactional keeps the session open for u.getUserRoles() below (LAZY) -
  // without it this threw LazyInitializationException on every call.
  long trainers=users.findAll().stream().filter(u->u.getUserRoles().stream().anyMatch(r->r.getRole().getName().name().equals("TRAINER"))).count();
  long studentUsers=users.findAll().stream().filter(u->u.getUserRoles().stream().anyMatch(r->r.getRole().getName().name().equals("STUDENT"))).count();
  BigDecimal revenue=payments.findAll().stream().map(p->p.getAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);

  long activeBatches=batches.findAll().stream().filter(b->b.getBatchStatus()==BatchStatus.ACTIVE).count();
  long completedBatches=batches.findAll().stream().filter(b->b.getBatchStatus()==BatchStatus.COMPLETED).count();

  LocalDate today=LocalDate.now();
  long todaysSessions=sessions.findAll().stream().filter(s->today.equals(s.getSessionDate())).count();

  // Pending = actually not yet evaluated, not "every submission ever made".
  long pendingAssignments=submissions.findAll().stream().filter(s->!"EVALUATED".equals(s.getStatus())).count();

  long upcomingExams=exams.findAll().stream().filter(e->e.getStartAt().isAfter(LocalDateTime.now())).count();

  var attendanceRecords=attendance.findAll();
  double averageAttendance=attendanceRecords.isEmpty()?0.0:Math.round(attendanceRecords.stream()
          .filter(a->a.getStatus()==AttendanceStatus.PRESENT).count()*10000.0/attendanceRecords.size())/100.0;

  var submittedAttempts=examAttempts.findAll().stream().filter(a->a.isSubmitted()).toList();
  double averageExamScore=submittedAttempts.isEmpty()?0.0:Math.round(submittedAttempts.stream()
          .mapToDouble(a->a.getPercentage().doubleValue()).average().orElse(0)*100.0)/100.0;

  BigDecimal pendingFees=paymentService.totalOutstanding();

  Map<String,Object> m=new LinkedHashMap<>();
  m.put("students",students.count());
  m.put("studentUsers",studentUsers);
  m.put("trainers",trainers);
  m.put("courses",courses.count());
  m.put("activeBatches",activeBatches);
  m.put("completedBatches",completedBatches);
  m.put("todaysSessions",todaysSessions);
  m.put("pendingAssignments",pendingAssignments);
  m.put("upcomingExams",upcomingExams);
  m.put("averageAttendance",averageAttendance);
  m.put("averageExamScore",averageExamScore);
  m.put("revenue",revenue);
  m.put("pendingFees",pendingFees);
  return m;
 }
}
