package com.learnspherex.examination.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="exam_question_options") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class QuestionOption {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @JsonIgnore
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="question_id") private Question question;
 @Column(nullable=false,columnDefinition="TEXT") private String optionText;
 @Column(nullable=false) private boolean correct;
}
