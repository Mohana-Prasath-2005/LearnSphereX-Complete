package com.learnspherex.payment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.*; import java.time.*; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="installments",uniqueConstraints=@UniqueConstraint(columnNames={"fee_plan_id","installment_number"})) @Getter @NoArgsConstructor @EqualsAndHashCode(of="id") public class Installment { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 // Back-reference: never serialize the parent FeePlan from an Installment - FeePlan.installments
 // is the forward direction, and without breaking the cycle here Jackson recurses
 // FeePlan -> installments -> feePlan -> installments -> ... indefinitely.
 @JsonIgnore
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="fee_plan_id",nullable=false) private FeePlan feePlan; @Column(name="installment_number",nullable=false) private int installmentNumber; @Column(nullable=false,precision=12,scale=2) private BigDecimal amount; @Column(nullable=false) private LocalDate dueDate; public Installment(FeePlan f,int n,BigDecimal a,LocalDate d){feePlan=f;installmentNumber=n;amount=a;dueDate=d;} }
