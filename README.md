# TalentIQ – Resume & Skill Matching Management System

**TalentIQ** is an enterprise-grade desktop software application built with **Java SE 21**, **Java Swing**, and **MySQL 8.x**. It is engineered for resume repository management, rule-based skill gap evaluation, deterministic candidate ranking, and recruitment analytics.

> **CRITICAL ARCHITECTURAL GUARANTEE**: This system is 100% traditional deterministic software. It does **NOT** use Artificial Intelligence, Machine Learning, NLP, LLMs, or external AI APIs. All intelligence, scoring, and resume parsing are powered by transparent Java OOP algorithms, regular expressions, and weighted scoring mathematics.

---

## 1. Technology Stack

- **Platform**: Java SE 21+
- **GUI Framework**: Java Swing with FlatLaf 3.5.4 (Modern macOS Dark & Light Themes)
- **Database Engine**: MySQL 8.x
- **Persistence / Driver**: JDBC (`com.mysql:mysql-connector-j:8.3.0`)
- **Build System**: Apache Maven 3.9+
- **Data Visualization**: JFreeChart 1.5.5
- **Security**: jBCrypt 0.4 (Blowfish crypt password hashing)
- **Document Text Extraction**:
  - Apache PDFBox 3.0.3 (PDF parsing & report generation)
  - Apache POI 5.2.5 (DOCX text extraction)
- **Testing**: JUnit 5.10.2

---

## 2. Architecture & Design Patterns

The system strictly follows the standard enterprise layered architecture:
```
com.talentiq
│
├── config      # Database connection management, fallback logic, system runtime properties
├── model       # 19 Normalized domain entities (User, CandidateProfile, Job, MatchResult, etc.)
├── dao         # PreparedStatements, SQL transactions, connection management (BaseDAO pattern)
├── service     # Core business rules, deterministic matching engine, ranking, auth, analytics
├── util        # PDFBox/POI text extractors, regex ResumeParser, PasswordUtil, ExportUtil, Session
├── ui          # FlatLaf Modern GUI
│   ├── theme        # ThemeManager (Dark/Light macOS themes, color palettes, fonts)
│   ├── components   # StatCard, ModernTable, ProgressBarCellRenderer, BadgeCellRenderer, TopNavBar, SidebarPanel
│   ├── auth         # LoginDialog, RegisterDialog
│   ├── admin        # AdminDashboardPanel, UserManagementPanel, SkillManagementPanel, SystemSettingsPanel, ActivityLogPanel
│   ├── recruiter    # RecruiterDashboardPanel, JobManagementPanel, CandidateRankingPanel, CandidateComparisonPanel, SkillGapAnalysisPanel, JobApplicationManagementPanel
│   ├── candidate    # CandidateDashboardPanel, CandidateProfilePanel, ResumeUploadPanel, CandidateJobsPanel, MyApplicationsPanel
│   ├── dialogs      # MatchDetailDialog, ResumeCorrectionDialog, JobEditDialog, CandidateDetailDialog, CandidateEditDialog
│   └── analytics    # AnalyticsReportsPanel (6 JFreeChart charts + CSV/PDF exports)
└── Main.java   # Application entry point with database diagnostics
```

---

## 3. Database Setup (MySQL 8.x)

The database schema `talentiq_db` consists of **20 normalized tables** with primary keys, foreign key constraints, cascading deletions, and performance indexes.

### Database Credentials Configuration
Database settings can be configured in:
- `config/database.properties` (or `src/main/resources/database.properties`):
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/talentiq_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.username=root
db.password=Dhayanithi@1401
```
*You can also set environment variables `TALENTIQ_DB_URL`, `TALENTIQ_DB_USER`, and `TALENTIQ_DB_PASSWORD`.*

### Database Initialization
Open a terminal in the project directory and run:

```bash
# 1. Create the database schema
mysql -u root -p talentiq_db < database/schema.sql

# 2. Populate the realistic sample seed data
mysql -u root -p talentiq_db < database/seed_data.sql
```

The 20 normalized tables created:
1. `users`
2. `roles`
3. `candidate_profiles`
4. `recruiters`
5. `resumes`
6. `education`
7. `work_experience`
8. `skills`
9. `candidate_skills`
10. `certifications`
11. `projects`
12. `jobs`
13. `job_required_skills`
14. `job_preferred_skills`
15. `job_applications`
16. `match_results`
17. `match_details`
18. `skill_categories`
19. `activity_logs`
20. `system_settings`

---

## 4. Default Seed User Accounts

All passwords in the seed database are hashed with **BCrypt**:

| Role | Username | Password | Full Name / Organization | Description |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `Admin@123` | System Administrator | Full access to users, settings, dictionary, audit logs |
| **RECRUITER** | `sarah.recruiter` | `Recruiter@123` | Sarah Jenkins (TechCorp Solutions) | Post jobs, evaluate, compare & rank candidates |
| **RECRUITER** | `david.recruiter` | `Recruiter@123` | David Miller (CloudPeak Innovations) | DevOps and Cloud tech recruitment |
| **CANDIDATE** | `john.doe` | `Candidate@123` | Johnathan Doe | Senior Java Backend Engineer (5.5 yrs exp) |
| **CANDIDATE** | `priya.sharma` | `Candidate@123` | Priya Sharma | Full Stack Java & React Engineer (3.5 yrs exp) |
| **CANDIDATE** | `alex.kumar` | `Candidate@123` | Alex Kumar | Lead DevOps & Cloud Architect (7.0 yrs exp) |
| **CANDIDATE** | `elena.rostova` | `Candidate@123` | Elena Rostova | Junior Java / Python Developer (1.0 yrs exp) |
| **CANDIDATE** | `marcus.chen` | `Candidate@123` | Marcus Chen, Ph.D. | Principal Distributed Systems Architect (10.5 yrs exp) |

*The login dialog also features one-click "Demo Logins" buttons for instant testing.*

---

## 5. Application Modules Overview

### Module 1: Authentication & Session Management
- Role-based access control (Admin, Recruiter, Candidate).
- Secure password hashing with BCrypt (log rounds = 12).
- Candidate self-registration with live field validation.
- Centralized thread-safe `SessionManager` notifying UI components on login/logout.

### Module 2: Admin Dashboard
- Live KPI cards: Total Candidates, Total Recruiters, Total Resumes, Active Jobs, Total Applications, Shortlisted Applicants.
- Interactive JFreeChart graphics: Experience Level Distribution & Applications per Job.
- System Activity Log table displaying real-time database audit entries.

### Module 3: Candidate Management
- Candidate Dossier viewing and full profile management.
- Multi-tab management for Candidate sub-entities:
  - Technical Skills (with proficiency: Beginner, Intermediate, Advanced, Expert)
  - Work Experience (dates, company, title, responsibilities, tech used)
  - Educational Background (degree, level, institution, year, GPA)
  - Industry Certifications (credential ID, issuing body, validity)
  - Portfolio Projects (role, tech stack, descriptions, URLs)

### Module 4: Resume Management & Conventional Parsing
- File uploader supporting **PDF**, **DOCX**, and **TXT** files.
- Text extraction using Apache PDFBox 3.x and Apache POI 5.2.5.
- Rule-based regex parser extracting:
  - Email addresses: `(?i)\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b`
  - Phone numbers: National & international phone formats
  - Experience tenure: `(?i)(\d+(?:\.\d+)?)\+?\s*(?:years?|yrs?)(?:…)` and date ranges
  - Educational degrees: Doctorate, Master, Bachelor, Associate, High School
  - Dictionary skills: Exact word-boundary matching against centralized skill library
- Files saved in `uploads/resumes/` with metadata stored in MySQL.
- **Manual Correction Dialog**: Lets user review, edit, and apply extracted details directly to their database profile.
- Sample resume files included in `sample_resumes/` for testing!

### Module 5: Centralized Skill Dictionary
- 8 Industry-standard categories:
  1. Programming Languages
  2. Frontend Development
  3. Backend Development
  4. Databases
  5. Cloud Computing
  6. Software Tools
  7. Testing
  8. DevOps
- 54+ pre-seeded skills with common industry aliases.
- Admin CRUD interface to add, edit, or delete skills and categories.

### Module 6: Job Management
- Recruiters create and edit vacancies with:
  - Job title, department, description, employment type, location, salary range, deadline.
  - Required Skills list (60% weight) with minimum proficiency level.
  - Preferred Skills list (20% weight) with minimum proficiency level.
  - Minimum Experience years and Minimum Education requirement.
  - Status toggle (`ACTIVE`, `CLOSED`, `DRAFT`).

### Module 7 & 8: Resume & Skill Matching & Candidate Ranking
The central deterministic rule-based matching algorithm:

$$\text{Final Score} = (\text{Required Skill Score} \times 0.60) + (\text{Preferred Skill Score} \times 0.20) + (\text{Experience Score} \times 0.10) + (\text{Education Score} \times 0.10)$$

Where:
- $\text{Required Skill Score} = \frac{\text{Matched Required Skills}}{\text{Total Required Skills}} \times 100$
- $\text{Preferred Skill Score} = \frac{\text{Matched Preferred Skills}}{\text{Total Preferred Skills}} \times 100$
- $\text{Experience Score} = \min\left(100, \frac{\text{Candidate Years}}{\text{Job Required Years}} \times 100\right)$ (100% if candidate $\ge$ job requirement)
- $\text{Education Score}$: Hierarchical level match (Doctorate: 5, Master: 4, Bachelor: 3, Associate: 2, High School: 1). 100% if meets/exceeds requirement, 75% if 1 tier below, 50% if 2 tiers below.

**Ranking Features:**
- Sorted table ranking candidates from highest to lowest matching percentage.
- Graphical progress bar renderer for matching score.
- Dynamic filters: skill keyword, minimum experience, education, score threshold ($\ge 80\%$, $\ge 60\%$, $\ge 40\%$).
- Actions: View Detailed Match Breakdown, Shortlist Candidate, Schedule Interview, Mark Hired, Reject.
- Export ranked candidates to CSV.
- Export candidate evaluation match report to **PDF** using Apache PDFBox!

### Module 9: Candidate Comparison
- Compare up to 4 candidates side-by-side for any job opening.
- Comparison matrix showing Match %, Matched Skills, Missing Skills, Experience, Education, Certifications, and Projects.
- Side-by-side JFreeChart bar chart comparing overall and component scores.

### Module 10: Skill Gap Analysis
- Select any candidate and target job.
- Displays existing technical competencies.
- Flags **Critical Missing Required Skills** in Red and **Secondary Missing Preferred Skills** in Amber.
- Calculates exact skill completion percentage.
- Generates transparent recruiter recommendations and candidate upskilling action plan.
- JFreeChart Donut / Pie chart visualizing acquired vs missing skills.

### Module 11: Candidate Dashboard & Self-Service Portal
- Profile completeness and credentials overview.
- Upload resume and review extracted information.
- Browse active vacancies with **real-time calculated match percentages**.
- One-click job application submission (checks for and prevents duplicates).
- Track application status (`APPLIED`, `SHORTLISTED`, `INTERVIEW_SCHEDULED`, `HIRED`, `REJECTED`) and recruiter feedback.

### Module 12: Analytics & Reports
- 6 JFreeChart visual graphs:
  1. Candidate Experience Distribution (Bar chart)
  2. Applications per Job Opening (Bar chart)
  3. Top Demanded Skills in Jobs (Horizontal bar chart)
  4. Most Common Candidate Skills (Horizontal bar chart)
  5. Matching Score Distribution (Pie chart)
  6. Application Recruitment Statuses (Pie chart)
- Export Candidate Directory to CSV.
- Export Job Vacancies to CSV.
- Export Application Summary to CSV.
- Export Candidate Match Dossier to PDF.

---

## 6. Building and Running the Application

### Prerequisites
- JDK 21 or higher installed
- Apache Maven 3.9+ installed
- MySQL 8.x running with `talentiq_db` created and seeded

### Run via Maven
```bash
# Compile and run the desktop application
mvn compile exec:java
```

### Run Unit Tests
```bash
mvn test
```
*All 11 unit tests verify database connectivity, matching formula correctness, regex parsing, and PDF/DOCX text extractors.*

### Build Executable Fat JAR
```bash
mvn clean package -DskipTests=false
```
The standalone executable shaded JAR is created at:
```
target/talentiq-system-1.0.0.jar
```

Run the packaged application directly:
```bash
java -jar target/talentiq-system-1.0.0.jar
```

---

## 7. Sample Resumes for Testing

Sample files are located in `sample_resumes/`:
1. `sample_resumes/resume_john_doe.pdf` (PDF format)
2. `sample_resumes/resume_sarah_connor.docx` (Microsoft Word DOCX format)
3. `sample_resumes/resume_alex_kumar.txt` (Plain text format)

You can upload these directly inside **Candidate Portal -> Resume Management** to observe the instant text extraction, regex parsing, and the interactive **Manual Correction Dialog**.

