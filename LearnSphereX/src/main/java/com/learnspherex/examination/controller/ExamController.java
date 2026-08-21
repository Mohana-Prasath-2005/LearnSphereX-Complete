package com.learnspherex.examination.controller;
import com.learnspherex.examination.dto.ExamDtos.*; import com.learnspherex.examination.entity.*; import com.learnspherex.examination.service.ExamService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/exams") @RequiredArgsConstructor
public class ExamController {
 private final ExamService service;
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')") public Exam create(@Valid @RequestBody CreateExamRequest r){return service.create(r);}
 @GetMapping public List<Exam> list(){return service.list();}
 @GetMapping("/{id}") public Exam get(@PathVariable Long id){return service.get(id);}
 @PostMapping("/{id}/questions") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER')") public Question question(@PathVariable Long id,@Valid @RequestBody CreateQuestionRequest r){return service.addQuestion(id,r);}
 @PostMapping("/{id}/attempts") public StartAttemptResponse start(@PathVariable Long id,@RequestParam Long studentId,Authentication authentication){return service.start(id,studentId,authentication);}
 @PostMapping("/attempts/{attemptId}/answers") @ResponseStatus(HttpStatus.NO_CONTENT) public void answer(@PathVariable Long attemptId,@Valid @RequestBody AnswerRequest r,Authentication authentication){service.answer(attemptId,r,authentication);}
 @PostMapping("/attempts/{attemptId}/submit") public ResultResponse submit(@PathVariable Long attemptId,Authentication authentication){return service.submit(attemptId,authentication);}
 @PutMapping("/attempts/{attemptId}/answers/{answerId}/grade") @PreAuthorize("hasRole('ADMIN') or hasRole('TRAINER') or hasRole('EVALUATOR')") public GradeAnswerResponse gradeAnswer(@PathVariable Long attemptId,@PathVariable Long answerId,@Valid @RequestBody GradeAnswerRequest r){return service.gradeAnswer(attemptId,answerId,r);}
}
