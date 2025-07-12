package com.example.demo.javaSrc.voting;


import jakarta.persistence.*;
import java.util.Date;
import java.util.List; // For the @OneToMany relationship
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "voting")
public class Vote {

    public enum VotingLevel {
        SCHOOL("school"),
        ACLASS("aclass"),
        TEACHERS_GROUP("teachers_group"),
        SELECTED_USERS("selected_users");

        private final String dbValue;

        VotingLevel(String dbValue) {
            this.dbValue = dbValue;
        }

        @Override
        public String toString() {
            return dbValue;
        }
    }

    public enum VoteStatus {
        OPEN, CLOSED
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

    @Column(name = "multiple_choice", nullable = false)
    private boolean multipleChoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "voting_level", nullable = false)
    private VotingLevel votingLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VoteStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variants", nullable = false)
    private String variantsJson;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VotingVariant> variants;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<VotingParticipant> participants;

    public Vote() {
    }

    public Vote(Long schoolId, Long classId, String title, String description,
            Long createdBy, Date startDate, Date endDate, boolean multipleChoice,
            VotingLevel votingLevel, String variantsJson) {
        this.schoolId = schoolId;
        this.classId = classId;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.multipleChoice = multipleChoice;
        this.votingLevel = votingLevel;
        this.status = VoteStatus.OPEN;
        this.variantsJson = variantsJson;
    }

    public VotingLevel getVotingLevel() {
        return votingLevel;
    }

    public void setVotingLevel(VotingLevel votingLevel) {
        this.votingLevel = votingLevel;
    }

    public VoteStatus getStatus() {
        return status;
    }

    public void setStatus(VoteStatus status) {
        this.status = status;
    }

    public String getVariantsJson() {
        return variantsJson;
    }

    public void setVariantsJson(String variantsJson) {
        this.variantsJson = variantsJson;
    }

    public List<VotingVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<VotingVariant> variants) {
        this.variants = variants;
    }

    public List<VotingParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<VotingParticipant> participants) {
        this.participants = participants;
    }

    public Long getId() {
        return id;
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

    public boolean isMultipleChoice() {
        return multipleChoice;
    }

    public void setMultipleChoice(boolean multipleChoice) {
        this.multipleChoice = multipleChoice;
    }

    public void setId(Long id) {
       this.id = id;
    }
}