package com.learnspherex.examination.service;
import com.learnspherex.examination.dto.ExamDtos.*; import com.learnspherex.examination.entity.*; import com.learnspherex.examination.repository.*;
import com.learnspherex.exception.*; import com.learnspherex.notification.event.NotificationEvent; import com.learnspherex.security.CurrentUserService; import com.learnspherex.student.entity.EnrollmentStatus; import com.learnspherex.student.repository.StudentCourseRepository; import com.learnspherex.student.repository.StudentRepository;
import org.springframework.context.ApplicationEventPublisher; import org.springframework.security.core.Authentication; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.math.*; import java.time.*; import java.util.*;
@Service
public class ExamService {
 private final ExamRepository exams; private final QuestionRepository questions; private final ExamAttemptRepository attempts; private final ExamAnswerRepository answers;
 private final CurrentUserService currentUserService; private final StudentRepository studentRepository;
 private final StudentCourseRepository studentCourseRepository; private final ApplicationEventPublisher eventPublisher;
 public ExamService(ExamRepository e,QuestionRepository q,ExamAttemptRepository a,ExamAnswerRepository ans,CurrentUserService cu,StudentRepository sr,StudentCourseRepository scr,ApplicationEventPublisher ep){exams=e;questions=q;attempts=a;answers=ans;currentUserService=cu;studentRepository=sr;studentCourseRepository=scr;eventPublisher=ep;}

 // Only the owning student (or ADMIN) may act on an attempt already started.
 private void assertAttemptOwner(Authentication authentication,ExamAttempt a){
  Long ownerUserId=studentRepository.findById(a.getStudentId()).map(com.learnspherex.student.entity.Student::getUserId).orElse(null);
  currentUserService.assertOwnerOrRole(authentication,ownerUserId,"ADMIN");
 }

 @Transactional public Exam create(CreateExamRequest r){
  if(r.endAt().isBefore(r.startAt())) throw new BadRequestException("Exam end time must be after start time");
  Exam e=new Exam(null,r.title(),r.courseId(),r.description(),r.durationMinutes(),r.maximumMarks(),r.negativeMarks(),r.passingMarks(),r.attemptsAllowed(),r.startAt(),r.endAt(),true,new ArrayList<>());
  Exam saved=exams.save(e);
  for(var enrollment:studentCourseRepository.findByCourseId(r.courseId())){
   if(enrollment.getStatus()!=EnrollmentStatus.ACTIVE) continue;
   eventPublisher.publishEvent(new NotificationEvent(enrollment.getStudent().getUserId(),null,"Exam Scheduled",
           "\""+r.title()+"\" has been scheduled for "+r.startAt(),"EXAM_SCHEDULED"));
  }
  return saved;
 }
 @Transactional public Question addQuestion(Long examId,CreateQuestionRequest r){ Exam e=exams.findById(examId).orElseThrow(()->new ResourceNotFoundException("Exam not found: "+examId)); Question q=new Question(null,e,r.questionText(),r.questionType().toUpperCase(),r.marks(),r.questionOrder(),new ArrayList<>(),r.expectedAnswer(),r.constraints()); if(r.options()!=null) for(var o:r.options()) q.getOptions().add(new QuestionOption(null,q,o.optionText(),o.correct())); e.getQuestions().add(q); return questions.save(q);}
 @Transactional public StartAttemptResponse start(Long examId,Long studentId,Authentication authentication){ Exam e=exams.findById(examId).orElseThrow(()->new ResourceNotFoundException("Exam not found")); var student=studentRepository.findById(studentId).orElseThrow(()->new ResourceNotFoundException("Student not found: "+studentId)); currentUserService.assertOwnerOrRole(authentication,student.getUserId(),"ADMIN"); LocalDateTime now=LocalDateTime.now(); if(!e.isActive()||now.isBefore(e.getStartAt())||now.isAfter(e.getEndAt())) throw new InvalidOperationException("Exam is not currently available"); int used=attempts.findByStudentIdAndExamId(studentId,examId).size(); if(used>=e.getAttemptsAllowed()) throw new InvalidOperationException("Attempt limit reached"); ExamAttempt a=attempts.save(new ExamAttempt(e,studentId)); return new StartAttemptResponse(a.getId(),examId,a.getStartedAt(),a.getStartedAt().plusMinutes(e.getDurationMinutes())); }
 private boolean isExpired(ExamAttempt a){ return LocalDateTime.now().isAfter(a.getStartedAt().plusMinutes(a.getExam().getDurationMinutes())); }

 // noRollbackFor: the expired-attempt branch below deliberately persists the
 // auto-finalize (finalizeAttempt) before throwing to reject the stale answer;
 // every other throw in this method happens before any write, so this is safe.
 @Transactional(noRollbackFor = InvalidOperationException.class) public void answer(Long attemptId,AnswerRequest r,Authentication authentication){
  ExamAttempt a=attempts.findById(attemptId).orElseThrow(()->new ResourceNotFoundException("Attempt not found"));
  assertAttemptOwner(authentication,a);
  if(a.isSubmitted()) throw new InvalidOperationException("Attempt already submitted");
  if(isExpired(a)){ finalizeAttempt(a); throw new InvalidOperationException("Time is up for this attempt; it has been auto-submitted"); }
  Question q=questions.findById(r.questionId()).orElseThrow(()->new ResourceNotFoundException("Question not found"));
  if(!q.getExam().getId().equals(a.getExam().getId())) throw new BadRequestException("Question does not belong to exam");
  ExamAnswer ea=answers.findByAttemptIdAndQuestionId(attemptId,q.getId()).orElseGet(()->{ExamAnswer x=new ExamAnswer();x.setAttempt(a);x.setQuestion(q);return x;});
  ea.setAnswerText(r.answerText());
  if(r.selectedOptionId()!=null){QuestionOption opt=q.getOptions().stream().filter(x->x.getId().equals(r.selectedOptionId())).findFirst().orElseThrow(()->new BadRequestException("Invalid option"));ea.setSelectedOption(opt);}
  answers.save(ea);
 }

 @Transactional public ResultResponse submit(Long attemptId,Authentication authentication){
  ExamAttempt a=attempts.findById(attemptId).orElseThrow(()->new ResourceNotFoundException("Attempt not found"));
  assertAttemptOwner(authentication,a);
  if(a.isSubmitted()) return result(a);
  finalizeAttempt(a);
  return result(a);
 }

 // System-triggered finalize for attempts the student never explicitly submitted (see the scheduler).
 @Transactional public void autoSubmit(Long attemptId){
  ExamAttempt a=attempts.findById(attemptId).orElseThrow(()->new ResourceNotFoundException("Attempt not found"));
  if(!a.isSubmitted()) finalizeAttempt(a);
 }

 /**
  * Scores every answered MCQ question (auto-graded, deterministic) and preserves whatever
  * has already been manually graded for non-MCQ questions. Any non-MCQ answer nobody has
  * graded yet contributes 0 for now and keeps the attempt's status at SUBMITTED (pending
  * manual grading) rather than prematurely deciding PASSED/FAILED.
  */
 private void finalizeAttempt(ExamAttempt a){
  BigDecimal score=BigDecimal.ZERO; boolean allGraded=true;
  for(ExamAnswer ea:a.getAnswers()){
   if("MCQ".equalsIgnoreCase(ea.getQuestion().getQuestionType())){
    BigDecimal awarded=BigDecimal.ZERO;
    if(ea.getSelectedOption()!=null&&ea.getSelectedOption().isCorrect()) awarded=ea.getQuestion().getMarks();
    else if(ea.getSelectedOption()!=null) awarded=a.getExam().getNegativeMarks().negate();
    ea.setMarksAwarded(awarded); ea.setGraded(true);
   } else if(!ea.isGraded()){
    allGraded=false;
   }
   score=score.add(ea.getMarksAwarded());
  }
  a.setScore(score);
  a.setSubmitted(true);
  if(a.getSubmittedAt()==null) a.setSubmittedAt(LocalDateTime.now());
  BigDecimal totalMarks=a.getExam().getMaximumMarks();
  a.setTotalMarks(totalMarks);
  a.setPercentage(totalMarks.compareTo(BigDecimal.ZERO)==0?BigDecimal.ZERO
          :score.multiply(BigDecimal.valueOf(100)).divide(totalMarks,2,RoundingMode.HALF_UP));
  ExamAttemptStatus previousStatus=a.getStatus();
  if(!allGraded) a.setStatus(ExamAttemptStatus.SUBMITTED);
  else a.setStatus(score.compareTo(a.getExam().getPassingMarks())>=0?ExamAttemptStatus.PASSED:ExamAttemptStatus.FAILED);
  attempts.save(a);

  // Only notify once the result is actually final (PASSED/FAILED), not on the
  // intermediate SUBMITTED-pending-manual-grading state, and only the one time
  // the status actually transitions into a final state.
  boolean justFinalized=(a.getStatus()==ExamAttemptStatus.PASSED||a.getStatus()==ExamAttemptStatus.FAILED)
          &&previousStatus!=ExamAttemptStatus.PASSED&&previousStatus!=ExamAttemptStatus.FAILED;
  if(justFinalized){
   Long ownerUserId=studentRepository.findById(a.getStudentId()).map(com.learnspherex.student.entity.Student::getUserId).orElse(null);
   eventPublisher.publishEvent(new NotificationEvent(ownerUserId,null,"Exam Result",
           "Your result for \""+a.getExam().getTitle()+"\" is ready: "+a.getStatus()+" (score "+a.getScore()+")","EXAM_RESULT"));
  }
 }

 @Transactional public GradeAnswerResponse gradeAnswer(Long attemptId,Long answerId,GradeAnswerRequest r){
  ExamAttempt a=attempts.findById(attemptId).orElseThrow(()->new ResourceNotFoundException("Attempt not found"));
  ExamAnswer ea=answers.findById(answerId).orElseThrow(()->new ResourceNotFoundException("Answer not found"));
  if(!ea.getAttempt().getId().equals(attemptId)) throw new BadRequestException("Answer does not belong to this attempt");
  if(!a.isSubmitted()) throw new InvalidOperationException("Attempt has not been submitted yet");
  if("MCQ".equalsIgnoreCase(ea.getQuestion().getQuestionType())) throw new InvalidOperationException("MCQ answers are auto-graded and cannot be manually overridden");
  if(r.marks().compareTo(ea.getQuestion().getMarks())>0) throw new BadRequestException("Marks cannot exceed the question's maximum: "+ea.getQuestion().getMarks());
  ea.setMarksAwarded(r.marks()); ea.setGraded(true);
  answers.save(ea);
  finalizeAttempt(a);
  return new GradeAnswerResponse(ea.getId(),ea.getMarksAwarded(),ea.isGraded(),result(a));
 }

 @Transactional(readOnly=true) public ResultResponse result(ExamAttempt a){
  return new ResultResponse(a.getId(),a.getExam().getId(),a.getStudentId(),a.getScore(),a.getTotalMarks(),a.getPercentage(),
          a.getExam().getPassingMarks(),a.getStatus().name(),a.getStatus()==ExamAttemptStatus.PASSED,a.getSubmittedAt());
 }
 // Forces Exam.questions and each Question.options to load while the transaction/session
 // is still open; without this, serializing the response throws LazyInitializationException
 // once the method returns and open-in-view=false closes the session.
 private Exam initQuestions(Exam e){ e.getQuestions().size(); e.getQuestions().forEach(q->q.getOptions().size()); return e; }
 @Transactional(readOnly=true) public List<Exam> list(){ var all=exams.findAll(); all.forEach(this::initQuestions); return all; }
 @Transactional(readOnly=true) public Exam get(Long id){ return initQuestions(exams.findById(id).orElseThrow(()->new ResourceNotFoundException("Exam not found: "+id))); }
}
