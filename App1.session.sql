USE `App1`;

INSERT INTO `people`
  (`school_id`, `class_id`, `first_name`, `last_name`, `email`, `password_hash`, `role`, `about_me`, `date_of_birth`)
VALUES
   (NULL, NULL, 
   'Admin', 'Admin', 'admin@example.com',
   '$2a$10$69zcW51D.VXXT/b78tS.XupfshEa22/pUBe8Njip4Ykm3TFEVw8LC',
    'ADMIN', 'Test admin account', '1990-01-01');
