insert into application_user (version, id, username,register_number,email,password,
bank_details_completed,
personal_details_completed,
address_details_completed)
values (1, '1', 'admin', 'admin', 'admin@mail.com', '$2a$10$aJjL4/gK8.kcGo0Cftl38uoGm9Bwbw26aDno66lBaPbOjgMY2ekB6', 0,
        0, 0);

insert into user_roles (user_id, roles)
values ('1', 'ADMIN');

INSERT INTO t_districts (name) VALUES
('Ariyalur'),
('Chengalpattu'),
('Chennai'),
('Coimbatore'),
('Cuddalore'),
('Dharmapuri'),
('Dindigul'),
('Erode'),
('Kallakurichi'),
('Kanchipuram'),
('Kanyakumari'),
('Karur'),
('Krishnagiri'),
('Madurai'),
('Mayiladuthurai'),
('Nagapattinam'),
('Namakkal'),
('Nilgiris'),
('Perambalur'),
('Pudukkottai'),
('Ramanathapuram'),
('Ranipet'),
('Salem'),
('Sivaganga'),
('Tenkasi'),
('Thanjavur'),
('Theni'),
('Thiruvallur'),
('Thiruvarur'),
('Thoothukudi'),
('Tiruchirappalli'),
('Tirunelveli'),
('Tirupathur'),
('Tiruppur'),
('Tiruvannamalai'),
('Vellore'),
('Viluppuram'),
('Virudhunagar');


INSERT INTO t_states (name) VALUES
('Andhra Pradesh'),
('Arunachal Pradesh'),
('Assam'),
('Bihar'),
('Chhattisgarh'),
('Goa'),
('Gujarat'),
('Haryana'),
('Himachal Pradesh'),
('Jharkhand'),
('Karnataka'),
('Kerala'),
('Madhya Pradesh'),
('Maharashtra'),
('Manipur'),
('Meghalaya'),
('Mizoram'),
('Nagaland'),
('Odisha'),
('Punjab'),
('Rajasthan'),
('Sikkim'),
('Tamil Nadu'),
('Telangana'),
('Tripura'),
('Uttar Pradesh'),
('Uttarakhand'),
('West Bengal'),
('Andaman and Nicobar Islands'),
('Chandigarh'),
('Dadra and Nagar Haveli and Daman and Diu'),
('Lakshadweep'),
('Delhi'),
('Puducherry');

INSERT INTO t_countries (name) VALUES
('India'),
('Pakistan'),
('China'),
('Nepal'),
('Bhutan'),
('Bangladesh'),
('Myanmar'),
('Sri Lanka'),
('Maldives');

INSERT INTO t_blood_groups (name) VALUES
('A+'),
('A-'),
('B+'),
('B-'),
('AB+'),
('AB-'),
('O+'),
('O-');

INSERT INTO t_departments (name, short_name)
VALUES ('Tamil', 'TML'),
       ('English', 'ENG'),
       ('Commerce', 'COM'),
       ('Commerce CA', 'CA'),
       ('Commerce CS', 'CS'),
       ('Banking Insurance', 'BI'),
       ('Professional Accounting', 'PA'),
       ('Management Studies', 'MS'),
       ('Computer Applications(BCA)', 'BCA'),
       ('Bio-Technology', 'BT'),
       ('Costume Design & Fashion', 'CDF'),
       ('Chemistry', 'CHEM'),
       ('Mathematics', 'MATH'),
       ('Physics', 'PHY'),
       ('Psychology', 'PSY'),
       ('Computer Science', 'CS'),
       ('Computer Technology', 'CT'),
       ('Artificial Intelligence and Data Science – AI & DS', 'AI&DS'),
       ('IT', 'IT');

INSERT INTO t_academic_year (start_year, end_year) VALUES
('2020', '2023'),
('2021', '2024'),
('2022', '2025'),
('2023', '2026'),
('2024', '2027'),
('2025', '2028'),
('2026', '2029'),
('2027', '2030'),
('2028', '2031'),
('2029', '2032');

-- Creating Editor User
insert into application_user (version, id, username,register_number,email,password)
values (1, '2', 'editor', 'editor123', 'editor@mail.com',
        '$2a$10$2n/eokbCM.6rbaUcIF2Rs.d/49AMbEzzDdiovCCD2Acbq9Q/PZPVG');

insert into user_roles (user_id, roles)
values ('2', 'EDITOR');

insert into application_user (version, id, username, register_number, email, password, department_id)
values (1, '3', 'hod', 'hod', 'hod@mail.com', '$2a$10$0cjwVZmupJyONZ2tr69APubG9OJBE3qDICTBP5w.DfXhtEIvnPWDC', 1);

insert into user_roles (user_id, roles)
values ('3', 'HOD');
