package com.learnspherex.examination.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*; import java.time.*; import java.util.*;
@Entity @Table(name="exam_attempts") @Getter @Setter @NoArgsConstructor
public class ExamAttempt {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="exam_id") private Exam exam;
 @Column(nullable=false) private Long studentId;
 @Column(nullable=false) private LocalDateTime startedAt;
 private LocalDateTime submittedAt;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal score=BigDecimal.ZERO;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal totalMarks=BigDecimal.ZERO;
 @Column(nullable=false,precision=5,scale=2) private BigDecimal percentage=BigDecimal.ZERO;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ExamAttemptStatus status=ExamAttemptStatus.STARTED;
 @Column(nullable=false) private boolean submitted=false;
 @OneToMany(mappedBy="attempt",cascade=CascadeType.ALL,orphanRemoval=true) private List<ExamAnswer> answers=new ArrayList<>();
 public ExamAttempt(Exam e,Long student){exam=e;studentId=student;startedAt=LocalDateTime.now();}
}
