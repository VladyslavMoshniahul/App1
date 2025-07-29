package com.example.demo.javaSrc.controllers;

import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.comments.PetitionsComment;
import com.example.demo.javaSrc.comments.PetitionsCommentService;
import com.example.demo.javaSrc.peoples.People;
import com.example.demo.javaSrc.peoples.PeopleService;
import com.example.demo.javaSrc.petitions.Petition;
import com.example.demo.javaSrc.petitions.PetitionCreateRequest;
import com.example.demo.javaSrc.petitions.PetitionDto;
import com.example.demo.javaSrc.petitions.PetitionService;
import com.example.demo.javaSrc.petitions.PetitionVoteRequest;
import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.SchoolClass;

@RestController
@RequestMapping("/api/petitions")
public class PetitionsController {
    @Autowired
    private final PetitionService petitionService;
    @Autowired
    private final PeopleService userService;
    @Autowired
    private final PeopleController userController;
    @Autowired
    private final PetitionsCommentService petitionsCommentService;
    @Autowired
    private final ClassService classService;

    public PetitionsController(PetitionService petitionService, PeopleService userService, PeopleController userController, 
            PetitionsCommentService petitionsCommentService, ClassService classService) {
        this.petitionService = petitionService;
        this.userService = userService;
        this.userController = userController;
        this.petitionsCommentService = petitionsCommentService;
        this.classService = classService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/createPetition")
    public ResponseEntity<PetitionDto> createPetition(
            @RequestBody PetitionCreateRequest req,
            Authentication auth) {

        People user = userController.currentUser(auth);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Petition petition = new Petition();
        petition.setTitle(req.title());
        petition.setDescription(req.description());

        Date startDate = Date.from(
                req.startDate()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant());
        Date endDate = Date.from(
                req.endDate()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant());
        petition.setStartDate(startDate);
        petition.setEndDate(endDate);

        petition.setSchoolId(user.getSchoolId());
        Long classId = req.classId() != null ? req.classId() : user.getClassId();
        petition.setClassId(classId);
        petition.setCreatedBy(user.getId());

        petition.setStatus(Petition.Status.OPEN);
        petition.setCurrentPositiveVoteCount(0);
        petition.setDirectorsDecision(Petition.DirectorsDecision.NOT_ENOUGH_VOTING);

        Petition saved = petitionService.createPetition(petition);

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
                .map(petition -> {
                    int totalStudents;
                    if (petition.getClassId() != null) {
                        totalStudents = userService
                                .getBySchoolClassAndRole(petition.getSchoolId(), petition.getClassId(), People.Role.STUDENT)
                                .size();
                    } else {
                        totalStudents = userService
                                .getBySchoolClassAndRole(petition.getSchoolId(), null, People.Role.STUDENT)
                                .size();
                    }
                    return PetitionDto.from(petition, totalStudents);
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
            People user = userService.findByEmail(auth.getName()); 
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
    @PostMapping("/{id}/directorApprove")
    public ResponseEntity<Void> directorApprove(
            @PathVariable Long id) {

        Petition petition = petitionService.getPetitionById(id);
        if (petition == null || petition.getDirectorsDecision() != Petition.DirectorsDecision.PENDING) {
            return ResponseEntity.badRequest().build();
        }
        petition.setDirectorsDecision(Petition.DirectorsDecision.APPROVED);
        petitionService.createPetition(petition);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('DIRECTOR')")
    @PostMapping("/{id}/directorReject")
    public ResponseEntity<Void> directorReject(
            @PathVariable Long id) {

        Petition petition = petitionService.getPetitionById(id);
        if (petition == null || petition.getDirectorsDecision() != Petition.DirectorsDecision.PENDING) {
            return ResponseEntity.badRequest().build();
        }

        petition.setDirectorsDecision(Petition.DirectorsDecision.REJECTED);
        petitionService.createPetition(petition);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('DIRECTOR')")
    @GetMapping("/petitionsForDirector")
    public List<Petition> getPetitionsForDirector(Authentication auth,
                                                        @RequestParam(required= false) String className) {
        List<Petition> petitions;                                                   
        People me = userController.currentUser(auth);
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(me.getSchoolId(), className);
        if (schoolClass == null) {
            petitions = petitionService.getPetitionBySchool(me.getSchoolId());
        }else{
            petitions = petitionService.getPetitionByClassAndSchool(schoolClass.getId(), me.getSchoolId());
        }
        return petitions;
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
