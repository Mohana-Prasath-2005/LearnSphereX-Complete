package com.learnspherex.batch.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.learnspherex.course.entity.Course;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Batch name is required")
    private String batchName;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    private BatchMode batchMode;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    private BatchStatus batchStatus;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than 0")
    private Integer capacity;

    @NotNull(message = "Trainer is required")
    private Long trainerId;

    @NotNull(message = "Course is required")
    private Long courseId;

    /*
     * Course relationship is used internally by JPA.
     *
     * We don't want Jackson to serialize the complete Course object
     * when returning a Batch because Course.modules is LAZY.
     *
     * Without @JsonIgnore, we get:
     * "could not initialize proxy - no Session"
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_ref_id")
    private Course course;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public Batch() {
        super();
    }


    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }


    public BatchMode getBatchMode() {
        return batchMode;
    }

    public void setBatchMode(BatchMode batchMode) {
        this.batchMode = batchMode;
    }


    public BatchStatus getBatchStatus() {
        return batchStatus;
    }

    public void setBatchStatus(BatchStatus batchStatus) {
        this.batchStatus = batchStatus;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }


    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }


    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }


    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }


    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }


    // ==========================================
    // BATCH CODE
    // ==========================================

    /*
     * Your existing project/database uses batch_name,
     * while some older code refers to batchCode.
     *
     * These methods maintain compatibility.
     */

    public String getBatchCode() {
        return batchName;
    }

    public void setBatchCode(String code) {
        this.batchName = code;
    }


    // ==========================================
    // ACTIVE CHECK
    // ==========================================

    public boolean isActive() {
        return batchStatus == BatchStatus.ACTIVE;
    }
}