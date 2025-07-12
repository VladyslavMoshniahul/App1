package com.example.demo.javaSrc.invitations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InvitationsRepository  extends JpaRepository<Invitation, Long>{

}
