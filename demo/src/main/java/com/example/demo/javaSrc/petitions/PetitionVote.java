package com.example.demo.javaSrc.petitions;

import jakarta.persistence.*;

@Entity
@Table(name = "petition_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"petition_id", "student_id"})
})
public class PetitionVote {

}

