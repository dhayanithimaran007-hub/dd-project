package com.talentiq;

import com.talentiq.model.CandidateProfile;
import com.talentiq.model.CandidateSkill;
import com.talentiq.model.Job;
import com.talentiq.model.JobSkill;
import com.talentiq.model.MatchResult;
import com.talentiq.service.MatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Rule-Based Deterministic Matching Algorithm Tests")
public class MatchingAlgorithmTest {

    private MatchingService matchingService;

    @BeforeEach
    public void setUp() {
        matchingService = new MatchingService();
    }

    @Test
    @DisplayName("Exact prompt example match calculation")
    public void testPromptExampleMatch() {
        // Job: Java Developer
        // Required Skills: Java, Spring Boot, MySQL, JDBC
        // Preferred Skills: Docker, Git
        // Min Exp: 3.0 yrs, Min Edu: Bachelor
        Job job = new Job();
        job.setJobId(101);
        job.setTitle("Java Developer");
        job.setMinExperienceYears(3.0);
        job.setMinEducationLevel("Bachelor");

        List<JobSkill> req = new ArrayList<>();
        req.add(new JobSkill(101, 1, "Java", "Intermediate", true));
        req.add(new JobSkill(101, 19, "Spring Boot", "Intermediate", true));
        req.add(new JobSkill(101, 26, "MySQL", "Intermediate", true));
        req.add(new JobSkill(101, 7, "JDBC", "Intermediate", true));
        job.setRequiredSkills(req);

        List<JobSkill> pref = new ArrayList<>();
        pref.add(new JobSkill(101, 48, "Docker", "Beginner", false));
        pref.add(new JobSkill(101, 36, "Git", "Beginner", false));
        job.setPreferredSkills(pref);

        // Candidate Skills: Java, Spring Boot, MySQL, Git
        // Exp: 4.0 yrs, Edu: Bachelor
        CandidateProfile candidate = new CandidateProfile();
        candidate.setCandidateId(201);
        candidate.setFullName("Test Java Dev");
        candidate.setTotalExperienceYears(4.0);
        candidate.setHighestEducationLevel("Bachelor");

        List<CandidateSkill> candSkills = new ArrayList<>();
        candSkills.add(new CandidateSkill(201, 1, "Java", "Advanced", 4.0));
        candSkills.add(new CandidateSkill(201, 19, "Spring Boot", "Advanced", 3.5));
        candSkills.add(new CandidateSkill(201, 26, "MySQL", "Advanced", 3.0));
        candSkills.add(new CandidateSkill(201, 36, "Git", "Advanced", 4.0));
        candidate.setSkills(candSkills);

        MatchResult result = matchingService.calculateMatch(candidate, job);

        // Required skills: 3 / 4 matched = 75.0%
        assertEquals(75.0, result.getRequiredSkillScore(), 0.01);

        // Preferred skills: 1 / 2 matched (Git) = 50.0%
        assertEquals(50.0, result.getPreferredSkillScore(), 0.01);

        // Experience: 4.0 >= 3.0 = 100.0%
        assertEquals(100.0, result.getExperienceScore(), 0.01);

        // Education: Bachelor >= Bachelor = 100.0%
        assertEquals(100.0, result.getEducationScore(), 0.01);

        // Formula: (75 * 0.60) + (50 * 0.20) + (100 * 0.10) + (100 * 0.10)
        // = 45.0 + 10.0 + 10.0 + 10.0 = 75.0%
        assertEquals(75.0, result.getOverallScore(), 0.01);

        // Test determinism: Running 5 times produces exact same score
        for (int i = 0; i < 5; i++) {
            MatchResult rerun = matchingService.calculateMatch(candidate, job);
            assertEquals(result.getOverallScore(), rerun.getOverallScore());
            assertEquals(result.getRequiredSkillScore(), rerun.getRequiredSkillScore());
            assertEquals(result.getPreferredSkillScore(), rerun.getPreferredSkillScore());
        }
    }

    @Test
    @DisplayName("Handles empty skills and missing candidate data safely without throwing exceptions")
    public void testEmptySkillsHandling() {
        Job job = new Job();
        job.setJobId(102);
        job.setTitle("Generalist");
        job.setRequiredSkills(new ArrayList<>());
        job.setPreferredSkills(new ArrayList<>());
        job.setMinExperienceYears(0.0);
        job.setMinEducationLevel("Bachelor");

        CandidateProfile candidate = new CandidateProfile();
        candidate.setCandidateId(202);
        candidate.setFullName("Entry Candidate");
        candidate.setSkills(new ArrayList<>());
        candidate.setTotalExperienceYears(0.0);
        candidate.setHighestEducationLevel("Bachelor");

        assertDoesNotThrow(() -> {
            MatchResult mr = matchingService.calculateMatch(candidate, job);
            assertNotNull(mr);
            assertEquals(100.0, mr.getOverallScore(), 0.01);
        });
    }

    @Test
    @DisplayName("Partial experience scaling calculation")
    public void testPartialExperience() {
        Job job = new Job();
        job.setMinExperienceYears(5.0);
        job.setMinEducationLevel("Bachelor");

        CandidateProfile candidate = new CandidateProfile();
        candidate.setTotalExperienceYears(2.5);
        candidate.setHighestEducationLevel("Bachelor");

        MatchResult mr = matchingService.calculateMatch(candidate, job);
        // Exp score should be (2.5 / 5.0) * 100 = 50.0%
        assertEquals(50.0, mr.getExperienceScore(), 0.01);
    }
}

