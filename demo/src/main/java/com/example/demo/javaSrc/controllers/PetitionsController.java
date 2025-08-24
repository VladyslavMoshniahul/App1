package com.example.demo.javaSrc.controllers;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.comments.PetitionsComment;
import com.example.demo.javaSrc.comments.PetitionsCommentService;
import com.example.demo.javaSrc.comments.PetitonCommentDto;
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
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public PetitionsController(PetitionService petitionService, PeopleService userService,
            PeopleController userController,
            PetitionsCommentService petitionsCommentService, ClassService classService,
            SimpMessagingTemplate messagingTemplate) {
        this.petitionService = petitionService;
        this.userService = userService;
        this.userController = userController;
        this.petitionsCommentService = petitionsCommentService;
        this.classService = classService;
        this.messagingTemplate = messagingTemplate;
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

        LocalDateTime startDate =  req.startDate();
        LocalDateTime endDate = req.endDate();
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

        messagingTemplate.convertAndSend("/topic/petitions/new", dto);
        if (saved.getSchoolId() != null) {
            messagingTemplate.convertAndSend("/topic/petitions/school/" + saved.getSchoolId(), dto);
        }
        if (saved.getClassId() != null) {
            messagingTemplate.convertAndSend("/topic/petitions/class/" + saved.getClassId(), dto);
        }

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
                                .getBySchoolClassAndRole(petition.getSchoolId(), petition.getClassId(),
                                        People.Role.STUDENT)
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

            Petition updatedPetition = petitionService.getPetitionById(id);
            if (updatedPetition != null) {
                int totalStudents = petitionService.getTotalStudentsForPetition(updatedPetition);
                PetitionDto updatedDto = PetitionDto.from(updatedPetition, totalStudents);
                messagingTemplate.convertAndSend("/topic/petitions/vote/" + id, updatedDto);
                if (updatedPetition.getSchoolId() != null) {
                    messagingTemplate.convertAndSend("/topic/petitions/school/" + updatedPetition.getSchoolId(),
                            updatedDto);
                }
                if (updatedPetition.getClassId() != null) {
                    messagingTemplate.convertAndSend("/topic/petitions/class/" + updatedPetition.getClassId(),
                            updatedDto);
                }
                messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/petitions/vote/status",
                        "Ваш голос за петицію " + id + " (" + req.getVote() + ") зараховано.");
            }

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
    @PatchMapping("/{id}/directorApprove")
    public ResponseEntity<Void> directorApprove(
            @PathVariable Long id) {

        Petition petition = petitionService.getPetitionById(id);
        if (petition == null || petition.getDirectorsDecision() != Petition.DirectorsDecision.PENDING) {
            return ResponseEntity.badRequest().build();
        }
        petition.setDirectorsDecision(Petition.DirectorsDecision.APPROVED);
        Petition updated = petitionService.createPetition(petition);

        int totalStudents = petitionService.getTotalStudentsForPetition(updated);
        PetitionDto dto = PetitionDto.from(updated, totalStudents);

        messagingTemplate.convertAndSend("/topic/petitions/decision/" + id, dto);
        if (updated.getSchoolId() != null) {
            messagingTemplate.convertAndSend("/topic/petitions/school/" + updated.getSchoolId(), dto);
        }
        if (updated.getClassId() != null) {
            messagingTemplate.convertAndSend("/topic/petitions/class/" + updated.getClassId(), dto);
        }
        if (updated.getCreatedBy() != null) {
            messagingTemplate.convertAndSendToUser(updated.getCreatedBy().toString(),
                    "/queue/petitions/my-petition/decision",
                    "Ваша петиція '" + updated.getTitle() + "' схвалена директором.");
        }
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('DIRECTOR')")
    @PatchMapping("/{id}/directorReject")
    public ResponseEntity<Void> directorReject(
            @PathVariable Long id) {

        Petition petition = petitionService.getPetitionById(id);
        if (petition == null || petition.getDirectorsDecision() != Petition.DirectorsDecision.PENDING) {
            return ResponseEntity.badRequest().build();
        }

        petition.setDirectorsDecision(Petition.DirectorsDecision.REJECTED);
        Petition updated = petitionService.createPetition(petition);

        int totalStudents = petitionService.getTotalStudentsForPetition(updated);
        PetitionDto dto = PetitionDto.from(updated, totalStudents);

        messagingTemplate.convertAndSend("/topic/petitions/decision/" + id, dto);
        if (updated.getSchoolId() != null) {
            messagingTemplate.convertAndSend("/topic/petitions/school/" + updated.getSchoolId(), dto);
        }
        if (updated.getClassId() != null) {
            messagingTemplate.convertAndSend("/topic/petitions/class/" + updated.getClassId(), dto);
        }
        if (updated.getCreatedBy() != null) {
            messagingTemplate.convertAndSendToUser(updated.getCreatedBy().toString(),
                    "/queue/petitions/my-petition/decision",
                    "Ваша петиція '" + updated.getTitle() + "' відхилена директором.");
        }
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('DIRECTOR')")
    @GetMapping("/petitionsForDirector")
    public List<Petition> getPetitionsForDirector(Authentication auth,
            @RequestParam(required = false) String className) {
        People me = userController.currentUser(auth);

        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(me.getSchoolId(), className);

        List<Petition> petitions;
        long countStudents;

        if (schoolClass == null) {
            petitions = petitionService.getPetitionBySchool(me.getSchoolId());
            countStudents = userService.getCountBySchoolIdAndRole(me.getSchoolId(), People.Role.STUDENT);
        } else {
            petitions = petitionService.getPetitionByClassAndSchool(schoolClass.getId(), me.getSchoolId());
            countStudents = userService.getCountBySchoolIdAndClassIdAndRole(
                    me.getSchoolId(), schoolClass.getId(), People.Role.STUDENT);
        }

        for (Petition petition : petitions) {
            if (petition.getCurrentPositiveVoteCount() <= countStudents / 2 + 1) {
                petition.setDirectorsDecision(Petition.DirectorsDecision.NOT_ENOUGH_VOTING);
            } else {
                petition.setDirectorsDecision(Petition.DirectorsDecision.PENDING);
            }
        }

        return petitions.stream()
                .filter(p -> p.getDirectorsDecision() == Petition.DirectorsDecision.PENDING)
                .collect(Collectors.toList());
    }

    @PostMapping("/comments")
    public ResponseEntity<PetitionsComment> addComment(@RequestBody PetitionsComment comment) {
        PetitionsComment saved = petitionsCommentService.addComment(comment);
        messagingTemplate.convertAndSend("/topic/petitions/" + saved.getPetitionId() + "/comments/new", saved);
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
    public List<PetitonCommentDto> getCommentsByPetition(@PathVariable Long petitionId) {
        List<PetitionsComment> comments = petitionsCommentService.getCommentsByPetitionId(petitionId);

        return comments.stream()
                .map(c -> {
                    People author = userService.getPeopleById(c.getUserId()); 
                    return new PetitonCommentDto(c, author);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/comments/user/{userId}")
    public List<PetitionsComment> getCommentsByUser(@PathVariable Long userId) {
        return petitionsCommentService.getCommentsByUserId(userId);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        Long petitionId = null;
        PetitionsComment comment = petitionsCommentService.getComment(id);
        if (comment != null && comment.getPetitionId() != null) {
            petitionId = comment.getPetitionId();
        }

        petitionsCommentService.deleteComment(id);
        if (petitionId != null) {
            messagingTemplate.convertAndSend("/topic/petitions/" + petitionId + "/comments/deleted",
                    "Коментар ID " + id + " видалено.");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/petition/{petitionId}")
    public ResponseEntity<Void> deleteCommentsByPetition(@PathVariable Long petitionId) {
        petitionsCommentService.deleteCommentsByPetitionId(petitionId);
        messagingTemplate.convertAndSend("/topic/petitions/" + petitionId + "/comments/allDeleted",
                "Усі коментарі для петиції ID " + petitionId + " видалено.");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/user/{userId}")
    public ResponseEntity<Void> deleteCommentsByUser(@PathVariable Long userId) {

        List<PetitionsComment> commentsToDelete = petitionsCommentService.getCommentsByUserId(userId);
        petitionsCommentService.deleteCommentsByUserId(userId);
        commentsToDelete.forEach(comment -> {
            if (comment.getPetitionId() != null) {
                messagingTemplate.convertAndSend("/topic/petitions/" + comment.getPetitionId() + "/comments/deleted",
                        "Коментар ID " + comment.getId() + " користувача " + userId + " видалено.");
            }
        });
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/my-comments/deleted",
                "Ваші коментарі були видалені.");
        return ResponseEntity.noContent().build();
    }
}