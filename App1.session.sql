USE `App1`;

INSERT INTO `admins`
(`first_name`, `last_name`, `email`, `password_hash`, `about_me`, `date_of_birth`)
VALUES
('Admin', 'Test', 'admin@example.com',
 '$2a$10$69zcW51D.VXXT/b78tS.XupfshEa22/pUBe8Njip4Ykm3TFEVw8LC',
 'Admin account', '2009-11-30');
