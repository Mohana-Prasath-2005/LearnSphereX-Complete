package com.learnspherex.certificate.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*;
@Entity @Table(name="certificates") @Getter @Setter @NoArgsConstructor
public class Certificate {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=60) private String certificateId;
 @Column(nullable=false) private Long studentId;
 @Column(nullable=false) private Long courseId;
 @Column(nullable=false,length=150) private String studentName;
 @Column(nullable=false,length=150) private String courseName;
 @Column(nullable=false,length=5) private String grade;
 @Column(nullable=false) private LocalDate issuedDate;
 @Column(nullable=false) private String status="ACTIVE";
 public Certificate(String cid,Long sid,Long courseId,String sn,String cn,String grade){this.certificateId=cid;this.studentId=sid;this.courseId=courseId;this.studentName=sn;this.courseName=cn;this.grade=grade;this.issuedDate=LocalDate.now();}
}
