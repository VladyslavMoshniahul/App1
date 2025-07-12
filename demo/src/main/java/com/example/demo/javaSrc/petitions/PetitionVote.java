package com.example.demo.javaSrc.petitions;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "petition_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"petition_id", "student_id"})
})
public class PetitionVote {

    public enum VoteVariant {
        YES, NO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "petition_id")
    private Petition petition;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteVariant vote;

    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt = LocalDateTime.now();

    public PetitionVote() {
    }
    public PetitionVote(Petition petition, Long studentId, VoteVariant vote) {
        this.petition = petition;
        this.studentId = studentId;
        this.vote = vote;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Petition getPetition() {
        return petition;
    }
    public void setPetition(Petition petition) {
        this.petition = petition;
    }
    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    public VoteVariant getVote() {
        return vote;
    }
    public void setVote(VoteVariant vote) {
        this.vote = vote;
    }
    public LocalDateTime getVotedAt() {
        return votedAt;
    }
    public void setVotedAt(LocalDateTime votedAt) {
        this.votedAt = votedAt;
    }
    
}