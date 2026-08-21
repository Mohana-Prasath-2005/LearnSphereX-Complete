package com.learnspherex.batch.entity;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name="batch_enrollments")
public class BatchEnrollment {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@NotNull(message = "Batch ID is required")
	private Long batchId;
	@NotNull(message = "Student ID is required")
	private Long studentId;
	private LocalDate enrollmentDate;
	@NotBlank(message = "Enrollment status is required")
	private String status;
	public BatchEnrollment() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getStudentId() {
		return studentId;
	}
	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}
	public LocalDate getEnrollmentDate() {
		return enrollmentDate;
	}
	public void setEnrollmentDate(LocalDate enrollmentDate) {
		this.enrollmentDate = enrollmentDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getBatchId() {
		return batchId;
	}
	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}
	
	
}
