package com.example.demo.javaSrc.petitions;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "petitions")
public class Petition {

    public enum Status {
        OPEN, CLOSED
    }

    public enum DirectorsDecision {
        APPROVED, REJECTED, PENDING, NOT_ENOUGH_VOTING
    }

    public enum VariantsOfVote {
        YES, NO, DID_NOT_VOTE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public Status status;

    @Column(name = "current_positive_vote_count", nullable = false)
    private int currentPositiveVoteCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "directors_decision", nullable = false)
    public DirectorsDecision directorsDecision = DirectorsDecision.NOT_ENOUGH_VOTING;

    public Petition() {
    }

    public Petition(String title, String description, Long schoolId, Long classId,
            Long createdBy, Date startDate, Date endDate, Status status) {
        this.schoolId = schoolId;
        this.classId = classId;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getCurrentPositiveVoteCount() {
        return currentPositiveVoteCount;
    }

    public void setCurrentPositiveVoteCount(int count) {
        this.currentPositiveVoteCount = count;
    }

    public DirectorsDecision getDirectorsDecision() {
        return directorsDecision;
    }

    public void setDirectorsDecision(DirectorsDecision directors_decision) {
        this.directorsDecision = directors_decision;
    }
}