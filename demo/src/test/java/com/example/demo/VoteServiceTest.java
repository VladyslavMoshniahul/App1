package com.example.demo;

import com.example.demo.javaSrc.people.People;
import com.example.demo.javaSrc.people.PeopleRepository;
import com.example.demo.javaSrc.voting.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;
    @Mock
    private VotingVariantRepository votingVariantRepository;
    @Mock
    private VotingParticipantRepository votingParticipantRepository;
    @Mock
    private VotingVoteRepository votingVoteRepository;
    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private VoteService voteService;

    private Vote mockVote;
    private People mockUser;
    private VotingVariant mockVariant1;
    private VotingVariant mockVariant2;

    @BeforeEach
    void setUp() {
        peopleRepository.deleteAll();
        votingVariantRepository.deleteAll();
        votingParticipantRepository.deleteAll();
        votingVoteRepository.deleteAll();
        voteRepository.deleteAll();

        mockUser = new People();
        mockUser.setId(1L);
        mockUser.setSchoolId(100L);
        mockUser.setClassId(10L);
        mockUser.setRole(People.Role.STUDENT);

        mockVote = new Vote();
        mockVote.setId(1L);
        mockVote.setSchoolId(100L);
        mockVote.setClassId(10L);
        mockVote.setTitle("Test Vote");
        mockVote.setDescription("Description");
        mockVote.setCreatedBy(mockUser.getId());
        mockVote.setStartDate(new Date(System.currentTimeMillis() - 100000)); 
        mockVote.setEndDate(new Date(System.currentTimeMillis() + 86400000)); 
        mockVote.setMultipleChoice(false);
        mockVote.setVotingLevel(Vote.VotingLevel.SCHOOL);
        mockVote.setStatus(Vote.VoteStatus.OPEN);

        mockVariant1 = new VotingVariant();
        mockVariant1.setId(10L);
        mockVariant1.setVote(mockVote);
        mockVariant1.setText("Option A");

        mockVariant2 = new VotingVariant();
        mockVariant2.setId(11L);
        mockVariant2.setVote(mockVote);
        mockVariant2.setText("Option B");
    }

    @Test
    void createVoting_shouldSaveVoteAndVariantsAndParticipants() {
        List<String> variantStrings = Arrays.asList("Variant X", "Variant Y");
        List<Long> participantIds = Arrays.asList(2L, 3L);

        when(voteRepository.save(any(Vote.class))).thenReturn(mockVote);

        
        when(votingVariantRepository.save(any(VotingVariant.class)))
                .thenReturn(mockVariant1) 
                .thenReturn(mockVariant2); 

        when(votingParticipantRepository.save(any(VotingParticipant.class)))
                .thenAnswer(invocation -> { 
                    VotingParticipant p = invocation.getArgument(0);
                    p.setId(100L); 
                    return p;
                });

        Vote createdVote = voteService.createVoting(mockVote, variantStrings, participantIds);

        assertNotNull(createdVote);
        assertEquals(mockVote.getId(), createdVote.getId());
        assertEquals(2, createdVote.getVariants().size()); 

        verify(voteRepository, times(1)).save(mockVote);
        verify(votingVariantRepository, times(2)).save(any(VotingVariant.class));
        verify(votingParticipantRepository, times(2)).save(any(VotingParticipant.class));
    }

    @Test
    void createVoting_shouldHandleNullVariantsAndParticipants() {
        when(voteRepository.save(any(Vote.class))).thenReturn(mockVote);

        Vote createdVote = voteService.createVoting(mockVote, null, null);

        assertNotNull(createdVote);

        assertTrue(createdVote.getVariants() == null || createdVote.getVariants().isEmpty());

        verify(voteRepository, times(1)).save(mockVote);
        verify(votingVariantRepository, never()).save(any(VotingVariant.class));
        verify(votingParticipantRepository, never()).save(any(VotingParticipant.class));
    }


    @Test
    void getVotingById_shouldReturnVoteIfExists() {
        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));

        Vote foundVote = voteService.getVotingById(1L);

        assertNotNull(foundVote);
        assertEquals(mockVote.getId(), foundVote.getId());
        verify(voteRepository, times(1)).findById(1L);
    }

    @Test
    void getVotingById_shouldReturnNullIfNotExists() {
        when(voteRepository.findById(anyLong())).thenReturn(Optional.empty());

        Vote foundVote = voteService.getVotingById(99L);

        assertNull(foundVote);
        verify(voteRepository, times(1)).findById(99L);
    }

    @Test
    void getVotingsByClassAndSchool_shouldReturnCorrectVotings() {
        List<Vote> votes = Collections.singletonList(mockVote);
        when(voteRepository.findByClassIdAndSchoolId(10L, 100L)).thenReturn(votes);

        List<Vote> foundVotes = voteService.getVotingsByClassAndSchool(10L, 100L);

        assertNotNull(foundVotes);
        assertEquals(1, foundVotes.size());
        assertEquals(mockVote.getId(), foundVotes.get(0).getId());
        verify(voteRepository, times(1)).findByClassIdAndSchoolId(10L, 100L);
    }

    @Test
    void getVotingsBySchool_shouldReturnCorrectVotings() {
        List<Vote> votes = Collections.singletonList(mockVote);
        when(voteRepository.findBySchoolId(100L)).thenReturn(votes);

        List<Vote> foundVotes = voteService.getVotingsBySchool(100L);

        assertNotNull(foundVotes);
        assertEquals(1, foundVotes.size());
        verify(voteRepository, times(1)).findBySchoolId(100L);
    }

    @Test
    void getVotingsByTitle_shouldReturnCorrectVotings() {
        List<Vote> votes = Collections.singletonList(mockVote);
        when(voteRepository.findByTitle("Test Vote")).thenReturn(votes);

        List<Vote> foundVotes = voteService.getVotingsByTitle("Test Vote");

        assertNotNull(foundVotes);
        assertEquals(1, foundVotes.size());
        verify(voteRepository, times(1)).findByTitle("Test Vote");
    }

    @Test
    void getVotingsByDescription_shouldReturnCorrectVotings() {
        List<Vote> votes = Collections.singletonList(mockVote);
        when(voteRepository.findByDescription("Description")).thenReturn(votes);

        List<Vote> foundVotes = voteService.getVotingsByDescription("Description");

        assertNotNull(foundVotes);
        assertEquals(1, foundVotes.size());
        verify(voteRepository, times(1)).findByDescription("Description");
    }

    @Test
    void getVotingsByCreatedBy_shouldReturnCorrectVotings() {
        List<Vote> votes = Collections.singletonList(mockVote);
        when(voteRepository.findByCreatedBy(1L)).thenReturn(votes);

        List<Vote> foundVotes = voteService.getVotingsByCreatedBy(1L);

        assertNotNull(foundVotes);
        assertEquals(1, foundVotes.size());
        verify(voteRepository, times(1)).findByCreatedBy(1L);
    }

    @Test
    void getVotingsByStartDateBetween_shouldReturnCorrectVotings() {
        Date start = new Date(System.currentTimeMillis() - 200000);
        Date end = new Date(System.currentTimeMillis() + 200000);
        List<Vote> votes = Collections.singletonList(mockVote);
        when(voteRepository.findByStartDateBetween(any(Date.class), any(Date.class))).thenReturn(votes);

        List<Vote> foundVotes = voteService.getVotingsByStartDateBetween(start, end);

        assertNotNull(foundVotes);
        assertEquals(1, foundVotes.size());
        verify(voteRepository, times(1)).findByStartDateBetween(any(Date.class), any(Date.class));
    }

    @Test
    void getAllVotings_shouldReturnAllVotings() {
        List<Vote> votes = Arrays.asList(mockVote, new Vote());
        when(voteRepository.findAll()).thenReturn(votes);

        List<Vote> allVotes = voteService.getAllVotings();

        assertNotNull(allVotes);
        assertEquals(2, allVotes.size());
        verify(voteRepository, times(1)).findAll();
    }

    @Test
    void deleteVoting_shouldDeleteVoteById() {
        doNothing().when(voteRepository).deleteById(1L);

        voteService.deleteVoting(1L);

        verify(voteRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateVoting_shouldUpdateVoteAndVariantsAndParticipants() {
        Vote updatedVoteData = new Vote();
        updatedVoteData.setSchoolId(200L);
        updatedVoteData.setTitle("Updated Title");
        updatedVoteData.setMultipleChoice(true);
        updatedVoteData.setVotingLevel(Vote.VotingLevel.SELECTED_USERS); 

        List<String> updatedVariantTexts = Collections.singletonList("New Option A");
        List<Long> updatedParticipantUserIds = Collections.singletonList(5L);

        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));
        when(voteRepository.save(any(Vote.class))).thenReturn(mockVote);

        when(votingVariantRepository.findByVoteId(1L)).thenReturn(Collections.singletonList(mockVariant1));
        doNothing().when(votingVariantRepository).delete(any(VotingVariant.class));

        when(votingVariantRepository.save(any(VotingVariant.class))).thenReturn(new VotingVariant());

        when(votingParticipantRepository.findByVoteId(1L)).thenReturn(Collections.singletonList(new VotingParticipant(mockVote, 2L)));
        doNothing().when(votingParticipantRepository).delete(any(VotingParticipant.class));

        when(votingParticipantRepository.save(any(VotingParticipant.class))).thenReturn(new VotingParticipant());

        Vote result = voteService.updateVoting(1L, updatedVoteData, updatedVariantTexts, updatedParticipantUserIds);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals(200L, result.getSchoolId());
        assertTrue(result.isMultipleChoice());
        assertEquals(Vote.VotingLevel.SELECTED_USERS, result.getVotingLevel());

        verify(voteRepository, times(1)).findById(1L);
        verify(voteRepository, times(1)).save(mockVote); 
        verify(votingVariantRepository, times(1)).findByVoteId(1L);
        verify(votingVariantRepository, times(1)).delete(any(VotingVariant.class)); 
        verify(votingVariantRepository, times(1)).save(any(VotingVariant.class)); 
        verify(votingParticipantRepository, times(1)).findByVoteId(1L); 
        verify(votingParticipantRepository, times(1)).delete(any(VotingParticipant.class)); 
        verify(votingParticipantRepository, times(1)).save(any(VotingParticipant.class)); 
    }

    @Test
    void updateVoting_shouldReturnNullIfVoteNotFound() {
        when(voteRepository.findById(anyLong())).thenReturn(Optional.empty());

        Vote result = voteService.updateVoting(99L, new Vote(), Collections.emptyList(), Collections.emptyList());

        assertNull(result);
        verify(voteRepository, times(1)).findById(99L);
        verify(voteRepository, never()).save(any(Vote.class));
        verify(votingVariantRepository, never()).findByVoteId(anyLong());
        verify(votingParticipantRepository, never()).findByVoteId(anyLong());
    }

    @Test
    void updateVoting_shouldNotUpdateVariantsIfVariantTextsIsNull() {
        Vote updatedVoteData = new Vote();
        updatedVoteData.setTitle("Updated Title");

        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));
        when(voteRepository.save(any(Vote.class))).thenReturn(mockVote);

        Vote result = voteService.updateVoting(1L, updatedVoteData, null, Collections.emptyList());

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(votingVariantRepository, never()).findByVoteId(anyLong());
        verify(votingVariantRepository, never()).delete(any(VotingVariant.class));
        verify(votingVariantRepository, never()).save(any(VotingVariant.class));
    }

    @Test
    void updateVoting_shouldNotUpdateParticipantsIfNotSelectedUsersLevel() {
        mockVote.setVotingLevel(Vote.VotingLevel.SCHOOL); 
        Vote updatedVoteData = new Vote();
        updatedVoteData.setVotingLevel(Vote.VotingLevel.SCHOOL); 

        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));
        when(voteRepository.save(any(Vote.class))).thenReturn(mockVote);

        Vote result = voteService.updateVoting(1L, updatedVoteData, Collections.emptyList(), Collections.singletonList(5L));

        assertNotNull(result);
        verify(votingParticipantRepository, never()).findByVoteId(anyLong());
        verify(votingParticipantRepository, never()).delete(any(VotingParticipant.class));
        verify(votingParticipantRepository, never()).save(any(VotingParticipant.class));
    }


    @Test
    void getAccessibleVotingsForUser_shouldReturnSchoolLevelVoteForUserInThatSchool() {
        Vote schoolLevelVote = new Vote();
        schoolLevelVote.setId(2L);
        schoolLevelVote.setSchoolId(100L);
        schoolLevelVote.setVotingLevel(Vote.VotingLevel.SCHOOL);
        schoolLevelVote.setStatus(Vote.VoteStatus.OPEN);
        schoolLevelVote.setEndDate(new Date(System.currentTimeMillis() + 86400000));

        when(voteRepository.findByStatus(Vote.VoteStatus.OPEN)).thenReturn(Collections.singletonList(schoolLevelVote));

        List<Vote> accessibleVotings = voteService.getAccessibleVotingsForUser(mockUser.getId(), 100L, 10L);

        assertNotNull(accessibleVotings);
        assertEquals(1, accessibleVotings.size());
        assertEquals(schoolLevelVote.getId(), accessibleVotings.get(0).getId());
    }

    @Test
    void getAccessibleVotingsForUser_shouldReturnClassLevelVoteForUserInThatClass() {
        Vote classLevelVote = new Vote();
        classLevelVote.setId(3L);
        classLevelVote.setSchoolId(100L);
        classLevelVote.setClassId(10L);
        classLevelVote.setVotingLevel(Vote.VotingLevel.ACLASS);
        classLevelVote.setStatus(Vote.VoteStatus.OPEN);
        classLevelVote.setEndDate(new Date(System.currentTimeMillis() + 86400000));

        when(voteRepository.findByStatus(Vote.VoteStatus.OPEN)).thenReturn(Collections.singletonList(classLevelVote));

        List<Vote> accessibleVotings = voteService.getAccessibleVotingsForUser(mockUser.getId(), 100L, 10L);

        assertNotNull(accessibleVotings);
        assertEquals(1, accessibleVotings.size());
        assertEquals(classLevelVote.getId(), accessibleVotings.get(0).getId());
    }

    @Test
    void getAccessibleVotingsForUser_shouldReturnTeachersGroupVoteForTeacherInThatSchool() {
        mockUser.setRole(People.Role.TEACHER);
        Vote teacherGroupVote = new Vote();
        teacherGroupVote.setId(4L);
        teacherGroupVote.setSchoolId(100L);
        teacherGroupVote.setVotingLevel(Vote.VotingLevel.TEACHERS_GROUP);
        teacherGroupVote.setStatus(Vote.VoteStatus.OPEN);
        teacherGroupVote.setEndDate(new Date(System.currentTimeMillis() + 86400000));

        when(voteRepository.findByStatus(Vote.VoteStatus.OPEN)).thenReturn(Collections.singletonList(teacherGroupVote));
        when(peopleRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

        List<Vote> accessibleVotings = voteService.getAccessibleVotingsForUser(mockUser.getId(), 100L, null);

        assertNotNull(accessibleVotings);
        assertEquals(1, accessibleVotings.size());
        assertEquals(teacherGroupVote.getId(), accessibleVotings.get(0).getId());
    }

    @Test
    void getAccessibleVotingsForUser_shouldNotReturnTeachersGroupVoteForNonTeacher() {
        mockUser.setRole(People.Role.STUDENT); 
        Vote teacherGroupVote = new Vote();
        teacherGroupVote.setId(4L);
        teacherGroupVote.setSchoolId(100L);
        teacherGroupVote.setVotingLevel(Vote.VotingLevel.TEACHERS_GROUP);
        teacherGroupVote.setStatus(Vote.VoteStatus.OPEN);
        teacherGroupVote.setEndDate(new Date(System.currentTimeMillis() + 86400000));

        when(voteRepository.findByStatus(Vote.VoteStatus.OPEN)).thenReturn(Collections.singletonList(teacherGroupVote));
        when(peopleRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

        List<Vote> accessibleVotings = voteService.getAccessibleVotingsForUser(mockUser.getId(), 100L, null);

        assertNotNull(accessibleVotings);
        assertTrue(accessibleVotings.isEmpty());
    }

    @Test
    void getAccessibleVotingsForUser_shouldReturnSelectedUsersVoteIfParticipant() {
        Vote selectedUsersVote = new Vote();
        selectedUsersVote.setId(5L);
        selectedUsersVote.setSchoolId(100L);
        selectedUsersVote.setVotingLevel(Vote.VotingLevel.SELECTED_USERS);
        selectedUsersVote.setStatus(Vote.VoteStatus.OPEN);
        selectedUsersVote.setEndDate(new Date(System.currentTimeMillis() + 86400000));

        VotingParticipant participant = new VotingParticipant(selectedUsersVote, mockUser.getId());

        when(voteRepository.findByStatus(Vote.VoteStatus.OPEN)).thenReturn(Collections.singletonList(selectedUsersVote));
        when(votingParticipantRepository.findByVoteIdAndUserId(selectedUsersVote.getId(), mockUser.getId())).thenReturn(Optional.of(participant));

        List<Vote> accessibleVotings = voteService.getAccessibleVotingsForUser(mockUser.getId(), 100L, 10L);

        assertNotNull(accessibleVotings);
        assertEquals(1, accessibleVotings.size());
        assertEquals(selectedUsersVote.getId(), accessibleVotings.get(0).getId());
    }

    @Test
    void getAccessibleVotingsForUser_shouldNotReturnSelectedUsersVoteIfNotParticipant() {
        Vote selectedUsersVote = new Vote();
        selectedUsersVote.setId(5L);
        selectedUsersVote.setSchoolId(100L);
        selectedUsersVote.setVotingLevel(Vote.VotingLevel.SELECTED_USERS);
        selectedUsersVote.setStatus(Vote.VoteStatus.OPEN);
        selectedUsersVote.setEndDate(new Date(System.currentTimeMillis() + 86400000));

        when(voteRepository.findByStatus(Vote.VoteStatus.OPEN)).thenReturn(Collections.singletonList(selectedUsersVote));
        when(votingParticipantRepository.findByVoteIdAndUserId(selectedUsersVote.getId(), mockUser.getId())).thenReturn(Optional.empty());

        List<Vote> accessibleVotings = voteService.getAccessibleVotingsForUser(mockUser.getId(), 100L, 10L);

        assertNotNull(accessibleVotings);
        assertTrue(accessibleVotings.isEmpty());
    }

    @Test
    void getAccessibleVotingsForUser_shouldNotReturnClosedVotes() {
        mockVote.setStatus(Vote.VoteStatus.CLOSED); 
        when(voteRepository.findByStatus(Vote.VoteStatus.OPEN)).thenReturn(Collections.emptyList()); 

        List<Vote> accessibleVotings = voteService.getAccessibleVotingsForUser(mockUser.getId(), 100L, 10L);

        assertNotNull(accessibleVotings);
        assertTrue(accessibleVotings.isEmpty());
        verify(voteRepository, times(1)).findByStatus(Vote.VoteStatus.OPEN);
    }

    
    @Test
    void closeVoting_shouldSetStatusToClosedAndSave() {
        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));
        when(voteRepository.save(any(Vote.class))).thenReturn(mockVote);

        Vote closedVote = voteService.closeVoting(1L);

        assertNotNull(closedVote);
        assertEquals(Vote.VoteStatus.CLOSED, closedVote.getStatus());
        verify(voteRepository, times(1)).findById(1L);
        verify(voteRepository, times(1)).save(mockVote);
    }

    @Test
    void closeVoting_shouldReturnNullIfVoteNotFound() {
        when(voteRepository.findById(anyLong())).thenReturn(Optional.empty());

        Vote closedVote = voteService.closeVoting(99L);

        assertNull(closedVote);
        verify(voteRepository, times(1)).findById(99L);
        verify(voteRepository, never()).save(any(Vote.class));
    }

    
    @Test
    void closeExpiredVotings_shouldCloseExpiredVotes() {
        Vote expiredVote1 = new Vote();
        expiredVote1.setId(10L);
        expiredVote1.setEndDate(new Date(System.currentTimeMillis() - 1000)); 
        expiredVote1.setStatus(Vote.VoteStatus.OPEN);

        Vote activeVote = new Vote();
        activeVote.setId(11L);
        activeVote.setEndDate(new Date(System.currentTimeMillis() + 100000)); 
        activeVote.setStatus(Vote.VoteStatus.OPEN);

        when(voteRepository.findByStatus(Vote.VoteStatus.OPEN)).thenReturn(Arrays.asList(expiredVote1, activeVote));

        voteService.closeExpiredVotings();

        assertEquals(Vote.VoteStatus.CLOSED, expiredVote1.getStatus()); 
        assertEquals(Vote.VoteStatus.OPEN, activeVote.getStatus()); 

        verify(voteRepository, times(1)).findByStatus(Vote.VoteStatus.OPEN);
        verify(voteRepository, times(1)).saveAll(argThat(list -> {
            long count = StreamSupport.stream(list.spliterator(), false).count();
            boolean hasClosed = StreamSupport.stream(list.spliterator(), false)
                .anyMatch(v -> v.getId().equals(10L) && v.getStatus() == Vote.VoteStatus.CLOSED);
            boolean hasOpen = StreamSupport.stream(list.spliterator(), false)
                .anyMatch(v -> v.getId().equals(11L) && v.getStatus() == Vote.VoteStatus.OPEN);
            return count == 2 && hasClosed && hasOpen;
        }));
    }

    @Test
    void recordVote_shouldReturnFalseIfVoteNotFound() {
        when(voteRepository.findById(anyLong())).thenReturn(Optional.empty());

        boolean result = voteService.recordVote(99L, Collections.singletonList(10L), mockUser.getId());

        assertFalse(result);
        verify(voteRepository, times(1)).findById(99L);
        verify(votingVoteRepository, never()).save(any(VotingVote.class));
    }

    @Test
    void recordVote_shouldReturnFalseIfVoteIsClosed() {
        mockVote.setStatus(Vote.VoteStatus.CLOSED);
        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));

        boolean result = voteService.recordVote(1L, Collections.singletonList(10L), mockUser.getId());

        assertFalse(result);
        verify(voteRepository, times(1)).findById(1L);
        verify(votingVoteRepository, never()).save(any(VotingVote.class));
    }

    @Test
    void recordVote_shouldReturnFalseIfVoteIsExpired() {
        mockVote.setEndDate(new Date(System.currentTimeMillis() - 1000));
        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));

        boolean result = voteService.recordVote(1L, Collections.singletonList(10L), mockUser.getId());

        assertFalse(result);
        verify(voteRepository, times(1)).findById(1L);
        verify(votingVoteRepository, never()).save(any(VotingVote.class));
    }

    @Test
    void recordVote_shouldReturnFalseIfSelectedUsersVoteAndUserIsNotParticipant() {
        mockVote.setVotingLevel(Vote.VotingLevel.SELECTED_USERS);
        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));
        when(votingParticipantRepository.findByVoteIdAndUserId(1L, mockUser.getId())).thenReturn(Optional.empty());

        boolean result = voteService.recordVote(1L, Collections.singletonList(10L), mockUser.getId());

        assertFalse(result);
        verify(votingParticipantRepository, times(1)).findByVoteIdAndUserId(1L, mockUser.getId());
        verify(votingVoteRepository, never()).save(any(VotingVote.class));
    }

    @Test
    void recordVote_shouldReturnFalseForMultipleVariantsInSingleChoiceVote() {
        mockVote.setMultipleChoice(false); 
        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));

        boolean result = voteService.recordVote(1L, Arrays.asList(10L, 11L), mockUser.getId()); 

        assertFalse(result);
        verify(votingVoteRepository, never()).save(any(VotingVote.class));
    }

    @Test
    void recordVote_shouldReturnFalseIfUserAlreadyVotedInSingleChoiceVote() {
        mockVote.setMultipleChoice(false);
        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));

        when(votingVariantRepository.findByVoteId(1L)).thenReturn(Collections.singletonList(mockVariant1));
        when(votingVoteRepository.findByVotingIdAndUserId(1L, mockUser.getId()))
                .thenReturn(Collections.singletonList(new VotingVote())); 

        boolean result = voteService.recordVote(1L, Collections.singletonList(mockVariant1.getId()), mockUser.getId());

        assertFalse(result);
        verify(votingVoteRepository, never()).save(any(VotingVote.class));
    }

    @Test
    void recordVote_shouldRecordVoteSuccessfullyForSingleChoice() {
        mockVote.setMultipleChoice(false);
        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));
        when(votingVariantRepository.findById(mockVariant1.getId())).thenReturn(Optional.of(mockVariant1));
        when(votingVariantRepository.findByVoteId(1L)).thenReturn(Collections.singletonList(mockVariant1));
        when(votingVoteRepository.findByVotingIdAndUserId(1L, mockUser.getId()))
                .thenReturn(Collections.emptyList()); 

        when(votingVoteRepository.save(any(VotingVote.class))).thenReturn(new VotingVote());

        boolean result = voteService.recordVote(1L, Collections.singletonList(mockVariant1.getId()), mockUser.getId());

        assertTrue(result);
        verify(votingVoteRepository, times(1)).save(any(VotingVote.class));
    }

    @Test
    void recordVote_shouldRecordVoteSuccessfullyForMultipleChoice_NewVote() {
        mockVote.setMultipleChoice(true);
        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));
        when(votingVariantRepository.findById(mockVariant1.getId())).thenReturn(Optional.of(mockVariant1));
        when(votingVariantRepository.findById(mockVariant2.getId())).thenReturn(Optional.of(mockVariant2));

        when(votingVoteRepository.findByVotingIdAndUserIdAndVariantId(1L, mockUser.getId(), mockVariant1.getId()))
                .thenReturn(Optional.empty());
        when(votingVoteRepository.findByVotingIdAndUserIdAndVariantId(1L, mockUser.getId(), mockVariant2.getId()))
                .thenReturn(Optional.empty());

        when(votingVoteRepository.save(any(VotingVote.class))).thenReturn(new VotingVote());

        boolean result = voteService.recordVote(1L, Arrays.asList(mockVariant1.getId(), mockVariant2.getId()), mockUser.getId());

        assertTrue(result);
        verify(votingVoteRepository, times(2)).save(any(VotingVote.class)); // Two votes saved
    }
    
    
    @Test
    void recordVote_shouldThrowIllegalArgumentExceptionIfVariantNotFound() {
        mockVote.setMultipleChoice(false);
        when(voteRepository.findById(1L)).thenReturn(Optional.of(mockVote));
        when(votingVariantRepository.findById(anyLong())).thenReturn(Optional.empty()); 

        assertThrows(IllegalArgumentException.class, () ->
                voteService.recordVote(1L, Collections.singletonList(99L), mockUser.getId()));

        verify(votingVoteRepository, never()).save(any(VotingVote.class));
    }   

    @Test
    void getVotingResults_shouldReturnNullWhenVoteDoesNotExist() {
        when(voteRepository.findById(anyLong())).thenReturn(Optional.empty());

        VotingResults results = voteService.getVotingResults(99L);

        assertNull(results);
        verify(voteRepository, times(1)).findById(99L);
        verify(votingVariantRepository, never()).findByVoteId(anyLong());
        verify(votingVoteRepository, never()).countByVotingIdAndVariantId(anyLong(), anyLong());
    }
}

