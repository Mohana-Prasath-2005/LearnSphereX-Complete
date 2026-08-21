package com.learnspherex.trainer.entity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="trainer_profiles",uniqueConstraints=@UniqueConstraint(columnNames="user_id")) @Getter @Setter @NoArgsConstructor
public class TrainerProfile { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="user_id",nullable=false) private Long userId; @Column(length=150) private String specialization; @Column(length=1000) private String bio; @Column(length=500) private String qualifications; private Integer experienceYears; public TrainerProfile(Long u,String s,String b,String q,Integer e){userId=u;specialization=s;bio=b;qualifications=q;experienceYears=e;} }
