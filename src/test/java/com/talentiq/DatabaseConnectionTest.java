package com.talentiq;

import com.talentiq.config.DatabaseConfig;
import com.talentiq.dao.SkillDAO;
import com.talentiq.dao.UserDAO;
import com.talentiq.model.Skill;
import com.talentiq.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MySQL Database Connection & Seed Data Tests")
public class DatabaseConnectionTest {

    @BeforeAll
    public static void init() {
        DatabaseConfig.init();
    }

    @Test
    @DisplayName("Verify MySQL connection test succeeds")
    public void testConnection() {
        assertTrue(DatabaseConfig.testConnection(), "Database connection must be valid and active");
    }

    @Test
    @DisplayName("Verify seed users and admin exist in MySQL")
    public void testSeedUsers() {
        UserDAO userDAO = new UserDAO();
        User admin = userDAO.findByUsername("admin");
        assertNotNull(admin, "Seed admin user must exist in database");
        assertEquals("admin@talentiq.com", admin.getEmail());
        assertTrue(admin.isAdmin());
    }

    @Test
    @DisplayName("Verify centralized skill dictionary is seeded with 50+ skills")
    public void testSeedSkills() {
        SkillDAO skillDAO = new SkillDAO();
        List<Skill> skills = skillDAO.findAllSkills();
        assertNotNull(skills);
        assertTrue(skills.size() >= 50, "Skill dictionary should contain at least 50 skills across 8 categories");
    }
}

