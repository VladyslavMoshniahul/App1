package com.example.demo.javaSrc.petitions;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetitionRepository extends JpaRepository<Petition, Long> {

    List<Petition> findByClassIdAndSchoolId(Long classId, Long schoolId);

    List<Petition> findBySchoolId(Long schoolId);

    List<Petition> findByTitle(String title);

    List<Petition> findByDescription(String description);

    List<Petition> findByCreatedBy(Long createdBy);

    List<Petition> findByStatus(Petition.Status status);

    List<Petition> findByStartDateBetween(Date startDate, Date endDate);

    List<Petition> findByDirectorsDecisionAndSchoolIdAndClassId(Long schoolId, Long classId, Petition.DirectorsDecision directorsDecision);

    List<Petition> findByDirectorsDecisionAndSchoolId(Long schoolId, Petition.DirectorsDecision directorsDecision);
}