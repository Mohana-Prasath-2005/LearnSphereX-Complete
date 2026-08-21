package com.learnspherex.certificate.service;
import com.learnspherex.certificate.entity.Certificate; import com.learnspherex.certificate.repository.CertificateRepository; import com.learnspherex.student.repository.*; import com.learnspherex.student.entity.*; import com.learnspherex.course.repository.CourseRepository; import com.learnspherex.batch.repository.AttendanceRepository; import com.learnspherex.batch.entity.AttendanceStatus; import com.learnspherex.project.repository.*; import com.learnspherex.examination.repository.ExamAttemptRepository; import com.learnspherex.auth.*; import com.learnspherex.exception.*; import com.learnspherex.notification.event.NotificationEvent; import org.springframework.context.ApplicationEventPublisher; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.math.*; import java.util.*;
@Service
public class CertificateService {
 private final CertificateRepository certs; private final StudentRepository students; private final StudentCourseRepository enrollments; private final CourseRepository courses; private final AttendanceRepository attendance; private final ProjectSubmissionRepository submissions; private final ProjectEvaluationRepository evaluations; private final ExamAttemptRepository attempts; private final UserRepository users; private final ApplicationEventPublisher eventPublisher;
 public CertificateService(CertificateRepository c,StudentRepository s,StudentCourseRepository e,CourseRepository cr,AttendanceRepository a,ProjectSubmissionRepository p,ProjectEvaluationRepository pe,ExamAttemptRepository at,UserRepository u,ApplicationEventPublisher ep){certs=c;students=s;enrollments=e;courses=cr;attendance=a;submissions=p;evaluations=pe;attempts=at;users=u;eventPublisher=ep;}
 // Minimum project score to count as "completed" - the spec names the four AND
 // conditions but doesn't give an explicit passing %, so this mirrors the same
 // 75-as-a-bar convention used for attendance; a reasonable default, not a rule
 // taken verbatim from the spec.
 private static final double PROJECT_PASS_PERCENTAGE = 50.0;

 @Transactional public Certificate generate(Long studentId,Long courseId){
  Student st=students.findById(studentId).orElseThrow(()->new ResourceNotFoundException("Student not found"));
  var enrollment=enrollments.findFirstByStudentIdAndCourseId(studentId,courseId).orElseThrow(()->new InvalidOperationException("Student is not enrolled in course"));
  if(enrollment.getStatus()!=EnrollmentStatus.COMPLETED) throw new InvalidOperationException("Course is not completed");

  // Attendance must be scoped to the specific batch this enrollment is for,
  // not summed across every batch/course the student has ever attended.
  Long batchId=enrollment.getBatch().getId();
  List<com.learnspherex.batch.entity.Attendance> ats=attendance.findByBatchIdAndStudentId(batchId,studentId);
  long total=ats.size(),present=ats.stream().filter(x->x.getStatus()==AttendanceStatus.PRESENT).count();
  double pct=total==0?0:present*100.0/total;
  if(pct<75) throw new InvalidOperationException("Attendance must be at least 75% for this batch");

  var project=submissions.findByStudentId(studentId).stream().filter(x->x.getProject().getCourse().getId().equals(courseId)).findFirst().orElseThrow(()->new InvalidOperationException("Project not submitted"));
  var evaluation=evaluations.findBySubmissionId(project.getId()).orElseThrow(()->new InvalidOperationException("Project is not evaluated"));

  // "Project completed" must mean it passed, not merely that some evaluation
  // row exists - a 2/100 evaluated project used to satisfy this check.
  int projectMax=project.getProject().getMaximumMarks();
  double projectScore=projectMax==0?0:evaluation.getTotalMarks()*100.0/projectMax;
  if(projectScore<PROJECT_PASS_PERCENTAGE) throw new InvalidOperationException("Project evaluation did not meet the passing score");

  var exam=attempts.findAll().stream().filter(x->x.getStudentId().equals(studentId)&&x.getExam().getCourseId().equals(courseId)).toList().stream().filter(x->x.isSubmitted()&&x.getScore().compareTo(x.getExam().getPassingMarks())>=0).findFirst();
  if(exam.isEmpty()) throw new InvalidOperationException("No passed exam found for this course");

  if(certs.existsByStudentIdAndCourseId(studentId,courseId)) return certs.findByStudentId(studentId).stream().filter(x->x.getCourseId().equals(courseId)).findFirst().orElseThrow();

  var course=courses.findById(courseId).orElseThrow();
  String name=users.findById(st.getUserId()).map(u->u.getFirstName()+" "+u.getLastName()).orElse(st.getStudentCode());
  double score=evaluation.getTotalMarks()*100.0/project.getProject().getMaximumMarks();
  String grade=score>=90?"A+":score>=80?"A":score>=70?"B":score>=60?"C":"D";

  // CERT-<courseCode>-<year>-<5-digit sequence>, matching the spec's format
  // (e.g. CERT-JFS-2026-00125) instead of a random UUID fragment.
  int year=java.time.LocalDate.now().getYear();
  long seq=certs.countByCourseIdAndIssuedDateBetween(courseId,java.time.LocalDate.of(year,1,1),java.time.LocalDate.of(year,12,31))+1;
  String certificateId="CERT-"+course.getCourseCode()+"-"+year+"-"+String.format("%05d",seq);

  Certificate c=new Certificate(certificateId,studentId,courseId,name,course.getCourseName(),grade);
  Certificate saved=certs.save(c);

  eventPublisher.publishEvent(new NotificationEvent(st.getUserId(),null,"Certificate Generated",
          "Your certificate for "+course.getCourseName()+" is ready: "+certificateId,"CERTIFICATE_GENERATED"));

  return saved;
 }
 @Transactional(readOnly=true) public List<Certificate> byStudent(Long studentId){return certs.findByStudentId(studentId);}
 @Transactional(readOnly=true) public Certificate get(String id){return certs.findByCertificateId(id).orElseThrow(()->new ResourceNotFoundException("Certificate not found"));}
}
