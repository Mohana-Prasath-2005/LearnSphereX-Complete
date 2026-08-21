package com.learnspherex.examination.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*; import lombok.*; import java.util.*;
@Entity @Table(name="exam_questions") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Question {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 // Back-reference: never serialize the parent Exam from a Question (mirrors Batch.course's
 // @JsonIgnore) - Exam.questions is LAZY and this caused LazyInitializationException on every
 // response that included a Question.
 @JsonIgnore
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="exam_id") private Exam exam;
 @Column(nullable=false,columnDefinition="TEXT") private String questionText;
 @Column(nullable=false,length=30) private String questionType; // MCQ, CODING, DESCRIPTIVE, PROJECT
 @Column(nullable=false,precision=10,scale=2) private java.math.BigDecimal marks;
 @Column(nullable=false) private Integer questionOrder;
 @OneToMany(mappedBy="question",cascade=CascadeType.ALL,orphanRemoval=true) private List<QuestionOption> options=new ArrayList<>();
 @Column(columnDefinition="TEXT") private String expectedAnswer;
 @Column(columnDefinition="TEXT") private String constraints;
}
