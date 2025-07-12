package com.example.demo.javaSrc.petitions;

public class PetitionVoteRequest {
    private PetitionVote.VoteVariant vote;
    public PetitionVote.VoteVariant getVote() { return vote; }
    public void setVote(PetitionVote.VoteVariant vote) { this.vote = vote; }
}