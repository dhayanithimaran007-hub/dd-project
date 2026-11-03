package com.talentiq.service;

import com.talentiq.dao.ActivityLogDAO;
import com.talentiq.dao.SkillDAO;
import com.talentiq.model.Skill;
import com.talentiq.model.SkillCategory;

import java.sql.SQLException;
import java.util.List;

public class SkillService {

    private final SkillDAO skillDAO = new SkillDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public List<SkillCategory> getAllCategories() {
        return skillDAO.findAllCategories();
    }

    public SkillCategory getCategoryById(int id) {
        return skillDAO.findCategoryById(id);
    }

    public int saveCategory(SkillCategory category) throws SQLException {
        int id = skillDAO.saveCategory(category);
        activityLogDAO.log(null, "SKILL_CATEGORY_SAVED", "Saved skill category: " + category.getCategoryName(), "127.0.0.1");
        return id;
    }

    public boolean deleteCategory(int id) {
        SkillCategory cat = skillDAO.findCategoryById(id);
        boolean success = skillDAO.deleteCategory(id);
        if (success && cat != null) {
            activityLogDAO.log(null, "SKILL_CATEGORY_DELETED", "Deleted skill category: " + cat.getCategoryName(), "127.0.0.1");
        }
        return success;
    }

    public List<Skill> getAllSkills() {
        return skillDAO.findAllSkills();
    }

    public List<Skill> getSkillsByCategory(int categoryId) {
        return skillDAO.findSkillsByCategory(categoryId);
    }

    public Skill getSkillById(int id) {
        return skillDAO.findSkillById(id);
    }

    public Skill getSkillByName(String name) {
        return skillDAO.findSkillByName(name);
    }

    public int addSkill(Skill skill) throws SQLException {
        int id = skillDAO.saveSkill(skill);
        activityLogDAO.log(null, "SKILL_ADDED", "Added new skill to dictionary: " + skill.getSkillName(), "127.0.0.1");
        return id;
    }

    public boolean updateSkill(Skill skill) {
        boolean success = skillDAO.updateSkill(skill);
        if (success) {
            activityLogDAO.log(null, "SKILL_UPDATED", "Updated skill: " + skill.getSkillName(), "127.0.0.1");
        }
        return success;
    }

    public boolean deleteSkill(int id) {
        Skill s = skillDAO.findSkillById(id);
        boolean success = skillDAO.deleteSkill(id);
        if (success && s != null) {
            activityLogDAO.log(null, "SKILL_DELETED", "Deleted skill from dictionary: " + s.getSkillName(), "127.0.0.1");
        }
        return success;
    }
}

