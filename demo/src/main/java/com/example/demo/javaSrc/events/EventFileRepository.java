package com.example.demo.javaSrc.events;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventFileRepository extends JpaRepository<EventFile,Long>{
    List<EventFile> findByEventId(Long eventId);
}
