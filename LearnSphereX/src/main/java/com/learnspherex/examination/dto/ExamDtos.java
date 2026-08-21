package com.learnspherex.examination.dto;
import java.math.*; import java.time.*; import java.util.*; import jakarta.validation.constraints.*;
public final class ExamDtos {
 private ExamDtos(){}
 public record CreateExamRequest(@NotBlank String title,@NotNull Long courseId,String description,@Min(1) Integer durationMinutes,@NotNull BigDecimal maximumMarks,@NotNull BigDecimal negativeMarks,@NotNull BigDecimal passingMarks,@Min(1) Integer attemptsAllowed,@NotNull LocalDateTime startAt,@NotNull LocalDateTime endAt){}
 public record CreateQuestionRequest(@NotBlank String questionText,@NotBlank String questionType,@NotNull BigDecimal marks,@Min(1) Integer questionOrder,List<OptionRequest> options,String expectedAnswer,String constraints,List<TestCaseRequest> testCases){}
 public record OptionRequest(@NotBlank String optionText,boolean correct){}
 public record TestCaseRequest(@NotBlank String input,@NotBlank String expectedOutput,@Min(1) Integer caseOrder){}
 public record StartAttemptResponse(Long attemptId,Long examId,LocalDateTime startedAt,LocalDateTime expiresAt){}
 public record AnswerRequest(@NotNull Long questionId,String answerText,Long selectedOptionId){}
 public record ResultResponse(Long attemptId,Long examId,Long studentId,BigDecimal score,BigDecimal totalMarks,BigDecimal percentage,BigDecimal passingMarks,String status,boolean passed,LocalDateTime submittedAt){}
 public record GradeAnswerRequest(@NotNull @DecimalMin(value="0",message="Marks cannot be negative") BigDecimal marks){}
 public record GradeAnswerResponse(Long answerId,BigDecimal marksAwarded,boolean graded,ResultResponse attempt){}
}
