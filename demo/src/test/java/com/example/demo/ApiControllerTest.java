package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.javaSrc.users.*;
import com.example.demo.javaSrc.petitions.*;
import com.example.demo.javaSrc.voting.*;
import com.example.demo.javaSrc.events.*;
import com.example.demo.javaSrc.school.*;
import com.example.demo.javaSrc.comments.*;
import com.example.demo.javaSrc.worker.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService peopleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SchoolService schoolService;

    @Mock
    private ClassService classService;

    @Mock
    private UserRepository peopleRepository;

    @Mock
    private VoteService voteService;

    @Mock
    private PetitionService petitionService;

    @Mock
    private PetitionRepository petitionRepository;

    @Mock
    private PetitionsCommentService commentService;

    @Mock
    private PetitionsCommentRepository commentRepository;

    @Mock
    private EventService eventService;

    @InjectMocks
    private ApiController apiController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        reset(peopleService, schoolService, classService, voteService, 
              petitionService, commentService, passwordEncoder,eventService);
    }

    @Test
    @WithMockUser(username = "user", roles = {"TEACHER"})
    void testGetAllSchool() throws Exception {
        School school1 = new School();
        school1.setName("School 1");
        School school2 = new School();
        school2.setName("School 2");
        List<School> schools = List.of(school1, school2);

        when(schoolService.getAllSchools()).thenReturn(schools);

        mockMvc.perform(get("/api/schools")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("School 1"))
                .andExpect(jsonPath("$[1].name").value("School 2"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"TEACHER"})
    void testGetAllClasses() throws Exception {
        SchoolClass class1 = new SchoolClass();
        class1.setName("Class 1");
        class1.setSchoolId(1L);
        SchoolClass class2 = new SchoolClass();
        class2.setName("Class 2");
        class2.setSchoolId(1L);
        List<SchoolClass> classes = List.of(class1, class2);

        when(classService.getBySchoolId(1L)).thenReturn(classes);

        mockMvc.perform(get("/api/classes")
                        .param("schoolId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Class 1"))
                .andExpect(jsonPath("$[1].name").value("Class 2"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"TEACHER"})
    void testGetAllUsers() throws Exception {
        User people1 = new User();
        people1.setFirstName("John");
        people1.setLastName("Doe");
        people1.setEmail("jo@gg.com");
        User people2 = new User();
        people2.setFirstName("Jane");
        people2.setLastName("Doe");
        people2.setEmail("ja@gg.com");

        List<User> peopleList = List.of(people1, people2);

        when(peopleService.getAllUsers()).thenReturn(peopleList);

        mockMvc.perform(get("/api/loadUsers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].lastName").value("Doe"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"TEACHER"})
    void testGetUsersByRole() throws Exception {
        User people1 = new User();
        people1.setFirstName("John");
        people1.setLastName("Doe");
        people1.setEmail("jo@gg.com");
        people1.setRole(User.Role.STUDENT);
        User people2 = new User();
        people2.setFirstName("Jane");
        people2.setLastName("Doe");
        people2.setEmail("ja@gg.com");
        people2.setRole(User.Role.STUDENT);

        List<User> peopleList = List.of(people1, people2);

        when(peopleService.getUserByRole("STUDENT")).thenReturn(peopleList);

        mockMvc.perform(get("/api/users/role/STUDENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }


    @Test
    @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
    void testUpdateUserByTeacher_Success() throws Exception {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");

        when(peopleService.updateProfile(eq(userId), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("User"));

        verify(peopleService).updateProfile(eq(userId), any(User.class));
    }

    @Test
    @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
    void testUpdateUserByTeacher_NotFound() throws Exception {
        Long userId = 999L;
        User updatedUser = new User();

        when(peopleService.updateProfile(eq(userId), any(User.class))).thenReturn(null);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound());
    }

    

    @Test
    void testSignPetition_Success() throws Exception {
        Long petitionId = 1L;
        User student = new User();
        student.setId(1L);
        student.setEmail("student@test.com");

        Authentication authentication = new UsernamePasswordAuthenticationToken(student, null, AuthorityUtils.createAuthorityList("ROLE_STUDENT"));

        doNothing().when(petitionService).vote(eq(petitionId), eq(1L), eq(PetitionVote.VoteVariant.YES));

        mockMvc.perform(post("/api/petitions/{id}/vote", petitionId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk());

        verify(petitionService).vote(petitionId, 1L, PetitionVote.VoteVariant.YES);
    }

    @Test
    void testSignPetition_BadRequest() throws Exception {
        Long petitionId = 1L;
        User student = new User();
        student.setId(1L);
        student.setEmail("student@test.com");

        Authentication authentication = new UsernamePasswordAuthenticationToken(student, null, AuthorityUtils.createAuthorityList("ROLE_STUDENT"));

        doThrow(new RuntimeException("Vote failed")).when(petitionService)
                .vote(eq(petitionId), eq(1L), eq(PetitionVote.VoteVariant.YES));

        mockMvc.perform(post("/api/petitions/{id}/vote", petitionId)
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "director@test.com", roles = {"DIRECTOR"})
    void testDirectorApprove_Success() throws Exception {
        Long petitionId = 1L;
        Petition petition = new Petition();
        petition.setId(petitionId);
        petition.setDirectorsDecision(Petition.DirectorsDecision.PENDING);

        when(petitionService.getPetitionById(petitionId)).thenReturn(petition);
        when(petitionService.createPetition(any(Petition.class))).thenReturn(petition);

        mockMvc.perform(post("/api/petitions/{id}/director", petitionId))
                .andExpect(status().isOk());

        verify(petitionService).createPetition(any(Petition.class));
    }

    @Test
    @WithMockUser(username = "director@test.com", roles = {"DIRECTOR"})
    void testDirectorApprove_BadRequest() throws Exception {
        Long petitionId = 1L;
        Petition petition = new Petition();
        petition.setId(petitionId);
        petition.setDirectorsDecision(Petition.DirectorsDecision.APPROVED); 

        when(petitionService.getPetitionById(petitionId)).thenReturn(petition);

        mockMvc.perform(post("/api/petitions/{id}/director", petitionId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void testCastVote_Success() throws Exception {
        Long votingId = 1L;
        List<Long> variantIds = List.of(1L);
        
        User user = new User();
        user.setId(1L);
        
        Vote vote = new Vote();
        vote.setMultipleChoice(false);

        when(voteService.getVotingById(votingId)).thenReturn(vote);
        when(peopleService.findByEmail("user@test.com")).thenReturn(user);
        when(voteService.recordVote(votingId, variantIds, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/voting/{votingId}/vote", votingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(variantIds)))
                .andExpect(status().isOk())
                .andExpect(content().string("Vote recorded successfully"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void testCastVote_MultipleChoiceViolation() throws Exception {
        Long votingId = 1L;
        List<Long> variantIds = List.of(1L, 2L); // Multiple choices
        
        Vote vote = new Vote();
        vote.setMultipleChoice(false); // Single choice only

        when(voteService.getVotingById(votingId)).thenReturn(vote);

        mockMvc.perform(post("/api/voting/{votingId}/vote", votingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(variantIds)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Це одно‑відповідне голосування, виберіть лише один варіант."));
    }


    
    @Test
    @WithMockUser(username = "user", roles = {"TEACHER"})
    void testGetEvents() throws Exception {
        User person1 = new User();
        person1.setFirstName("John");
        person1.setEmail("testemail@gmail.com");
        person1.setPassword("password123");
        person1.setRole(User.Role.STUDENT);
        person1.setSchoolId(1L);
        person1.setClassId(1L); 

        Event event1 = new Event();
        event1.setTitle("Event 1");
        event1.setSchoolId(1L);
        event1.setClassId(11L);
        event1.setCreatedBy(person1.getId());
        event1.setStartEvent(LocalDateTime.now());

        Event event2 = new Event();
        event2.setTitle("Event 2");
        event2.setSchoolId(1L);
        event2.setClassId(11L);
        event2.setCreatedBy(person1.getId());
        event2.setStartEvent(LocalDateTime.now().plusDays(1));

        List<Event> eventsFromService = List.of(event1, event2);

        when(eventService.getEventsForSchool(1L)).thenReturn(eventsFromService);

        mockMvc.perform(get("/api/getEvents")
                .param("schoolId", "1")
                .param("classId", "11")// convert Long to String
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Event 1"))
            .andExpect(jsonPath("$[1].title").value("Event 2"));
    }

}
