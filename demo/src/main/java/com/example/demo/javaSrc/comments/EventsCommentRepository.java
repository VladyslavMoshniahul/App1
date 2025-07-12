package com.example.demo.javaSrc.comments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventsCommentRepository extends JpaRepository<EventsComment, Long>{

}
