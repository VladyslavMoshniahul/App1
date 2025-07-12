package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.javaSrc.people.People;
import com.example.demo.javaSrc.people.PeopleRepository;
import com.example.demo.javaSrc.petitions.Petition;
import com.example.demo.javaSrc.petitions.PetitionRepository;
import com.example.demo.javaSrc.petitions.PetitionService;
import com.example.demo.javaSrc.petitions.PetitionVote;
import com.example.demo.javaSrc.petitions.PetitionVoteRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
public class PetitionServiceTest {
    @Autowired
    private PetitionService petitionService;

    @MockBean
    private PetitionRepository petitionRepository;

    @MockBean
    private PeopleRepository peopleRepository;

    @MockBean
    private PetitionVoteRepository petitionVoteRepository;

    private Petition petition;
    private People student;

    @BeforeEach
    void setup() {
        petition = new Petition();
        petition.setSchoolId(1L);
        petition.setClassId(1L);
        petition.setTitle("Test Petition");
        petition.setDescription("Test Description");
        petition.setCreatedBy(1L);
        petition.setStartDate(Date.from(LocalDateTime.now().minusDays(1)
            .atZone(ZoneId.systemDefault()).toInstant()));
        petition.setEndDate(Date.from(LocalDateTime.now().plusDays(1)
            .atZone(ZoneId.systemDefault()).toInstant()));
        petition.setStatus(Petition.Status.OPEN);
        petition.setCurrentPositiveVoteCount(0);
        petition.setDirectorsDecision(Petition.DirectorsDecision.NOT_ENOUGH_VOTING);

        student = new People();
        student.setRole(People.Role.STUDENT);
        student.setSchoolId(1L);
        student.setClassId(1L);
    }

    @Test
    void testCreateAndGetPetition() {
        when(petitionRepository.save(any(Petition.class))).thenReturn(petition);
        when(petitionRepository.findById(1L)).thenReturn(Optional.of(petition));

        Petition created = petitionService.createPetition(petition);
        assertThat(created).isEqualTo(petition);

        Petition found = petitionService.getPetitionById(1L);
        assertThat(found).isEqualTo(petition);
    }

    @Test
    void testGetPetitionsByVariousFilters() {
        List<Petition> list = Collections.singletonList(petition);

        when(petitionRepository.findByClassIdAndSchoolId(1L, 1L)).thenReturn(list);
        when(petitionRepository.findBySchoolId(1L)).thenReturn(list);
        when(petitionRepository.findByTitle("Test Petition")).thenReturn(list);
        when(petitionRepository.findByDescription("Test Description")).thenReturn(list);
        when(petitionRepository.findByCreatedBy(1L)).thenReturn(list);
        when(petitionRepository.findByStatus(Petition.Status.OPEN)).thenReturn(list);
        when(petitionRepository.findByDirectorsDecision(Petition.DirectorsDecision.NOT_ENOUGH_VOTING)).thenReturn(list);

        Date start = Date.from(LocalDateTime.now().minusDays(2)
            .atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDateTime.now().plusDays(2)
            .atZone(ZoneId.systemDefault()).toInstant());
        when(petitionRepository.findByStartDateBetween(start, end)).thenReturn(list);

        assertThat(petitionService.getPetitionByClassAndSchool(1L, 1L)).isEqualTo(list);
        assertThat(petitionService.getPetitionBySchool(1L)).isEqualTo(list);
        assertThat(petitionService.getPetitionByTitle("Test Petition")).isEqualTo(list);
        assertThat(petitionService.getPetitionByDescription("Test Description")).isEqualTo(list);
        assertThat(petitionService.getPetitionByCreatedBy(1L)).isEqualTo(list);
        assertThat(petitionService.getPetitionByStatus(Petition.Status.OPEN)).isEqualTo(list);
        assertThat(petitionService.getPetitionByDirectorsDecision(Petition.DirectorsDecision.NOT_ENOUGH_VOTING)).isEqualTo(list);
        assertThat(petitionService.getPetitionByStartDateBetween(start, end)).isEqualTo(list);
    }

    @Test
    void testUpdatePetition() {
        Petition updated = new Petition();
        updated.setSchoolId(2L);
        updated.setClassId(2L);
        updated.setTitle("Updated Title");
        updated.setDescription("Updated Description");
        updated.setCreatedBy(2L);
        updated.setStartDate(petition.getStartDate());
        updated.setEndDate(petition.getEndDate());
        updated.setStatus(Petition.Status.CLOSED);
        updated.setCurrentPositiveVoteCount(5);
        updated.setDirectorsDecision(Petition.DirectorsDecision.APPROVED);

        when(petitionRepository.findById(1L)).thenReturn(Optional.of(petition));
        when(petitionRepository.save(any(Petition.class))).thenAnswer(i -> i.getArgument(0));

        Petition result = petitionService.updatePetition(1L, updated);

        assertThat(result.getSchoolId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getStatus()).isEqualTo(Petition.Status.CLOSED);
        assertThat(result.getDirectorsDecision()).isEqualTo(Petition.DirectorsDecision.APPROVED);
    }

    @Test
    void testDeletePetition() {
        doNothing().when(petitionRepository).deleteById(1L);
        petitionService.deletePetition(1L);
        verify(petitionRepository, times(1)).deleteById(1L);
    }

    @Test
    @Transactional
    void testVoteSuccess() throws Exception {
        when(petitionRepository.findById(1L)).thenReturn(Optional.of(petition));
        when(petitionVoteRepository.existsByPetitionIdAndStudentId(1L, 100L)).thenReturn(false);

        // Здесь заменили строку на enum
        when(peopleRepository.findByRoleAndSchoolIdAndClassId(
                People.Role.STUDENT, 1L, 1L))
            .thenReturn(List.of(student));

        when(petitionVoteRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(petitionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        petitionService.vote(1L, 100L, PetitionVote.VoteVariant.YES);

        assertThat(petition.getCurrentPositiveVoteCount()).isEqualTo(1);
        assertThat(petition.getDirectorsDecision()).isEqualTo(Petition.DirectorsDecision.PENDING);

        verify(petitionVoteRepository, times(1)).save(any());
        verify(petitionRepository, times(1)).save(any());
    }

    @Test
    void testVoteAlreadyVotedThrows() {
        when(petitionRepository.findById(1L)).thenReturn(Optional.of(petition));
        when(petitionVoteRepository.existsByPetitionIdAndStudentId(1L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> petitionService.vote(1L, 100L, PetitionVote.VoteVariant.YES))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("already voted");
    }

    @Test
    void testVotePetitionEndedThrows() {
        petition.setEndDate(Date.from(LocalDateTime.now().minusDays(1)
            .atZone(ZoneId.systemDefault()).toInstant()));
        when(petitionRepository.findById(1L)).thenReturn(Optional.of(petition));

        assertThatThrownBy(() -> petitionService.vote(1L, 100L, PetitionVote.VoteVariant.YES))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("already ended");
    }
}
