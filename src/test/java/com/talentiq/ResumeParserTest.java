package com.talentiq;

import com.talentiq.model.Skill;
import com.talentiq.util.ResumeParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Resume Text and Regex Parsing Tests")
public class ResumeParserTest {

    @Test
    @DisplayName("Extract email, phone, experience, and degree from raw resume text")
    public void testRegexExtraction() {
        String text = "Johnathan Doe - Senior Java Engineer\n" +
                      "Email: john.doe@email.com | Phone: +1-555-0201 | San Francisco, CA\n" +
                      "Summary: Software architect with 6.5 years of experience building scalable systems.\n" +
                      "Education: Master of Science in Computer Science from Stanford University.\n" +
                      "Core Competencies: Java, Spring Boot, MySQL, Docker, Kubernetes, AWS, Git.";

        List<Skill> dictionary = new ArrayList<>();
        dictionary.add(new Skill(1, 1, "Java", "Core Java", ""));
        dictionary.add(new Skill(2, 3, "Spring Boot", "SpringBoot", ""));
        dictionary.add(new Skill(3, 4, "MySQL", "MySQL 8", ""));
        dictionary.add(new Skill(4, 8, "Docker", "Container", ""));
        dictionary.add(new Skill(5, 8, "Kubernetes", "K8s", ""));
        dictionary.add(new Skill(6, 1, "Python", "Python3", "")); // Not in text

        ResumeParser.ParsedResumeResult parsed = ResumeParser.parseText(text, dictionary);

        assertEquals("john.doe@email.com", parsed.getEmail());
        assertTrue(parsed.getPhone().contains("555-0201"));
        assertEquals("Master", parsed.getHighestEducationLevel());
        assertEquals(6.5, parsed.getTotalExperienceYears(), 0.1);

        // Check matched skills
        List<String> matchedNames = parsed.getMatchedSkills().stream().map(Skill::getSkillName).toList();
        assertTrue(matchedNames.contains("Java"));
        assertTrue(matchedNames.contains("Spring Boot"));
        assertTrue(matchedNames.contains("MySQL"));
        assertTrue(matchedNames.contains("Docker"));
        assertTrue(matchedNames.contains("Kubernetes"));
        assertFalse(matchedNames.contains("Python"));
    }
}

