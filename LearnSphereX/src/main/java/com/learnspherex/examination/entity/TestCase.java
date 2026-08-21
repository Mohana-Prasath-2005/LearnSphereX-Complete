package com.learnspherex.examination.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*; import lombok.*;

// Structured input/expected-output pairs for CODING questions. Stored for the
// evaluator to check submissions against, not for automated code execution -
// this codebase has no sandboxed runtime to safely execute submitted code, so
// grading a CODING answer against these still goes through the existing manual
// gradeAnswer endpoint.
@Entity @Table(name="exam_question_test_cases") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TestCase {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @JsonIgnore
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="question_id") private Question question;
 @Column(nullable=false,columnDefinition="TEXT") private String input;
 @Column(nullable=false,columnDefinition="TEXT") private String expectedOutput;
 @Column(nullable=false) private Integer caseOrder;
}
