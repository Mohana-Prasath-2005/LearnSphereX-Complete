package com.learnspherex.examination.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*; import java.time.*; import java.util.*;
@Entity @Table(name="exams") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Exam {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=200) private String title;
 @Column(nullable=false) private Long courseId;
 @Column(length=2000) private String description;
 @Column(nullable=false) private Integer durationMinutes;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal maximumMarks;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal negativeMarks=BigDecimal.ZERO;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal passingMarks;
 @Column(nullable=false) private Integer attemptsAllowed=2;
 @Column(nullable=false) private LocalDateTime startAt;
 @Column(nullable=false) private LocalDateTime endAt;
 @Column(nullable=false) private boolean active=true;
 @OneToMany(mappedBy="exam",cascade=CascadeType.ALL,orphanRemoval=true) private List<Question> questions=new ArrayList<>();
}
