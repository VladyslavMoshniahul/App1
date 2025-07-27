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
import org.springframework.messaging.simp.SimpMessagingTemplate; // Додано імпорт
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
import com.example.demo.javaSrc.petitions.Petition;
import com.example.demo.javaSrc.petitions.PetitionCreateRequest;
import com.example.demo.javaSrc.petitions.PetitionDto;
import com.example.demo.javaSrc.petitions.PetitionService;
import com.example.demo.javaSrc.petitions.PetitionVoteRequest;
import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.users.User;
import com.example.demo.javaSrc.users.UserService;

@RestController
@RequestMapping("/api/petitions")
public class PetitionsController {
    @Autowired
    private final PetitionService petitionService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final UserController userController;
    @Autowired
    private final PetitionsCommentService petitionsCommentService;
    @Autowired
    private final ClassService classService;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate; // Додано SimpMessagingTemplate

    public PetitionsController(PetitionService petitionService, UserService userService, UserController userController,
                               PetitionsCommentService petitionsCommentService, ClassService classService, SimpMessagingTemplate messagingTemplate) {
        this.petitionService = petitionService;
        this.userService = userService;
        this.userController = userController;
        this.petitionsCommentService = petitionsCommentService;
        this.classService = classService;
        this.messagingTemplate = messagingTemplate; // Ініціалізація
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/createPetition")
    public ResponseEntity<PetitionDto> createPetition(
            @RequestBody PetitionCreateRequest req,
            Authentication auth) {

        User user = userController.currentUser(auth);
        if (user == null) {
            // Закоментовано: Можливо, сповіщення про неавторизований доступ
            // messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/errors", "Unauthorized access for petition creation.");
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

        // --- WebSocket Integration ---
        // Сповіщаємо про створення нової петиції
        if (saved.getSchoolId() != null) {
            messagingTemplate.convertAndSend("/topic/school/" + saved.getSchoolId() + "/petitions", dto);
        }
        if (saved.getClassId() != null) {
            messagingTemplate.convertAndSend("/topic/class/" + saved.getClassId() + "/petitions", dto);
        } else {
            messagingTemplate.convertAndSend("/topic/petitions/general", dto); // Для петицій без прив'язки до класу
        }
        // --- End WebSocket Integration ---

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
                                .getBySchoolClassAndRole(petition.getSchoolId(), petition.getClassId(), User.Role.STUDENT)
                                .size();
                    } else {
                        totalStudents = userService
                                .getBySchoolClassAndRole(petition.getSchoolId(), null, User.Role.STUDENT)
                                .size();
                    }
                    return PetitionDto.from(petition, totalStudents);
                })
                .toList();

        // --- WebSocket Integration (Optional for GET methods) ---
        // Закоментовано: надсилання списку петицій при запиті
        // messagingTemplate.convertAndSend("/topic/petitions/user/" + userId + "/accessible", dtos);
        // --- End WebSocket Integration ---

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

            // --- WebSocket Integration ---
            // Сповіщаємо про оновлення голосів за петицією
            Petition updatedPetition = petitionService.getPetitionById(id); // Отримуємо оновлену петицію
            if (updatedPetition != null) {
                int totalStudents = petitionService.getTotalStudentsForPetition(updatedPetition);
                PetitionDto updatedDto = PetitionDto.from(updatedPetition, totalStudents);
                messagingTemplate.convertAndSend("/topic/petition/" + id + "/votes", updatedDto);
            }
            // --- End WebSocket Integration ---

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            // --- WebSocket Integration ---
            // Сповіщаємо про помилку голосування конкретному користувачу
            messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/errors", "Помилка голосування: " + ex.getMessage());
            // --- End WebSocket Integration ---
            return ResponseEntity.badRequest()
                    .body("Недопустимый тип голосування: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            // --- WebSocket Integration ---
            // Сповіщаємо про загальну помилку голосування конкретному користувачу
            messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/errors", "Помилка у голосуванні: " + ex.getMessage());
            // --- End WebSocket Integration ---
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
            // --- WebSocket Integration ---
            // Сповіщення про невдалу спробу схвалення
            // messagingTemplate.convertAndSend("/topic/petition/" + id + "/approvalFailed", "Petition not found or not pending for approval.");
            // --- End WebSocket Integration ---
            return ResponseEntity.badRequest().build();
        }
        petition.setDirectorsDecision(Petition.DirectorsDecision.APPROVED);
        petitionService.createPetition(petition); // Використовуйте `updatePetition` якщо такий метод є
        // --- WebSocket Integration ---
        // Сповіщаємо про схвалення петиції директором
        PetitionDto updatedDto = PetitionDto.from(petition, petitionService.getTotalStudentsForPetition(petition));
        messagingTemplate.convertAndSend("/topic/petition/" + id + "/statusUpdate", updatedDto);
        messagingTemplate.convertAndSend("/topic/school/" + petition.getSchoolId() + "/petitionStatus", updatedDto);
        // Також можна сповістити автора петиції
        // messagingTemplate.convertAndSendToUser(petition.getCreatedBy().toString(), "/queue/myPetitionStatus", "Your petition " + id + " has been APPROVED by the director.");
        // --- End WebSocket Integration ---
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('DIRECTOR')")
    @PostMapping("/{id}/directorReject")
    public ResponseEntity<Void> directorReject(
            @PathVariable Long id) {

        Petition petition = petitionService.getPetitionById(id);
        if (petition == null || petition.getDirectorsDecision() != Petition.DirectorsDecision.PENDING) {
            // --- WebSocket Integration ---
            // Сповіщення про невдалу спробу відхилення
            // messagingTemplate.convertAndSend("/topic/petition/" + id + "/rejectionFailed", "Petition not found or not pending for rejection.");
            // --- End WebSocket Integration ---
            return ResponseEntity.badRequest().build();
        }

        petition.setDirectorsDecision(Petition.DirectorsDecision.REJECTED);
        petitionService.createPetition(petition); // Використовуйте `updatePetition` якщо такий метод є
        // --- WebSocket Integration ---
        // Сповіщаємо про відхилення петиції директором
        PetitionDto updatedDto = PetitionDto.from(petition, petitionService.getTotalStudentsForPetition(petition));
        messagingTemplate.convertAndSend("/topic/petition/" + id + "/statusUpdate", updatedDto);
        messagingTemplate.convertAndSend("/topic/school/" + petition.getSchoolId() + "/petitionStatus", updatedDto);
        // Також можна сповістити автора петиції
        // messagingTemplate.convertAndSendToUser(petition.getCreatedBy().toString(), "/queue/myPetitionStatus", "Your petition " + id + " has been REJECTED by the director.");
        // --- End WebSocket Integration ---
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('DIRECTOR')")
    @GetMapping("/petitionsForDirector")
    public List<Petition> getPetitionsForDirector(Authentication auth,
                                                  @RequestParam(required= false) String className) {
        List<Petition> petitions;
        User me = userController.currentUser(auth);
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(me.getSchoolId(), className);
        if (schoolClass == null) {
            petitions = petitionService.getPetitionBySchool(me.getSchoolId());
        }else{
            petitions = petitionService.getPetitionByClassAndSchool(schoolClass.getId(), me.getSchoolId());
        }
        // --- WebSocket Integration (Optional for GET methods) ---
        // Закоментовано: надсилання списку петицій для директора
        // messagingTemplate.convertAndSendToUser(me.getId().toString(), "/queue/directorPetitions", petitions);
        // --- End WebSocket Integration ---
        return petitions;
    }

    @PostMapping("/comments")
    public ResponseEntity<PetitionsComment> addComment(@RequestBody PetitionsComment comment) {
        PetitionsComment saved = petitionsCommentService.addComment(comment);
        // --- WebSocket Integration ---
        // Сповіщаємо про новий коментар до петиції
        if (saved.getPetitionId() != null) {
            messagingTemplate.convertAndSend("/topic/petition/" + saved.getPetitionId() + "/comments", saved);
        }
        // --- End WebSocket Integration ---
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<PetitionsComment> getComment(@PathVariable Long id) {
        PetitionsComment comment = petitionsCommentService.getComment(id);
        if (comment == null) {
            // --- WebSocket Integration (Optional for GET methods) ---
            // messagingTemplate.convertAndSend("/topic/comment/" + id + "/notFound", "Comment with ID " + id + " not found.");
            // --- End WebSocket Integration ---
            return ResponseEntity.notFound().build();
        }
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/comment/" + id, comment);
        // --- End WebSocket Integration ---
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/comments/petition/{petitionId}")
    public List<PetitionsComment> getCommentsByPetition(@PathVariable Long petitionId) {
        List<PetitionsComment> comments = petitionsCommentService.getCommentsByPetitionId(petitionId);
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/petition/" + petitionId + "/commentsList", comments);
        // --- End WebSocket Integration ---
        return comments;
    }

    @GetMapping("/comments/user/{userId}")
    public List<PetitionsComment> getCommentsByUser(@PathVariable Long userId) {
        List<PetitionsComment> comments = petitionsCommentService.getCommentsByUserId(userId);
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/user/" + userId + "/petitionComments", comments);
        // --- End WebSocket Integration ---
        return comments;
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        PetitionsComment commentToDelete = petitionsCommentService.getComment(id);
        if (commentToDelete != null) {
            petitionsCommentService.deleteComment(id);
            // --- WebSocket Integration ---
            // Сповіщаємо про видалення коментаря
            if (commentToDelete.getPetitionId() != null) {
                messagingTemplate.convertAndSend("/topic/petition/" + commentToDelete.getPetitionId() + "/comments/deleted", id);
            }
            // --- End WebSocket Integration ---
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/petition/{petitionId}")
    public ResponseEntity<Void> deleteCommentsByPetition(@PathVariable Long petitionId) {
        petitionsCommentService.deleteCommentsByPetitionId(petitionId);
        // --- WebSocket Integration ---
        // Сповіщаємо про видалення всіх коментарів для петиції
        messagingTemplate.convertAndSend("/topic/petition/" + petitionId + "/comments/allDeleted", petitionId);
        // --- End WebSocket Integration ---
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/user/{userId}")
    public ResponseEntity<Void> deleteCommentsByUser(@PathVariable Long userId) {
        petitionsCommentService.deleteCommentsByUserId(userId);
        // --- WebSocket Integration ---
        // Сповіщаємо про видалення всіх коментарів користувача
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/petitionComments/allDeleted", userId);
        // --- End WebSocket Integration ---
        return ResponseEntity.noContent().build();
    }
}