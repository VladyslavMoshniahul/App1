package com.example.demo.javaSrc.voting;


import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "voting_vote")
public class VotingVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voting_id", nullable = false)
    private Vote voting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private VotingVariant variant;

    @Column(name = "user_id", nullable = false)
    private Long userId; 

    @Column(name = "vote_date", nullable = false)
    private Date voteDate;

    public VotingVote() {
    }

    public VotingVote(Vote voting, VotingVariant variant, Long userId) {
        this.voting = voting;
        this.variant = variant;
        this.userId = userId;
        this.voteDate = new Date(); 
    }


    public Long getId() { return id; }
    public Vote getVote() { return voting; }
    public VotingVariant getVariant() { return variant; }
    public Long getUserId() { return userId; }
    public Date getVoteDate() { return voteDate; }

    public void setVote(Vote voting) { this.voting = voting; }
    public void setVariant(VotingVariant variant) { this.variant = variant; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setVoteDate(Date voteDate) { this.voteDate = voteDate; }
}