package com.example.demo.javaSrc.worker;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashSet;

import com.example.demo.javaSrc.comments.PetitionsComment;
import com.example.demo.javaSrc.comments.PetitionsCommentRepository;
import com.example.demo.javaSrc.comments.PetitionsCommentService;
import com.example.demo.javaSrc.petitions.Petition;
import com.example.demo.javaSrc.petitions.PetitionCreateRequest;
import com.example.demo.javaSrc.petitions.PetitionDto;
import com.example.demo.javaSrc.petitions.PetitionRepository;
import com.example.demo.javaSrc.petitions.PetitionService;
import com.example.demo.javaSrc.petitions.PetitionVoteRequest;
import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.school.SchoolService;
import com.example.demo.javaSrc.users.User;
import com.example.demo.javaSrc.users.UserService;
import com.example.demo.javaSrc.voting.Vote;
import com.example.demo.javaSrc.voting.VoteService;
import com.example.demo.javaSrc.voting.VotingParticipant;
import com.example.demo.javaSrc.voting.VotingResults;
import com.example.demo.javaSrc.voting.VotingVariant;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SchoolService schoolService;
    private final ClassService classService;
    private final VoteService voteService;
    private final PetitionService petitionService;
    private final PetitionRepository petitionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PetitionsCommentService petitionsCommentService;
    private final PetitionsCommentRepository petitionsCommentRepository;

    @Autowired
    public ApiController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            SchoolService schoolService,
            ClassService classService,
            VoteService voteService,
            PetitionService petitionService,
            PetitionRepository petitionRepository,
            PetitionsCommentService petitionsCommentService,
            PetitionsCommentRepository petitionsCommentRepository) {

        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.schoolService = schoolService;
        this.classService = classService;
        this.voteService = voteService;
        this.petitionService = petitionService;
        this.petitionRepository = petitionRepository;
        this.petitionsCommentService = petitionsCommentService;
        this.petitionsCommentRepository = petitionsCommentRepository;
    }

    private User currentUser(Authentication auth) {
        return userService.findByEmail(auth.getName());
    }

    @GetMapping("/schools")
    public List<School> getAllSchools() {
        return schoolService.getAllSchools();
    }

    @GetMapping("/classes")
    public List<SchoolClass> getClasses(
            @RequestParam Long schoolId) {
        if (schoolId == null) {
            return List.of();
        }
        return classService.getBySchoolId(schoolId);
    }

    @GetMapping("/teachers")
    public List<User> getTeachers(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String name) {

        User me = currentUser(auth);
        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();

        List<User> teachers;
        if (classId == null) {
            teachers = userService.getBySchoolClassAndRole(sch, null, User.Role.TEACHER);
        } else {
            teachers = new ArrayList<>();
            teachers.addAll(userService.getBySchoolClassAndRole(sch, null, User.Role.TEACHER));
            teachers.addAll(userService.getBySchoolClassAndRole(sch, cls, User.Role.TEACHER));
        }

        if (name != null && !name.isBlank()) {
            teachers.removeIf(p -> !p.getFirstName().toLowerCase().contains(name.toLowerCase()) &&
                    !p.getLastName().toLowerCase().contains(name.toLowerCase()));
        }
        return teachers;
    }

    @GetMapping("/users")
    public List<User> getUsers(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String name) {

        User me = currentUser(auth);
        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();

        List<User> all;
        if (classId == null) {
            all = userService.getBySchoolAndClass(sch, null);
        } else {
            all = new ArrayList<>();
            all.addAll(userService.getBySchoolAndClass(sch, null));
            all.addAll(userService.getBySchoolAndClass(sch, cls));
        }

        if (name != null && !name.isBlank()) {
            all = all.stream()
                    .filter(p -> p.getFirstName().contains(name)
                            || p.getLastName().contains(name))
                    .toList();
        }
        return all;
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR')")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(
            @RequestBody User newUser,
            Authentication auth) {

        if (newUser.getSchoolId() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String rawPass = newUser.getPassword();
        newUser.setPassword(passwordEncoder.encode(rawPass));

        return ResponseEntity.ok(userService.createUser(newUser));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(Authentication auth) {
        String email = auth.getName();
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(@RequestBody User updatedData, Authentication auth) {
        String email = auth.getName();
        User currentUser = userService.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (updatedData.getEmail() != null && !updatedData.getEmail().equals(email)) {
            if (!isValidEmail(updatedData.getEmail())) {
                return ResponseEntity.badRequest().body(null);
            }
            if (userService.findByEmail(updatedData.getEmail()) != null) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        if (updatedData.getFirstName() != null) {
            currentUser.setFirstName(updatedData.getFirstName());
        }
        if (updatedData.getLastName() != null) {
            currentUser.setLastName(updatedData.getLastName());
        }
        if (updatedData.getAboutMe() != null) {
            currentUser.setAboutMe(updatedData.getAboutMe());
        }
        if (updatedData.getDateOfBirth() != null) {
            currentUser.setDateOfBirth(updatedData.getDateOfBirth());
        }
        if (updatedData.getEmail() != null) {
            currentUser.setEmail(updatedData.getEmail());
        }
        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(updatedData.getPassword()));
        }
        Long userId = currentUser.getId();
        User updated = userService.updateUser(userId, currentUser);
        return ResponseEntity.ok(updated);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR')")
    @GetMapping("/loadUsers")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR')")
    @GetMapping("/users/role/{role}")
    public List<User> getUsersByRole(@PathVariable String role) {
        return userService.getUserByRole(role);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR')")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUserByTeacher(@PathVariable Long id, @RequestBody User updatedData) {
        User updated = userService.updateProfile(id, updatedData);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/createPetition")
    public ResponseEntity<PetitionDto> createPetition(
            @RequestBody PetitionCreateRequest req,
            Authentication auth) {

        User u = currentUser(auth);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Petition p = new Petition();
        p.setTitle(req.title());
        p.setDescription(req.description());

        Date startDate = Date.from(
                req.startDate()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant());
        Date endDate = Date.from(
                req.endDate()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant());
        p.setStartDate(startDate);
        p.setEndDate(endDate);

        p.setSchoolId(u.getSchoolId());
        p.setClassId(req.classId());
        p.setCreatedBy(u.getId());

        p.setStatus(Petition.Status.OPEN);
        p.setCurrentPositiveVoteCount(0);
        p.setDirectorsDecision(Petition.DirectorsDecision.NOT_ENOUGH_VOTING);

        Petition saved = petitionService.createPetition(p);

        int totalStudents = petitionService.getTotalStudentsForPetition(saved);
        PetitionDto dto = PetitionDto.from(saved, totalStudents);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/petitions/user/{userId}")
    public ResponseEntity<List<PetitionDto>> getAccessiblePetitions(
            @PathVariable Long userId,
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long classId) {

        List<Petition> schoolPetitions = petitionService.getPetitionBySchool(schoolId);

        List<Petition> classPetitions = classId != null
                ? petitionService.getPetitionByClassAndSchool(classId, schoolId)
                : List.of();

        Set<Petition> merged = new LinkedHashSet<>();
        merged.addAll(schoolPetitions);
        merged.addAll(classPetitions);

        List<PetitionDto> dtos = merged.stream()
                .map(p -> {
                    int totalStudents;
                    if (p.getClassId() != null) {
                        totalStudents = userService
                                .getBySchoolClassAndRole(p.getSchoolId(), p.getClassId(), User.Role.STUDENT)
                                .size();
                    } else {
                        totalStudents = userService
                                .getBySchoolClassAndRole(p.getSchoolId(), null, User.Role.STUDENT)
                                .size();
                    }
                    return PetitionDto.from(p, totalStudents);
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }

    
    @PostMapping(value = "/petitions/{id}/vote", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> votePetition(
            @PathVariable Long id,
            @RequestBody PetitionVoteRequest req,
            Authentication auth) {

        try {
            User user = userService.findByEmail(auth.getName()); 
            petitionService.vote(id, user.getId(), req.getVote());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body("Недопустимый тип голосування: " + req.getVote());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Помилка у голосуванні: " + ex.getMessage());
        }
    }

    @PreAuthorize("hasRole('DIRECTOR')")
    @PostMapping("/petitions/{id}/director")
    public ResponseEntity<Void> directorApprove(
            @PathVariable Long id) {

        Petition p = petitionService.getPetitionById(id);
        if (p == null || p.getDirectorsDecision() != Petition.DirectorsDecision.PENDING) {
            return ResponseEntity.badRequest().build();
        }
        p.setDirectorsDecision(Petition.DirectorsDecision.APPROVED);
        petitionService.createPetition(p);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/createVoting")
    public ResponseEntity<Vote> createVoting(@RequestBody Vote request) {
        try {
            Vote newVote = new Vote();
            newVote.setSchoolId(request.getSchoolId());
            newVote.setClassId(request.getClassId());
            newVote.setTitle(request.getTitle());
            newVote.setDescription(request.getDescription());
            newVote.setCreatedBy(request.getCreatedBy());
            newVote.setStartDate(request.getStartDate());
            newVote.setEndDate(request.getEndDate());
            newVote.setMultipleChoice(request.isMultipleChoice());

            // Defensive: ensure votingLevel is not null and valid
            if (request.getVotingLevel() != null) {
                newVote.setVotingLevel(request.getVotingLevel());
            } else {
                newVote.setVotingLevel(Vote.VotingLevel.SCHOOL);
            }

            newVote.setStatus(Vote.VoteStatus.OPEN);

            // Serialize variants to JSON for variantsJson field
            try {
                if (request.getVariants() != null && !request.getVariants().isEmpty()) {
                    String variantsJson = objectMapper.writeValueAsString(
                            request.getVariants().stream().map(VotingVariant::getText).toList());
                    newVote.setVariantsJson(variantsJson);
                } else {
                    newVote.setVariantsJson("[]");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(null);
            }

            List<String> variantStrings = request.getVariants() != null
                    ? request.getVariants().stream().map(VotingVariant::getText).toList()
                    : List.of();

            List<Long> participantIds = request.getParticipants() != null
                    ? request.getParticipants().stream().map(VotingParticipant::getUserId).toList()
                    : List.of();

            Vote createdVote = voteService.createVoting(newVote, variantStrings, participantIds);
            return new ResponseEntity<>(createdVote, HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/votes")
    public List<Vote> getVotes(@RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId) {
        if (classId != null && schoolId != null) {
            return voteService.getVotingsByClassAndSchool(classId, schoolId);
        } else if (schoolId != null) {
            return voteService.getVotingsBySchool(schoolId);
        } else {
            return voteService.getAllVotings();
        }
    }

    @GetMapping("voting/{id}")
    public ResponseEntity<Vote> getVotingById(@PathVariable Long id) {
        Vote vote = voteService.getVotingById(id);
        if (vote != null) {
            return new ResponseEntity<>(vote, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("voting/user/{userId}")
    public ResponseEntity<List<Vote>> getAccessibleVotings(@PathVariable Long userId,
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long classId) {
        List<Vote> votings = voteService.getAccessibleVotingsForUser(userId, schoolId, classId);
        return new ResponseEntity<>(votings, HttpStatus.OK);
    }

    @PostMapping(value = "voting/{votingId}/vote", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> castVote(
            @PathVariable Long votingId,
            @RequestBody List<Long> variantIds,
            Authentication auth) {

        Vote vote = voteService.getVotingById(votingId);
        if (!vote.isMultipleChoice() && variantIds.size() > 1) {
            return ResponseEntity
                    .badRequest()
                    .body("Це одно‑відповідне голосування, виберіть лише один варіант.");
        }

        Long userId = currentUser(auth).getId();

        boolean success = voteService.recordVote(votingId, variantIds, userId);
        if (success) {
            return ResponseEntity.ok("Vote recorded successfully");
        } else {
            return ResponseEntity.badRequest()
                    .body("Failed to record vote. Check voting status, eligibility, or if you already voted.");
        }
    }

    @GetMapping("voting/{votingId}/results")
    public ResponseEntity<VotingResults> getVotingResults(@PathVariable Long votingId) {
        VotingResults results = voteService.getVotingResults(votingId);
        if (results != null) {
            return new ResponseEntity<>(results, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("voting/{id}")
    public ResponseEntity<Vote> updateVoting(@PathVariable Long id, @RequestBody Vote request) {
        Vote updatedVote = new Vote();
        updatedVote.setSchoolId(request.getSchoolId());
        updatedVote.setClassId(request.getClassId());
        updatedVote.setTitle(request.getTitle());
        updatedVote.setDescription(request.getDescription());
        updatedVote.setCreatedBy(request.getCreatedBy());
        updatedVote.setStartDate(request.getStartDate());
        updatedVote.setEndDate(request.getEndDate());
        updatedVote.setMultipleChoice(request.isMultipleChoice());
        updatedVote.setVotingLevel(request.getVotingLevel());

        List<String> variantStrings = request.getVariants() != null
                ? request.getVariants().stream().map(VotingVariant::getText).toList()
                : List.of();

        List<Long> participantIds = request.getParticipants() != null
                ? request.getParticipants().stream().map(VotingParticipant::getUserId).toList()
                : List.of();

        Vote createdVote = voteService.updateVoting(id, updatedVote, variantStrings, participantIds);
        return new ResponseEntity<>(createdVote, HttpStatus.CREATED);
    }

    @DeleteMapping("voting/{id}")
    public ResponseEntity<Void> deleteVoting(@PathVariable Long id) {
        voteService.deleteVoting(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/comments")
    public ResponseEntity<PetitionsComment> addComment(@RequestBody PetitionsComment comment) {
        PetitionsComment saved = petitionsCommentService.addComment(comment);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<PetitionsComment> getComment(@PathVariable Long id) {
        PetitionsComment comment = petitionsCommentService.getComment(id);
        if (comment == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/comments/petition/{petitionId}")
    public List<PetitionsComment> getCommentsByPetition(@PathVariable Long petitionId) {
        return petitionsCommentService.getCommentsByPetitionId(petitionId);
    }

    @GetMapping("/comments/user/{userId}")
    public List<PetitionsComment> getCommentsByUser(@PathVariable Long userId) {
        return petitionsCommentService.getCommentsByUserId(userId);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        petitionsCommentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/petition/{petitionId}")
    public ResponseEntity<Void> deleteCommentsByPetition(@PathVariable Long petitionId) {
        petitionsCommentService.deleteCommentsByPetitionId(petitionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/user/{userId}")
    public ResponseEntity<Void> deleteCommentsByUser(@PathVariable Long userId) {
        petitionsCommentService.deleteCommentsByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
