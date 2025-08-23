DROP DATABASE IF EXISTS `App1`;
CREATE DATABASE `App1` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `App1`;

/*SCHOOLS AND CLASSES*/
CREATE TABLE IF NOT EXISTS `schools` (
  `id`       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name`     VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `classes` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `school_id` BIGINT NOT NULL,
  `name`      VARCHAR(50) NOT NULL,
  UNIQUE KEY (`school_id`,`name`),
  FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

/*people*/
CREATE TABLE IF NOT EXISTS `people` (
  `id`             BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `school_id`      BIGINT NULL,
  `class_id`       BIGINT NULL,
  `first_name`     VARCHAR(50) NOT NULL,
  `last_name`      VARCHAR(50) NOT NULL,
  `email`          VARCHAR(100) NOT NULL UNIQUE,
  `password_hash`  VARCHAR(255) NOT NULL,
  `role`           ENUM('TEACHER','STUDENT','PARENT', 'DIRECTOR', 'ADMIN') NOT NULL,
  `about_me`       TEXT NULL,
  `date_of_birth`  DATE NOT NULL,
  FOREIGN KEY (`school_id`) REFERENCES `schools`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`class_id`)  REFERENCES `classes`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

/*VOTES*/
CREATE TABLE IF NOT EXISTS `voting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `school_id` BIGINT NOT NULL,
    `class_id` BIGINT NULL,
    `title` VARCHAR(250) NOT NULL,
    `description` TEXT NULL,
    `start_date` DATETIME NOT NULL,
    `end_date` DATETIME NOT NULL,
    `created_by` BIGINT NOT NULL,
    `multiple_choice` BOOLEAN NOT NULL DEFAULT FALSE,
    `voting_level` ENUM(
        'SCHOOL',
        'ACLASS',
        'TEACHERS_GROUP',
        'SELECTED_USERS'
    ) NOT NULL DEFAULT 'SCHOOL',
    `status` ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    `variants` JSON NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `voting_variant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `voting_id` BIGINT NOT NULL,
    `text` VARCHAR(255) NOT NULL, 
    FOREIGN KEY (`voting_id`) REFERENCES `voting`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `voting_vote` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `voting_id` BIGINT NOT NULL,
    `variant_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `vote_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`voting_id`) REFERENCES `voting`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`variant_id`) REFERENCES `voting_variant`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `people`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `unique_vote_per_variant` (`voting_id`, `user_id`, `variant_id`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `voting_participant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `voting_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    FOREIGN KEY (`voting_id`) REFERENCES `voting`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `people`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `unique_participant` (`voting_id`, `user_id`)
) ENGINE=InnoDB;

/*PETITIONS*/
CREATE TABLE IF NOT EXISTS `petitions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `title` VARCHAR(250) NOT NULL,
  `description` TEXT NULL,
  `start_date` DATETIME NOT NULL,
  `end_date` DATETIME NOT NULL,
  `created_by` BIGINT NOT NULL,
  `school_id` BIGINT NOT NULL,
  `class_id` BIGINT NULL,
  `status` ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN',
  `current_positive_vote_count` INT NOT NULL DEFAULT 0,
  `directors_decision` ENUM('APPROVED', 'REJECTED', 'PENDING', 'NOT_ENOUGH_VOTING') NOT NULL DEFAULT 'NOT_ENOUGH_VOTING'
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `petition_votes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `petition_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `vote` ENUM('YES', 'NO') NOT NULL,
  `voted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (`petition_id`, `student_id`),
  FOREIGN KEY (`petition_id`) REFERENCES `petitions`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`student_id`) REFERENCES `people`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `petitions_comments`(
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `petition_id` BIGINT NOT NULL,
  `text` TEXT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `people`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`petition_id`) REFERENCES `petitions`(`id`) ON DELETE CASCADE
)ENGINE=InnoDB;

/*EVENTS*/
CREATE TABLE IF NOT EXISTS `events` (
  `id`               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `school_id`        BIGINT NOT NULL,
  `class_id`         BIGINT NULL,
  `title`            VARCHAR(100) NOT NULL,
  `content`          TEXT NULL,
  `location_or_link` TEXT NULL,
  `duration`         INT NOT NULL,
  `start_event`      DATETIME NOT NULL,
  `event_type`       ENUM('EXAM','TEST','SCHOOL_EVENT','PARENTS_MEETING','PERSONAL') NOT NULL,
  `created_by`       BIGINT NOT NULL,
  FOREIGN KEY (`school_id`)   REFERENCES `schools`(`id`)   ON DELETE CASCADE,
  FOREIGN KEY (`class_id`)    REFERENCES `classes`(`id`)   ON DELETE SET NULL,
  FOREIGN KEY (`created_by`)  REFERENCES `people`(`id`)     ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `event_files` (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  event_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_type VARCHAR(100) NOT NULL,
  file_data LONGBLOB NOT NULL,  
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `events_comments` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `event_id`   BIGINT NOT NULL,
  `user_id`    BIGINT NOT NULL,
  `content`    TEXT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`event_id`)   REFERENCES `events`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`)    REFERENCES `people`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;
/*Invatitions*/
CREATE TABLE IF NOT EXISTS `invitations` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `event_id`   BIGINT NULL,
  `vote_id`    BIGINT NULL,
  `user_id`    BIGINT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`event_id`)   REFERENCES `events`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`)    REFERENCES `people`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`vote_id`)    REFERENCES `voting`(`id`) ON DELETE CASCADE,
  CONSTRAINT chk_event_or_vote CHECK (
    (event_id IS NOT NULL AND vote_id IS NULL) OR
    (event_id IS NULL AND vote_id IS NOT NULL)
  )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `user_invitations_status` (
  `id`             BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `invitation_id`  BIGINT NOT NULL,
  `user_id`        BIGINT NOT NULL,
  `status`         ENUM('pending','accepted','declined') NOT NULL DEFAULT 'pending',
  `updated_at`     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uq_invitation_user` (`invitation_id`,`user_id`),
  FOREIGN KEY (`invitation_id`) REFERENCES `invitations`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`)       REFERENCES `people`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

/* TASKS */
CREATE TABLE IF NOT EXISTS `tasks` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `school_id`  BIGINT NOT NULL,
  `class_id`   BIGINT NULL,
  `title`      VARCHAR(100) NOT NULL,
  `content`    TEXT NULL,
  `deadline`   DATETIME NOT NULL,
  FOREIGN KEY (`school_id`)  REFERENCES `schools`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`class_id`)   REFERENCES `classes`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`event_id`)   REFERENCES `events`(`id`)  ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `user_task_status` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`      BIGINT NOT NULL,
  `task_id`      BIGINT NOT NULL,
  `is_completed` BOOLEAN NOT NULL DEFAULT FALSE,
  `completed_at` TIMESTAMP NULL,
  UNIQUE KEY `uq_user_task` (`user_id`,`task_id`),
  FOREIGN KEY (`user_id`)   REFERENCES `people`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`task_id`)   REFERENCES `tasks`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

