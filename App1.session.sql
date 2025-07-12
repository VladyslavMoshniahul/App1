USE `App1`;

INSERT INTO `schools` (`name`) VALUES 
  ('School One'), 
  ('School Two'), 
  ('School Three');

SET @s1 = (SELECT id FROM `schools` WHERE `name` = 'School One');
SET @s2 = (SELECT id FROM `schools` WHERE `name` = 'School Two');
SET @s3 = (SELECT id FROM `schools` WHERE `name` = 'School Three');

INSERT INTO `classes` (`school_id`, `name`)
VALUES
  (@s1, '1A'), (@s1, '1B'), (@s1, '1C'),
  (@s1, '2A'), (@s1, '2B'), (@s1, '2C'),
  (@s1, '3A'), (@s1, '3B'), (@s1, '3C'),
  (@s1, '4A'), (@s1, '4B'), (@s1, '4C'),
  (@s1, '5A'), (@s1, '5B'), (@s1, '5C'),
  (@s1, '6A'), (@s1, '6B'), (@s1, '6C'),
  (@s1, '7A'), (@s1, '7B'), (@s1, '7C'),
  (@s1, '8A'), (@s1, '8B'), (@s1, '8C'),
  (@s1, '9A'), (@s1, '9B'), (@s1, '9C'),
  (@s1, '10A'), (@s1, '10B'), (@s1, '10C'),
  (@s1, '11A'), (@s1, '11B'), (@s1, '11C'),

  (@s2, '1A'), (@s2, '1B'), (@s2, '1C'),
  (@s2, '2A'), (@s2, '2B'), (@s2, '2C'),
  (@s2, '3A'), (@s2, '3B'), (@s2, '3C'),
  (@s2, '4A'), (@s2, '4B'), (@s2, '4C'),
  (@s2, '5A'), (@s2, '5B'), (@s2, '5C'),
  (@s2, '6A'), (@s2, '6B'), (@s2, '6C'),
  (@s2, '7A'), (@s2, '7B'), (@s2, '7C'),
  (@s2, '8A'), (@s2, '8B'), (@s2, '8C'),
  (@s2, '9A'), (@s2, '9B'), (@s2, '9C'),
  (@s2, '10A'), (@s2, '10B'), (@s2, '10C'),
  (@s2, '11A'), (@s2, '11B'), (@s2, '11C'),

  (@s3, '1A'), (@s3, '1B'), (@s3, '1C'),
  (@s3, '2A'), (@s3, '2B'), (@s3, '2C'),
  (@s3, '3A'), (@s3, '3B'), (@s3, '3C'),
  (@s3, '4A'), (@s3, '4B'), (@s3, '4C'),
  (@s3, '5A'), (@s3, '5B'), (@s3, '5C'),
  (@s3, '6A'), (@s3, '6B'), (@s3, '6C'),
  (@s3, '7A'), (@s3, '7B'), (@s3, '7C'),
  (@s3, '8A'), (@s3, '8B'), (@s3, '8C'),
  (@s3, '9A'), (@s3, '9B'), (@s3, '9C'),
  (@s3, '10A'), (@s3, '10B'), (@s3, '10C'),
  (@s3, '11A'), (@s3, '11B'), (@s3, '11C');

INSERT INTO `users`
  (`school_id`, `class_id`, `first_name`, `last_name`, `email`, `password_hash`, `role`, `about_me`, `date_of_birth`)
VALUES
  (@s1, NULL, 
   'Mary', 'Johnson', 'you@example.com',
   '$2a$10$69zcW51D.VXXT/b78tS.XupfshEa22/pUBe8Njip4Ykm3TFEVw8LC',
   'TEACHER',
   'Test teacher account', '1980-06-15'),
  (@s1, NULL, 
   'Alexa', 'Nytrinova', 'director@example.com',
   '$2a$10$69zcW51D.VXXT/b78tS.XupfshEa22/pUBe8Njip4Ykm3TFEVw8LC',
   'DIRECTOR',
   'Test director account', '1970-04-21'),
  (@s1, (SELECT id FROM `classes` WHERE `school_id`=@s1 AND `name`='1A'),
   'Peter', 'Petrov', 'peter.petrov@example.com',
   '$2a$10$69zcW51D.VXXT/b78tS.XupfshEa22/pUBe8Njip4Ykm3TFEVw8LC',
   'STUDENT',
   'Test student account', '2008-09-10'),
  (@s1, (SELECT id FROM `classes` WHERE `school_id`=@s1 AND `name`='1A'),
   'Olga', 'Ivanova', 'olga.ivanova@example.com',
   '$2a$10$69zcW51D.VXXT/b78tS.XupfshEa22/pUBe8Njip4Ykm3TFEVw8LC',
   'PARENT',
   'Test parent account', '1975-12-05'),
   (NULL, NULL, 
   'Admin', 'Admin', 'admin@example.com',
   '$2a$10$69zcW51D.VXXT/b78tS.XupfshEa22/pUBe8Njip4Ykm3TFEVw8LC',
    'ADMIN', 'Test admin account', '1990-01-01');
