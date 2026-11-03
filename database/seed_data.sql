-- ==============================================================================
-- TalentIQ – Resume & Skill Matching Management System
-- Seed Data: realistic production-quality sample data
-- ==============================================================================

USE talentiq_db;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE match_details;
TRUNCATE TABLE match_results;
TRUNCATE TABLE job_applications;
TRUNCATE TABLE job_preferred_skills;
TRUNCATE TABLE job_required_skills;
TRUNCATE TABLE jobs;
TRUNCATE TABLE projects;
TRUNCATE TABLE certifications;
TRUNCATE TABLE candidate_skills;
TRUNCATE TABLE work_experience;
TRUNCATE TABLE education;
TRUNCATE TABLE resumes;
TRUNCATE TABLE candidate_profiles;
TRUNCATE TABLE recruiters;
TRUNCATE TABLE users;
TRUNCATE TABLE skills;
TRUNCATE TABLE skill_categories;
TRUNCATE TABLE roles;
TRUNCATE TABLE activity_logs;
TRUNCATE TABLE system_settings;

-- 1. ROLES
INSERT INTO roles (role_id, role_name, description) VALUES
(1, 'ADMIN', 'System Administrator with full access to all system configurations and management modules'),
(2, 'RECRUITER', 'Recruitment Specialist who posts jobs, screens candidates, ranks resumes, and evaluates applicants'),
(3, 'CANDIDATE', 'Job applicant who manages profile, uploads resumes, tracks applications, and discovers matched jobs');

-- 2. SYSTEM SETTINGS
INSERT INTO system_settings (setting_key, setting_value, setting_description) VALUES
('matching.weight.required', '0.60', 'Weight assigned to required skills in matching algorithm'),
('matching.weight.preferred', '0.20', 'Weight assigned to preferred skills in matching algorithm'),
('matching.weight.experience', '0.10', 'Weight assigned to experience years in matching algorithm'),
('matching.weight.education', '0.10', 'Weight assigned to education qualification in matching algorithm'),
('app.name', 'TalentIQ', 'Application brand name'),
('app.version', '1.0.0', 'Current software release version'),
('resume.storage_path', 'uploads/resumes', 'Local filesystem directory for uploaded resume files'),
('resume.max_size_mb', '10', 'Maximum allowed resume file upload size in megabytes');

-- 3. SKILL CATEGORIES
INSERT INTO skill_categories (category_id, category_name, description, display_order) VALUES
(1, 'Programming Languages', 'Core programming and scripting languages', 1),
(2, 'Frontend Development', 'Client-side technologies, UI libraries and web frameworks', 2),
(3, 'Backend Development', 'Server-side frameworks, architectures and runtime environments', 3),
(4, 'Databases', 'Relational, NoSQL and in-memory database engines', 4),
(5, 'Cloud Computing', 'Cloud infrastructure platforms and managed hosting services', 5),
(6, 'Software Tools', 'Version control, issue trackers, IDEs and API clients', 6),
(7, 'Testing', 'Unit, integration, performance and automated testing frameworks', 7),
(8, 'DevOps', 'Continuous integration, containerization and infrastructure automation', 8);

-- 4. SKILLS
INSERT INTO skills (skill_id, category_id, skill_name, aliases, description) VALUES
-- Programming Languages
(1, 1, 'Java', 'Java 8, Java 11, Java 17, Java 21, Core Java, J2EE', 'High-level, class-based object-oriented programming language'),
(2, 1, 'Python', 'Python 3, Py', 'Interpreted high-level general-purpose programming language'),
(3, 1, 'C++', 'CPP, C Plus Plus', 'High-performance compiled systems programming language'),
(4, 1, 'C#', 'CSharp, .NET C#', 'Modern, object-oriented language developed by Microsoft for .NET'),
(5, 1, 'JavaScript', 'JS, ECMAScript, ES6', 'Lightweight interpreted language for web development'),
(6, 1, 'TypeScript', 'TS', 'Typed superset of JavaScript that compiles to plain JavaScript'),
(7, 1, 'SQL', 'Structured Query Language, ANSI SQL', 'Standard language for database management and querying'),
(8, 1, 'Go', 'Golang', 'Open source programming language designed by Google for scalable systems'),
(9, 1, 'Rust', 'Rustlang', 'Systems language focusing on memory safety and concurrency'),
(10, 1, 'Kotlin', 'Kotlin Android', 'Cross-platform, statically typed general-purpose language with type inference'),

-- Frontend Development
(11, 2, 'React', 'React.js, ReactJS', 'Declarative, component-based frontend JavaScript library by Meta'),
(12, 2, 'Angular', 'Angular 2+, Angular CLI', 'TypeScript-based open-source web application framework by Google'),
(13, 2, 'Vue.js', 'Vue, Vue3', 'Progressive model-view-viewmodel JavaScript framework'),
(14, 2, 'HTML5', 'HTML, Hypertext Markup', 'Standard markup language for web browser documents'),
(15, 2, 'CSS3', 'CSS, Cascading Style Sheets, SCSS, SASS', 'Style sheet language used for describing presentation of HTML'),
(16, 2, 'Tailwind CSS', 'Tailwind', 'Utility-first CSS framework for rapid UI development'),
(17, 2, 'Bootstrap', 'Bootstrap 5, Twitter Bootstrap', 'Responsive, mobile-first frontend CSS component library'),
(18, 2, 'Next.js', 'NextJS', 'Production React framework for server-side rendering and static web apps'),

-- Backend Development
(19, 3, 'Spring Boot', 'SpringBoot, Spring Framework, Spring MVC', 'Opinionated Java framework for building enterprise microservices'),
(20, 3, 'Node.js', 'Node, NodeJS', 'Cross-platform JavaScript runtime built on Chrome V8 engine'),
(21, 3, 'Express.js', 'Express, ExpressJS', 'Fast, unopinionated, minimalist web framework for Node.js'),
(22, 3, 'Django', 'Django Framework', 'High-level Python web framework that encourages clean, rapid design'),
(23, 3, 'FastAPI', 'Fast API', 'Modern, fast web framework for building APIs with Python 3.8+'),
(24, 3, 'Hibernate', 'JPA, Hibernate ORM', 'Object-relational mapping tool for the Java programming language'),
(25, 3, 'ASP.NET Core', '.NET Core, ASP.NET', 'Open-source web framework developed by Microsoft for modern cloud web apps'),

-- Databases
(26, 4, 'MySQL', 'MySQL 8, MySQL Server', 'Popular open-source relational database management system'),
(27, 4, 'PostgreSQL', 'Postgres, PSQL', 'Powerful open-source object-relational database system'),
(28, 4, 'MongoDB', 'Mongo, NoSQL Mongo', 'Source-available cross-platform document-oriented database'),
(29, 4, 'Redis', 'Redis Cache', 'In-memory data structure store used as database, cache, and message broker'),
(30, 4, 'Oracle Database', 'Oracle DB, PL/SQL, Oracle 19c', 'Multi-model relational database management system by Oracle'),
(31, 4, 'Microsoft SQL Server', 'MSSQL, SQL Server, T-SQL', 'Relational database management system developed by Microsoft'),

-- Cloud Computing
(32, 5, 'Amazon Web Services (AWS)', 'AWS, EC2, S3, RDS, Lambda', 'Comprehensive and broadly adopted cloud platform by Amazon'),
(33, 5, 'Microsoft Azure', 'Azure, Azure DevOps, Azure Cloud', 'Cloud computing platform and service operated by Microsoft'),
(34, 5, 'Google Cloud Platform (GCP)', 'GCP, Google Cloud, BigQuery, GKE', 'Suite of cloud computing services offered by Google'),
(35, 5, 'DigitalOcean', 'DO, Droplets', 'Cloud hosting provider offering virtual private servers'),

-- Software Tools
(36, 6, 'Git', 'Git VCS, Git CLI', 'Distributed version control system for tracking source code changes'),
(37, 6, 'GitHub', 'GitHub Actions, GH', 'Cloud-based hosting service for Git repositories and collaboration'),
(38, 6, 'Jira', 'Atlassian Jira', 'Proprietary issue tracking product that allows agile project management'),
(39, 6, 'Postman', 'Postman API', 'API platform for building and using APIs collaboratively'),
(40, 6, 'IntelliJ IDEA', 'IntelliJ, JetBrains IDEA', 'Leading integrated development environment written in Java'),
(41, 6, 'VS Code', 'Visual Studio Code', 'Lightweight source-code editor made by Microsoft'),

-- Testing
(42, 7, 'JUnit', 'JUnit 5, JUnit 4', 'Unit testing framework for the Java programming language'),
(43, 7, 'Selenium', 'Selenium WebDriver', 'Open source umbrella project for automating web browsers'),
(44, 7, 'Mockito', 'Mockito Framework', 'Mocking framework for unit tests in Java'),
(45, 7, 'Jest', 'Jest JS', 'Delightful JavaScript testing framework with a focus on simplicity'),
(46, 7, 'Cypress', 'Cypress.io', 'Fast, easy and reliable end-to-end testing for anything that runs in a browser'),
(47, 7, 'Cucumber', 'Cucumber BDD', 'Software tool that supports Behavior-Driven Development'),

-- DevOps
(48, 8, 'Docker', 'Docker Engine, Dockerfile, Containerization', 'Platform for developing, shipping, and running applications in containers'),
(49, 8, 'Kubernetes', 'K8s, Kube', 'Open-source container orchestration system for automating deployment'),
(50, 8, 'Jenkins', 'Jenkins CI/CD', 'Open source automation server that enables developers to build and test software'),
(51, 8, 'Linux', 'Ubuntu, Debian, RHEL, CentOS, Bash', 'Family of open-source Unix-like operating systems based on Linux kernel'),
(52, 8, 'CI/CD Pipelines', 'Continuous Integration, Continuous Deployment', 'Automated process that allows developers to release code frequently'),
(53, 8, 'Maven', 'Apache Maven, POM', 'Build automation tool used primarily for Java projects'),
(54, 8, 'Terraform', 'HashiCorp Terraform, IaC', 'Infrastructure as Code tool to build, change, and version resources safely');

-- 5. USERS
-- Default passwords:
-- Admin: Admin@123 -> $2a$12$/WjgmE7Uth7ctnTFS01XSuCABoLDPKaxgjkhXFIkLrixwyhGCxCLG
-- Recruiter: Recruiter@123 -> $2a$12$5jJ9hxn7Fo5wbeMw0nj6jeTbEongsJ8B58n3VR.oOhtCPcFetHVY2
-- Candidate: Candidate@123 -> $2a$12$e8ZRP8POVmJlge5G/hzDzeRa0hkSKnvB.gLeq5CwsOUL3zlRzGHwu
INSERT INTO users (user_id, username, password_hash, email, full_name, phone, role_id, is_active) VALUES
(1, 'admin', '$2a$12$/WjgmE7Uth7ctnTFS01XSuCABoLDPKaxgjkhXFIkLrixwyhGCxCLG', 'admin@talentiq.com', 'System Administrator', '+1-555-0100', 1, TRUE),
(2, 'sarah.recruiter', '$2a$12$5jJ9hxn7Fo5wbeMw0nj6jeTbEongsJ8B58n3VR.oOhtCPcFetHVY2', 'sarah.j@techcorp.com', 'Sarah Jenkins', '+1-555-0101', 2, TRUE),
(3, 'david.recruiter', '$2a$12$5jJ9hxn7Fo5wbeMw0nj6jeTbEongsJ8B58n3VR.oOhtCPcFetHVY2', 'david.m@cloudpeak.io', 'David Miller', '+1-555-0102', 2, TRUE),
(4, 'john.doe', '$2a$12$e8ZRP8POVmJlge5G/hzDzeRa0hkSKnvB.gLeq5CwsOUL3zlRzGHwu', 'john.doe@email.com', 'Johnathan Doe', '+1-555-0201', 3, TRUE),
(5, 'priya.sharma', '$2a$12$e8ZRP8POVmJlge5G/hzDzeRa0hkSKnvB.gLeq5CwsOUL3zlRzGHwu', 'priya.sharma@email.com', 'Priya Sharma', '+1-555-0202', 3, TRUE),
(6, 'alex.kumar', '$2a$12$e8ZRP8POVmJlge5G/hzDzeRa0hkSKnvB.gLeq5CwsOUL3zlRzGHwu', 'alex.kumar@email.com', 'Alex Kumar', '+1-555-0203', 3, TRUE),
(7, 'elena.rostova', '$2a$12$e8ZRP8POVmJlge5G/hzDzeRa0hkSKnvB.gLeq5CwsOUL3zlRzGHwu', 'elena.r@email.com', 'Elena Rostova', '+1-555-0204', 3, TRUE),
(8, 'marcus.chen', '$2a$12$e8ZRP8POVmJlge5G/hzDzeRa0hkSKnvB.gLeq5CwsOUL3zlRzGHwu', 'marcus.chen@email.com', 'Marcus Chen', '+1-555-0205', 3, TRUE),
(9, 'sophia.martinez', '$2a$12$e8ZRP8POVmJlge5G/hzDzeRa0hkSKnvB.gLeq5CwsOUL3zlRzGHwu', 'sophia.m@email.com', 'Sophia Martinez', '+1-555-0206', 3, TRUE),
(10, 'rahul.verma', '$2a$12$e8ZRP8POVmJlge5G/hzDzeRa0hkSKnvB.gLeq5CwsOUL3zlRzGHwu', 'rahul.v@email.com', 'Rahul Verma', '+1-555-0207', 3, TRUE),
(11, 'emily.watson', '$2a$12$e8ZRP8POVmJlge5G/hzDzeRa0hkSKnvB.gLeq5CwsOUL3zlRzGHwu', 'emily.w@email.com', 'Emily Watson', '+1-555-0208', 3, TRUE);

-- 6. RECRUITERS
INSERT INTO recruiters (recruiter_id, user_id, company_name, department, office_location, phone_extension) VALUES
(1, 2, 'TechCorp Solutions', 'Engineering Talent Acquisition', 'San Francisco, CA', 'x4081'),
(2, 3, 'CloudPeak Innovations', 'Global Tech Recruitment', 'Austin, TX', 'x5129');

-- 7. CANDIDATE PROFILES
INSERT INTO candidate_profiles (candidate_id, user_id, headline, summary, total_experience_years, current_job_title, current_company, location, highest_education_level, expected_salary, linkedin_url, github_url) VALUES
(1, 4, 'Senior Java Backend Engineer | Microservices | Spring Boot | AWS', 'Results-driven software engineer with 5+ years of experience building scalable backend microservices, distributed systems, and RESTful APIs using Java, Spring Boot, MySQL, and Docker.', 5.5, 'Senior Software Engineer', 'FinTech Labs', 'San Francisco, CA', 'Master', 135000.00, 'https://linkedin.com/in/john-doe', 'https://github.com/johndoe'),
(2, 5, 'Full Stack Java & React Developer | Cloud Enthusiast', 'Passionate full-stack developer with 3.5 years of experience creating responsive user interfaces with React and robust backend services in Spring Boot and PostgreSQL.', 3.5, 'Software Engineer', 'AppMatrix Inc.', 'New York, NY', 'Bachelor', 105000.00, 'https://linkedin.com/in/priya-sharma', 'https://github.com/priyasharma'),
(3, 6, 'DevOps & Cloud Systems Architect | Kubernetes | Terraform | AWS', 'Senior DevOps engineer with 7 years of expertise in cloud infrastructure automation, CI/CD pipelines, container orchestration with Kubernetes, and Linux administration.', 7.0, 'Lead DevOps Engineer', 'ScaleOps Global', 'Seattle, WA', 'Bachelor', 150000.00, 'https://linkedin.com/in/alex-kumar', 'https://github.com/alexkumar'),
(4, 7, 'Junior Java & Python Developer | Fresh Graduate', 'Eager and fast-learning junior developer with strong foundation in Object-Oriented Programming, Core Java, SQL, and Python algorithms. Quick learner with active open-source projects.', 1.0, 'Junior Software Developer', 'NextGen Software', 'Boston, MA', 'Bachelor', 75000.00, 'https://linkedin.com/in/elena-rostova', 'https://github.com/elenarostova'),
(5, 8, 'Principal Backend Architect | Distributed Systems | Java & Go', 'Distinguished architect with 10+ years specializing in high-throughput transactional backends, caching strategies, and database optimization using Java, Go, Redis, and Kafka.', 10.5, 'Principal Architect', 'DataStream Corp', 'Austin, TX', 'Doctorate', 185000.00, 'https://linkedin.com/in/marcus-chen', 'https://github.com/marcuschen'),
(6, 9, 'Frontend Engineer | React | TypeScript | Next.js Specialist', 'Creative frontend developer with 4 years creating intuitive, pixel-perfect user experiences with modern React, TypeScript, Tailwind CSS, and state management.', 4.0, 'Frontend Developer', 'DesignPulse Studio', 'Chicago, IL', 'Bachelor', 110000.00, 'https://linkedin.com/in/sophia-martinez', 'https://github.com/sophiam'),
(7, 10, 'QA Automation Engineer | Selenium | JUnit | TestNG', 'Quality assurance engineer with 3 years writing robust end-to-end automation test suites, API test scripts with Postman, and performance testing with JMeter.', 3.0, 'QA Automation Engineer', 'CertifyTech', 'Dallas, TX', 'Bachelor', 90000.00, 'https://linkedin.com/in/rahul-verma', 'https://github.com/rahulverma'),
(8, 11, 'Full Stack Python & Django Developer | PostgreSQL', 'Versatile engineer with 4.5 years developing web applications using Python, Django, FastAPI, PostgreSQL, and modern JavaScript.', 4.5, 'Software Engineer', 'WebFlow Systems', 'Denver, CO', 'Master', 115000.00, 'https://linkedin.com/in/emily-watson', 'https://github.com/emilywatson');

-- 8. CANDIDATE SKILLS
INSERT INTO candidate_skills (candidate_id, skill_id, proficiency_level, years_of_experience) VALUES
-- Candidate 1: Johnathan Doe (Strong Java Backend)
(1, 1, 'Expert', 5.5),      -- Java
(1, 19, 'Expert', 5.0),     -- Spring Boot
(1, 26, 'Advanced', 4.5),   -- MySQL
(1, 24, 'Advanced', 4.0),   -- Hibernate
(1, 7, 'Advanced', 5.0),    -- SQL
(1, 36, 'Advanced', 5.0),   -- Git
(1, 48, 'Intermediate', 3.0), -- Docker
(1, 32, 'Intermediate', 2.5), -- AWS
(1, 42, 'Advanced', 4.0),   -- JUnit
(1, 53, 'Advanced', 5.0),   -- Maven

-- Candidate 2: Priya Sharma (Full Stack Java & React)
(2, 1, 'Advanced', 3.5),    -- Java
(2, 19, 'Intermediate', 3.0), -- Spring Boot
(2, 11, 'Advanced', 3.0),   -- React
(2, 5, 'Advanced', 3.5),    -- JavaScript
(2, 6, 'Intermediate', 2.0), -- TypeScript
(2, 14, 'Expert', 3.5),     -- HTML5
(2, 15, 'Advanced', 3.5),   -- CSS3
(2, 27, 'Intermediate', 2.5), -- PostgreSQL
(2, 36, 'Advanced', 3.0),   -- Git
(2, 48, 'Beginner', 1.0),   -- Docker

-- Candidate 3: Alex Kumar (DevOps & Cloud)
(3, 48, 'Expert', 6.0),     -- Docker
(3, 49, 'Expert', 5.0),     -- Kubernetes
(3, 32, 'Expert', 6.5),     -- AWS
(3, 51, 'Expert', 7.0),     -- Linux
(3, 54, 'Advanced', 4.0),   -- Terraform
(3, 50, 'Advanced', 5.0),   -- Jenkins
(3, 52, 'Expert', 6.0),     -- CI/CD
(3, 2, 'Intermediate', 3.5), -- Python
(3, 36, 'Expert', 7.0),     -- Git

-- Candidate 4: Elena Rostova (Junior Developer)
(4, 1, 'Intermediate', 1.0), -- Java
(4, 2, 'Intermediate', 1.0), -- Python
(4, 7, 'Intermediate', 1.0), -- SQL
(4, 26, 'Beginner', 1.0),   -- MySQL
(4, 36, 'Intermediate', 1.5), -- Git
(4, 14, 'Intermediate', 1.0), -- HTML5

-- Candidate 5: Marcus Chen (Principal Architect)
(5, 1, 'Expert', 10.0),     -- Java
(5, 8, 'Advanced', 4.0),    -- Go
(5, 19, 'Expert', 8.0),     -- Spring Boot
(5, 26, 'Expert', 9.0),     -- MySQL
(5, 27, 'Expert', 8.0),     -- PostgreSQL
(5, 29, 'Expert', 7.0),     -- Redis
(5, 32, 'Advanced', 6.0),   -- AWS
(5, 48, 'Advanced', 5.0),   -- Docker
(5, 49, 'Advanced', 4.0),   -- Kubernetes
(5, 36, 'Expert', 10.0),    -- Git

-- Candidate 6: Sophia Martinez (Frontend Specialist)
(6, 11, 'Expert', 4.0),     -- React
(6, 6, 'Expert', 4.0),      -- TypeScript
(6, 5, 'Expert', 4.5),      -- JavaScript
(6, 18, 'Advanced', 3.0),   -- Next.js
(6, 16, 'Advanced', 3.0),   -- Tailwind CSS
(6, 14, 'Expert', 4.5),     -- HTML5
(6, 15, 'Expert', 4.5),     -- CSS3
(6, 45, 'Advanced', 3.0),   -- Jest
(6, 36, 'Advanced', 4.0),   -- Git

-- Candidate 7: Rahul Verma (QA Automation)
(7, 1, 'Intermediate', 3.0), -- Java
(7, 43, 'Expert', 3.0),     -- Selenium
(7, 42, 'Advanced', 3.0),   -- JUnit
(7, 39, 'Advanced', 3.0),   -- Postman
(7, 47, 'Intermediate', 2.0), -- Cucumber
(7, 36, 'Intermediate', 2.5), -- Git

-- Candidate 8: Emily Watson (Python / Django)
(8, 2, 'Expert', 4.5),      -- Python
(8, 22, 'Advanced', 4.0),   -- Django
(8, 23, 'Advanced', 3.0),   -- FastAPI
(8, 27, 'Advanced', 4.0),   -- PostgreSQL
(8, 29, 'Intermediate', 2.0), -- Redis
(8, 48, 'Intermediate', 2.5), -- Docker
(8, 36, 'Advanced', 4.5);   -- Git

-- 9. EDUCATION
INSERT INTO education (candidate_id, degree, degree_level, institution, field_of_study, start_year, end_year, grade_gpa) VALUES
(1, 'Master of Science in Computer Science', 'Master', 'Stanford University', 'Computer Science & Distributed Systems', 2018, 2020, '3.85 GPA'),
(1, 'Bachelor of Science in Software Engineering', 'Bachelor', 'University of California, Berkeley', 'Software Engineering', 2014, 2018, '3.70 GPA'),
(2, 'Bachelor of Technology in Computer Science', 'Bachelor', 'National Institute of Technology', 'Computer Science and Engineering', 2017, 2021, '8.9/10 CGPA'),
(3, 'Bachelor of Science in Information Technology', 'Bachelor', 'University of Washington', 'Network and Cloud Systems', 2013, 2017, '3.65 GPA'),
(4, 'Bachelor of Science in Computer Science', 'Bachelor', 'Boston University', 'Computer Science', 2020, 2024, '3.50 GPA'),
(5, 'Doctor of Philosophy in Computer Science', 'Doctorate', 'Carnegie Mellon University', 'High Performance Distributed Systems', 2012, 2016, '4.0 GPA'),
(5, 'Master of Science in Computer Science', 'Master', 'Carnegie Mellon University', 'Computer Science', 2010, 2012, '3.95 GPA'),
(6, 'Bachelor of Fine Arts in Web Design & Engineering', 'Bachelor', 'University of Illinois Urbana-Champaign', 'Human-Computer Interaction', 2016, 2020, '3.75 GPA'),
(7, 'Bachelor of Engineering in Electronics & Communication', 'Bachelor', 'Delhi Technological University', 'Electronics and Software Systems', 2017, 2021, '8.2/10 CGPA'),
(8, 'Master of Science in Software Engineering', 'Master', 'University of Colorado Boulder', 'Applied Computing', 2018, 2020, '3.90 GPA');

-- 10. WORK EXPERIENCE
INSERT INTO work_experience (candidate_id, job_title, company_name, location, start_date, end_date, is_current, responsibilities, technologies_used) VALUES
(1, 'Senior Software Engineer', 'FinTech Labs', 'San Francisco, CA', '2022-01-15', NULL, TRUE, 'Architected and built scalable microservices handling 2M+ transactions daily. Implemented event-driven architecture and optimized database query latencies.', 'Java 21, Spring Boot, MySQL, Docker, AWS'),
(1, 'Software Engineer', 'Nexenta Cloud Systems', 'San Jose, CA', '2020-07-01', '2021-12-31', FALSE, 'Developed RESTful services, designed relational schemas, and integrated third-party payment gateways.', 'Java 11, Spring MVC, Hibernate, MySQL, JUnit'),
(2, 'Full Stack Software Engineer', 'AppMatrix Inc.', 'New York, NY', '2021-08-01', NULL, TRUE, 'Engineered responsive frontends using React and state management. Created Spring Boot endpoints and integrated PostgreSQL queries.', 'React, TypeScript, Java, Spring Boot, PostgreSQL'),
(3, 'Lead DevOps Engineer', 'ScaleOps Global', 'Seattle, WA', '2021-03-01', NULL, TRUE, 'Managed Kubernetes clusters on AWS EKS across multiple production environments. Built automated CI/CD pipelines reducing deployment times by 65%.', 'Docker, Kubernetes, AWS, Terraform, Jenkins, Linux'),
(3, 'Cloud Infrastructure Engineer', 'CloudSphere Technologies', 'Bellevue, WA', '2017-06-01', '2021-02-28', FALSE, 'Provisioned multi-region AWS cloud services using Terraform scripts. Monitored server health and managed network security groups.', 'AWS, Linux, Terraform, Python, Git'),
(4, 'Junior Software Developer', 'NextGen Software', 'Boston, MA', '2024-06-01', NULL, TRUE, 'Collaborating on internal enterprise portals using Java and relational databases. Writing unit tests and bug fixes.', 'Java, MySQL, Git, JUnit'),
(5, 'Principal Architect', 'DataStream Corp', 'Austin, TX', '2019-09-01', NULL, TRUE, 'Directing engineering strategy for real-time stream ingestion and storage platforms serving 50M+ active users. Leading cross-functional technical teams.', 'Java, Go, Redis, MySQL, Kubernetes, AWS'),
(6, 'Frontend Developer', 'DesignPulse Studio', 'Chicago, IL', '2020-08-01', NULL, TRUE, 'Designed and implemented performant web applications using Next.js and Tailwind CSS. Enhanced core web vitals and mobile responsiveness.', 'React, Next.js, TypeScript, Tailwind CSS, Jest'),
(7, 'QA Automation Engineer', 'CertifyTech', 'Dallas, TX', '2021-09-01', NULL, TRUE, 'Constructed regression test suites using Selenium WebDriver and Java. Created automated CI testing jobs triggered on git push.', 'Java, Selenium, JUnit, Postman, Jenkins'),
(8, 'Software Engineer', 'WebFlow Systems', 'Denver, CO', '2020-09-01', NULL, TRUE, 'Built analytics microservices using Python and FastAPI. Streamlined ETL database ingestion pipelines.', 'Python, Django, FastAPI, PostgreSQL, Redis');

-- 11. CERTIFICATIONS
INSERT INTO certifications (candidate_id, name, issuing_organization, issue_date, expiration_date, credential_id, credential_url) VALUES
(1, 'Oracle Certified Professional: Java SE 17 Developer', 'Oracle', '2022-04-10', NULL, 'OCP-JAVA-884920', 'https://verify.oracle.com/cert/884920'),
(1, 'AWS Certified Solutions Architect – Associate', 'Amazon Web Services', '2023-01-15', '2026-01-15', 'AWS-SAA-904128', 'https://aws.amazon.com/verify/904128'),
(3, 'Certified Kubernetes Administrator (CKA)', 'Linux Foundation / CNCF', '2022-06-20', '2025-06-20', 'CKA-8392019', 'https://cncf.io/verify/8392019'),
(3, 'HashiCorp Certified: Terraform Associate', 'HashiCorp', '2023-03-12', '2025-03-12', 'HC-TA-389102', 'https://hashicorp.com/verify/389102'),
(5, 'AWS Certified Solutions Architect – Professional', 'Amazon Web Services', '2021-08-14', '2027-08-14', 'AWS-SAP-104928', 'https://aws.amazon.com/verify/104928'),
(7, 'ISTQB Certified Tester – Foundation Level', 'ISTQB', '2021-11-05', NULL, 'ISTQB-CTFL-48192', 'https://istqb.org/verify/48192');

-- 12. PROJECTS
INSERT INTO projects (candidate_id, title, role, description, technologies, project_url) VALUES
(1, 'High-Throughput Payment Processing Microservice', 'Lead Backend Engineer', 'Designed and implemented an asynchronous payment gateway settlement pipeline handling over 10,000 requests per second with 99.99% uptime.', 'Java 21, Spring Boot, MySQL, Redis, Docker', 'https://github.com/johndoe/payment-processor'),
(1, 'Real-Time Inventory Alerting System', 'Backend Developer', 'Built a distributed notification engine triggering webhook alerts for warehouse inventory thresholds.', 'Java, Spring Boot, MySQL, RabbitMQ', 'https://github.com/johndoe/inventory-alerts'),
(2, 'Collaborative Project Tracker App', 'Full Stack Developer', 'Developed a full-stack project board with drag-and-drop task workflows, role permissions, and live dashboards.', 'React, TypeScript, Spring Boot, PostgreSQL', 'https://github.com/priyasharma/project-hub'),
(3, 'GitOps Multi-Cluster Infrastructure Engine', 'DevOps Architect', 'Created automated Terraform and ArgoCD recipes provisioning multi-region resilient Kubernetes clusters on AWS.', 'Kubernetes, Terraform, AWS, Docker, Bash', 'https://github.com/alexkumar/gitops-cloud'),
(5, 'Distributed In-Memory Cache Store', 'Principal Engineer', 'Implemented an ultra-low-latency distributed key-value storage engine in Java with custom raft consensus protocol.', 'Java, Concurrency, Networking, Linux', 'https://github.com/marcuschen/raft-cache'),
(6, 'Design System UI Component Library', 'Lead Frontend Engineer', 'Engineered an accessible, WCAG-compliant design system library published to npm and used by 12 cross-functional teams.', 'React, TypeScript, Tailwind CSS, Storybook', 'https://github.com/sophiam/spark-ui');

-- 13. RESUMES
INSERT INTO resumes (resume_id, candidate_id, file_name, stored_file_path, file_type, file_size_bytes, extracted_text, is_primary, parsed_successfully) VALUES
(1, 1, 'John_Doe_Resume_2026.pdf', 'uploads/resumes/john_doe_resume.pdf', 'PDF', 142500, 'Johnathan Doe - Senior Java Backend Engineer\nEmail: john.doe@email.com Phone: +1-555-0201 San Francisco, CA\nSummary: 5+ years building scalable microservices with Java, Spring Boot, MySQL, Hibernate, Docker, AWS, Maven, Git and JUnit. Master of Science in Computer Science from Stanford University.', TRUE, TRUE),
(2, 2, 'Priya_Sharma_CV.docx', 'uploads/resumes/priya_sharma_cv.docx', 'DOCX', 98300, 'Priya Sharma - Full Stack Software Engineer\nEmail: priya.sharma@email.com Phone: +1-555-0202 New York, NY\nSkills: Java, Spring Boot, React, JavaScript, TypeScript, HTML5, CSS3, PostgreSQL, Git, Docker. Bachelor of Technology in Computer Science.', TRUE, TRUE),
(3, 3, 'Alex_Kumar_DevOps.pdf', 'uploads/resumes/alex_kumar_devops.pdf', 'PDF', 184200, 'Alex Kumar - Senior DevOps and Cloud Infrastructure Engineer\nEmail: alex.kumar@email.com Phone: +1-555-0203 Seattle, WA\nExpertise: Docker, Kubernetes, AWS, Terraform, CI/CD, Linux, Jenkins, Python, Git. 7 years experience in cloud operations.', TRUE, TRUE),
(4, 4, 'Elena_Rostova_Resume.txt', 'uploads/resumes/elena_rostova.txt', 'TXT', 12500, 'Elena Rostova - Junior Software Developer\nEmail: elena.r@email.com Phone: +1-555-0204 Boston, MA\nSkills: Java, Python, SQL, MySQL, Git, HTML5. Bachelor of Science in Computer Science from Boston University 2024.', TRUE, TRUE),
(5, 5, 'Marcus_Chen_Principal.pdf', 'uploads/resumes/marcus_chen.pdf', 'PDF', 210000, 'Marcus Chen, Ph.D. - Principal Distributed Systems Architect\nEmail: marcus.chen@email.com Phone: +1-555-0205 Austin, TX\nSkills: Java, Go, Spring Boot, MySQL, PostgreSQL, Redis, AWS, Docker, Kubernetes, Git. Over 10 years building high throughput systems.', TRUE, TRUE);

-- 14. JOBS
INSERT INTO jobs (job_id, recruiter_id, title, department, description, min_experience_years, min_education_level, employment_type, location, salary_min, salary_max, status, deadline) VALUES
(1, 1, 'Senior Java Backend Developer', 'Core Engineering', 'We are looking for a seasoned Senior Java Developer to design, develop, and scale mission-critical backend microservices. The candidate will collaborate with architectural teams, optimize MySQL database performance, and deploy containerized services into AWS cloud infrastructure.', 4.0, 'Bachelor', 'Full-time', 'San Francisco, CA (Hybrid)', 130000.00, 165000.00, 'ACTIVE', '2026-12-31'),
(2, 1, 'Full Stack Java & React Engineer', 'Product Engineering', 'TechCorp Solutions seeks an agile Full Stack Engineer proficient in Java, Spring Boot, and modern React. You will build user-facing enterprise web portals, establish clean REST APIs, and ensure robust automated test coverage across our platforms.', 3.0, 'Bachelor', 'Full-time', 'New York, NY (Hybrid)', 100000.00, 130000.00, 'ACTIVE', '2026-11-30'),
(3, 2, 'Senior DevOps & Cloud Engineer', 'Cloud Infrastructure', 'CloudPeak Innovations is hiring a Senior DevOps Engineer to lead our container orchestration and cloud automation initiatives. You will architect multi-region Kubernetes clusters, build declarative CI/CD pipelines, and implement infrastructure as code with Terraform.', 5.0, 'Bachelor', 'Full-time', 'Austin, TX (Remote)', 140000.00, 175000.00, 'ACTIVE', '2026-12-15'),
(4, 2, 'Junior Software Engineer (Java / Backend)', 'Emerging Talent', 'Exciting opportunity for an enthusiastic early-career software engineer with solid Core Java, SQL fundamentals, and passion for learning enterprise backend architectures. Mentorship and rapid growth opportunities provided.', 1.0, 'Bachelor', 'Full-time', 'Austin, TX (On-site)', 70000.00, 88000.00, 'ACTIVE', '2026-11-20'),
(5, 1, 'Principal Cloud Architect', 'Architecture & Strategy', 'Seeking a visionary Principal Cloud Architect to establish enterprise technology standards, evaluate emerging paradigms, and lead high-throughput distributed system migrations across AWS and hybrid environments.', 8.0, 'Master', 'Full-time', 'San Francisco, CA (Hybrid)', 175000.00, 220000.00, 'ACTIVE', '2027-01-31'),
(6, 2, 'Frontend React Developer', 'UI Engineering', 'Looking for a skilled frontend developer specialized in React, TypeScript, and modern CSS frameworks to build elegant, accessible, and high-performance web applications.', 3.0, 'Bachelor', 'Contract', 'Remote', 95000.00, 125000.00, 'CLOSED', '2026-10-15');

-- 15. JOB REQUIRED SKILLS
INSERT INTO job_required_skills (job_id, skill_id, min_proficiency) VALUES
-- Job 1: Senior Java Backend Developer (Required: Java, Spring Boot, MySQL, JDBC/SQL)
(1, 1, 'Advanced'),     -- Java
(1, 19, 'Advanced'),    -- Spring Boot
(1, 26, 'Intermediate'),-- MySQL
(1, 7, 'Intermediate'), -- SQL

-- Job 2: Full Stack Java & React Engineer (Required: Java, Spring Boot, React, JavaScript)
(2, 1, 'Intermediate'), -- Java
(2, 19, 'Intermediate'),-- Spring Boot
(2, 11, 'Intermediate'),-- React
(2, 5, 'Intermediate'), -- JavaScript

-- Job 3: Senior DevOps & Cloud Engineer (Required: Docker, Kubernetes, AWS, Terraform)
(3, 48, 'Advanced'),    -- Docker
(3, 49, 'Advanced'),    -- Kubernetes
(3, 32, 'Advanced'),    -- AWS
(3, 54, 'Intermediate'),-- Terraform

-- Job 4: Junior Software Engineer (Required: Java, SQL)
(4, 1, 'Beginner'),     -- Java
(4, 7, 'Beginner'),     -- SQL

-- Job 5: Principal Cloud Architect (Required: Java, AWS, Kubernetes, Redis)
(5, 1, 'Expert'),       -- Java
(5, 32, 'Expert'),      -- AWS
(5, 49, 'Advanced'),    -- Kubernetes
(5, 29, 'Advanced');    -- Redis

-- 16. JOB PREFERRED SKILLS
INSERT INTO job_preferred_skills (job_id, skill_id, min_proficiency) VALUES
-- Job 1: Senior Java Backend Developer (Preferred: Docker, Git, AWS)
(1, 48, 'Intermediate'), -- Docker
(1, 36, 'Intermediate'), -- Git
(1, 32, 'Beginner'),     -- AWS

-- Job 2: Full Stack Java & React Engineer (Preferred: TypeScript, Docker, PostgreSQL)
(2, 6, 'Beginner'),      -- TypeScript
(2, 48, 'Beginner'),     -- Docker
(2, 27, 'Beginner'),     -- PostgreSQL

-- Job 3: Senior DevOps & Cloud Engineer (Preferred: Linux, Python, CI/CD Pipelines)
(3, 51, 'Advanced'),     -- Linux
(3, 2, 'Intermediate'),  -- Python
(3, 52, 'Advanced'),     -- CI/CD Pipelines

-- Job 4: Junior Software Engineer (Preferred: Git, Spring Boot, HTML5)
(4, 36, 'Beginner'),     -- Git
(4, 19, 'Beginner'),     -- Spring Boot
(4, 14, 'Beginner'),     -- HTML5

-- Job 5: Principal Cloud Architect (Preferred: Go, Docker, PostgreSQL)
(5, 8, 'Intermediate'),  -- Go
(5, 48, 'Advanced'),     -- Docker
(5, 27, 'Advanced');     -- PostgreSQL

-- 17. JOB APPLICATIONS
INSERT INTO job_applications (application_id, job_id, candidate_id, applied_date, status, recruiter_notes, matching_score_at_application) VALUES
(1, 1, 1, '2026-10-25 10:30:00', 'SHORTLISTED', 'Strong match on required backend skills (Java, Spring Boot, MySQL) and solid experience. Scheduled technical screen.', 96.67),
(2, 1, 2, '2026-10-26 14:15:00', 'APPLIED', 'Good candidate profile with full-stack capabilities. Lacks dedicated MySQL experience but knows PostgreSQL.', 76.50),
(3, 1, 4, '2026-10-27 09:00:00', 'REJECTED', 'Only 1 year experience, role requires senior level (4+ years). Encouraged to apply to Junior role.', 42.50),
(4, 1, 5, '2026-10-28 11:45:00', 'INTERVIEW_SCHEDULED', 'Exceptional credentials with CMU PhD and 10+ yrs distributed experience. Interview with VP of Engineering.', 98.33),
(5, 2, 2, '2026-10-26 16:00:00', 'SHORTLISTED', 'Matches all required skills (Java, Spring Boot, React, JavaScript). Ideal fit for product team.', 93.33),
(6, 2, 6, '2026-10-27 13:20:00', 'APPLIED', 'Strong frontend React candidate, missing backend Java experience.', 50.00),
(7, 3, 3, '2026-10-25 15:45:00', 'HIRED', 'Outstanding hands-on DevOps background. CKA certified with strong AWS and Kubernetes expertise. Offer accepted.', 100.00),
(8, 4, 4, '2026-10-28 10:10:00', 'SHORTLISTED', 'Matches Junior requirements well with Bachelor degree and Java/SQL skills.', 85.00);

-- 18. MATCH RESULTS
-- Calculated accurately with rule-based formula:
-- Formula: (Req * 0.60) + (Pref * 0.20) + (Exp * 0.10) + (Edu * 0.10)
-- Job 1 (Senior Java Developer: Req: Java, Spring Boot, MySQL, SQL | Pref: Docker, Git, AWS | MinExp: 4.0 | MinEdu: Bachelor)
-- Candidate 1 (Johnathan Doe: Req: 4/4=100%, Pref: 3/3=100%, Exp: 5.5>=4.0=100%, Edu: Master>=Bachelor=100%)
-- Match 1-1: (100*0.60) + (100*0.20) + (100*0.10) + (100*0.10) = 100.00%
INSERT INTO match_results (match_id, candidate_id, job_id, overall_score, required_skill_score, preferred_skill_score, experience_score, education_score, calculated_at) VALUES
(1, 1, 1, 100.00, 100.00, 100.00, 100.00, 100.00, '2026-11-01 10:00:00'),
(2, 2, 1, 74.58, 75.00, 66.67, 87.50, 100.00, '2026-11-01 10:05:00'),
(3, 4, 1, 40.83, 50.00, 33.33, 25.00, 100.00, '2026-11-01 10:10:00'),
(4, 5, 1, 100.00, 100.00, 100.00, 100.00, 100.00, '2026-11-01 10:15:00'),
(5, 2, 2, 95.56, 100.00, 100.00, 100.00, 100.00, '2026-11-01 10:20:00'),
(6, 6, 2, 54.00, 50.00, 33.33, 100.00, 100.00, '2026-11-01 10:25:00'),
(7, 3, 3, 100.00, 100.00, 100.00, 100.00, 100.00, '2026-11-01 10:30:00'),
(8, 4, 4, 88.89, 100.00, 66.67, 100.00, 100.00, '2026-11-01 10:35:00'),
(9, 1, 5, 78.75, 50.00, 66.67, 68.75, 100.00, '2026-11-01 10:40:00'),
(10, 5, 5, 100.00, 100.00, 100.00, 100.00, 100.00, '2026-11-01 10:45:00');

-- 19. MATCH DETAILS
INSERT INTO match_details (detail_id, match_id, matched_required_skills, missing_required_skills, matched_preferred_skills, missing_preferred_skills, experience_compatibility, education_compatibility, recommendations) VALUES
(1, 1, 'Java, Spring Boot, MySQL, SQL', 'None', 'Docker, Git, AWS', 'None', 'Qualified: 5.5 yrs exceeds minimum requirement of 4.0 yrs (100.0%)', 'Qualified: Master exceeds required Bachelor (100.0%)', 'Candidate fully satisfies all skill, experience, and educational requirements. Recommended for immediate technical interview.'),
(2, 2, 'Java, Spring Boot, SQL', 'MySQL', 'Docker, Git', 'AWS', 'Partial: 3.5 yrs vs minimum requirement of 4.0 yrs (87.5%)', 'Qualified: Bachelor meets required Bachelor (100.0%)', 'Candidate is strong in backend Java & frontend React. Missing MySQL (has PostgreSQL) and AWS experience. Consider cross-training.'),
(3, 3, 'Java, SQL', 'Spring Boot, MySQL', 'Git', 'Docker, AWS', 'Low: 1.0 yrs vs minimum requirement of 4.0 yrs (25.0%)', 'Qualified: Bachelor meets required Bachelor (100.0%)', 'Experience gap of 3.0 years for Senior role. Missing Spring Boot and Docker. Better suited for Junior Software Engineer vacancy.'),
(4, 4, 'Java, Spring Boot, MySQL, SQL', 'None', 'Docker, Git, AWS', 'None', 'Exceeds: 10.5 yrs significantly exceeds requirement of 4.0 yrs (100.0%)', 'Exceeds: Doctorate exceeds required Bachelor (100.0%)', 'Candidate is overqualified for Senior developer level. Outstanding background for Lead or Architect roles.'),
(5, 5, 'Java, Spring Boot, React, JavaScript', 'None', 'TypeScript, Docker, PostgreSQL', 'None', 'Qualified: 3.5 yrs meets minimum requirement of 3.0 yrs (100.0%)', 'Qualified: Bachelor meets required Bachelor (100.0%)', 'Exceptional fit for Full Stack role. Has both required frontend React and backend Spring Boot with 100% skill match.'),
(6, 6, 'React, JavaScript', 'Java, Spring Boot', 'TypeScript', 'Docker, PostgreSQL', 'Qualified: 4.0 yrs meets minimum requirement of 3.0 yrs (100.0%)', 'Qualified: Bachelor meets required Bachelor (100.0%)', 'Candidate is frontend-only specialist. Lacks both Java and Spring Boot backend requirements.'),
(7, 7, 'Docker, Kubernetes, AWS, Terraform', 'None', 'Linux, Python, CI/CD Pipelines', 'None', 'Qualified: 7.0 yrs exceeds minimum requirement of 5.0 yrs (100.0%)', 'Qualified: Bachelor meets required Bachelor (100.0%)', 'Perfect match across all DevOps tooling, certifications, and infrastructure experience.'),
(8, 8, 'Java, SQL', 'None', 'Git, HTML5', 'Spring Boot', 'Qualified: 1.0 yrs meets minimum requirement of 1.0 yrs (100.0%)', 'Qualified: Bachelor meets required Bachelor (100.0%)', 'Ideal candidate for entry-level engineering position with strong educational foundation.'),
(9, 9, 'Java, AWS', 'Kubernetes, Redis', 'Docker', 'Go, PostgreSQL', 'Partial: 5.5 yrs vs minimum requirement of 8.0 yrs (68.8%)', 'Qualified: Master meets required Master (100.0%)', 'Strong Java developer, but lacks Kubernetes, Redis, and sufficient tenure for Principal Architect.'),
(10, 10, 'Java, AWS, Kubernetes, Redis', 'None', 'Go, Docker, PostgreSQL', 'None', 'Qualified: 10.5 yrs exceeds minimum requirement of 8.0 yrs (100.0%)', 'Exceeds: Doctorate exceeds required Master (100.0%)', 'Flawless fit for Principal Cloud Architect with deep expertise in distributed caching and cloud systems.');

-- 20. ACTIVITY LOGS
INSERT INTO activity_logs (log_id, user_id, action_type, description, ip_address, created_at) VALUES
(1, 1, 'SYSTEM_STARTUP', 'System initialized with version 1.0.0 and loaded database configurations', '127.0.0.1', '2026-10-20 08:00:00'),
(2, 1, 'USER_CREATED', 'Created recruiter account for Sarah Jenkins (TechCorp Solutions)', '127.0.0.1', '2026-10-20 09:30:00'),
(3, 1, 'USER_CREATED', 'Created recruiter account for David Miller (CloudPeak Innovations)', '127.0.0.1', '2026-10-20 09:35:00'),
(4, 2, 'JOB_CREATED', 'Posted new vacancy: Senior Java Backend Developer (Job #1)', '192.168.1.105', '2026-10-22 10:15:00'),
(5, 2, 'JOB_CREATED', 'Posted new vacancy: Full Stack Java & React Engineer (Job #2)', '192.168.1.105', '2026-10-22 11:20:00'),
(6, 3, 'JOB_CREATED', 'Posted new vacancy: Senior DevOps & Cloud Engineer (Job #3)', '192.168.1.112', '2026-10-23 08:45:00'),
(7, 4, 'RESUME_UPLOADED', 'Candidate Johnathan Doe uploaded resume: John_Doe_Resume_2026.pdf', '192.168.1.150', '2026-10-25 10:20:00'),
(8, 4, 'APPLICATION_SUBMITTED', 'Johnathan Doe submitted application for Job #1 (Senior Java Backend Developer)', '192.168.1.150', '2026-10-25 10:30:00'),
(9, 2, 'CANDIDATE_SHORTLISTED', 'Recruiter Sarah Jenkins shortlisted Johnathan Doe for Job #1 (Match Score: 100.0%)', '192.168.1.105', '2026-10-25 14:00:00'),
(10, 3, 'CANDIDATE_HIRED', 'Recruiter David Miller updated status to HIRED for Alex Kumar on Job #3 (Senior DevOps)', '192.168.1.112', '2026-10-30 16:30:00');

SET FOREIGN_KEY_CHECKS = 1;

