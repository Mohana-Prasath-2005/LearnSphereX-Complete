package com.learnspherex.examination.entity;
import jakarta.persistence.*; import lombok.*; 
@Entity @Table(name="exam_answers",uniqueConstraints=@UniqueConstraint(columnNames={"attempt_id","question_id"})) @Getter @Setter @NoArgsConstructor
public class ExamAnswer {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="attempt_id") private ExamAttempt attempt;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="question_id") private Question question;
 @Column(columnDefinition="TEXT") private String answerText;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="selected_option_id") private QuestionOption selectedOption;
 @Column(nullable=false,precision=10,scale=2) private java.math.BigDecimal marksAwarded=java.math.BigDecimal.ZERO;
 // MCQ answers are graded automatically the moment they're scored; CODING/DESCRIPTIVE/PROJECT
 // answers start ungraded (contributing 0) until a trainer/evaluator grades them explicitly.
 @Column(nullable=false) private boolean graded=false;
}
