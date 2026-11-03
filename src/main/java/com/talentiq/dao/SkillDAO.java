package com.talentiq.dao;

import com.talentiq.model.Skill;
import com.talentiq.model.SkillCategory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SkillDAO extends BaseDAO {

    // --- Category Operations ---
    public List<SkillCategory> findAllCategories() {
        List<SkillCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM skill_categories ORDER BY display_order, category_name";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SkillCategory cat = new SkillCategory();
                cat.setCategoryId(rs.getInt("category_id"));
                cat.setCategoryName(rs.getString("category_name"));
                cat.setDescription(rs.getString("description"));
                cat.setDisplayOrder(rs.getInt("display_order"));
                cat.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(cat);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching skill categories", e);
        }
        return list;
    }

    public SkillCategory findCategoryById(int categoryId) {
        String sql = "SELECT * FROM skill_categories WHERE category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SkillCategory cat = new SkillCategory();
                    cat.setCategoryId(rs.getInt("category_id"));
                    cat.setCategoryName(rs.getString("category_name"));
                    cat.setDescription(rs.getString("description"));
                    cat.setDisplayOrder(rs.getInt("display_order"));
                    cat.setCreatedAt(rs.getTimestamp("created_at"));
                    return cat;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding category by id: " + categoryId, e);
        }
        return null;
    }

    public int saveCategory(SkillCategory cat) throws SQLException {
        if (cat.getCategoryId() > 0) {
            String sql = "UPDATE skill_categories SET category_name = ?, description = ?, display_order = ? WHERE category_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cat.getCategoryName());
                ps.setString(2, cat.getDescription());
                ps.setInt(3, cat.getDisplayOrder());
                ps.setInt(4, cat.getCategoryId());
                ps.executeUpdate();
                return cat.getCategoryId();
            }
        } else {
            String sql = "INSERT INTO skill_categories (category_name, description, display_order) VALUES (?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, cat.getCategoryName());
                ps.setString(2, cat.getDescription());
                ps.setInt(3, cat.getDisplayOrder());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        cat.setCategoryId(id);
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    public boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM skill_categories WHERE category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting skill category: " + categoryId, e);
            return false;
        }
    }

    // --- Skill Operations ---
    public List<Skill> findAllSkills() {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT s.*, sc.category_name " +
                     "FROM skills s " +
                     "JOIN skill_categories sc ON s.category_id = sc.category_id " +
                     "ORDER BY sc.display_order, s.skill_name";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapSkill(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching all skills", e);
        }
        return list;
    }

    public List<Skill> findSkillsByCategory(int categoryId) {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT s.*, sc.category_name " +
                     "FROM skills s " +
                     "JOIN skill_categories sc ON s.category_id = sc.category_id " +
                     "WHERE s.category_id = ? ORDER BY s.skill_name";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSkill(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching skills by category: " + categoryId, e);
        }
        return list;
    }

    public Skill findSkillById(int skillId) {
        String sql = "SELECT s.*, sc.category_name " +
                     "FROM skills s " +
                     "JOIN skill_categories sc ON s.category_id = sc.category_id " +
                     "WHERE s.skill_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, skillId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSkill(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding skill by id: " + skillId, e);
        }
        return null;
    }

    public Skill findSkillByName(String skillName) {
        String sql = "SELECT s.*, sc.category_name " +
                     "FROM skills s " +
                     "JOIN skill_categories sc ON s.category_id = sc.category_id " +
                     "WHERE LOWER(s.skill_name) = LOWER(?) LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, skillName != null ? skillName.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSkill(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding skill by name: " + skillName, e);
        }
        return null;
    }

    public int saveSkill(Skill s) throws SQLException {
        String sql = "INSERT INTO skills (category_id, skill_name, aliases, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getCategoryId());
            ps.setString(2, s.getSkillName().trim());
            ps.setString(3, s.getAliases());
            ps.setString(4, s.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    s.setSkillId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean updateSkill(Skill s) {
        String sql = "UPDATE skills SET category_id = ?, skill_name = ?, aliases = ?, description = ? WHERE skill_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getCategoryId());
            ps.setString(2, s.getSkillName().trim());
            ps.setString(3, s.getAliases());
            ps.setString(4, s.getDescription());
            ps.setInt(5, s.getSkillId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating skill: " + s.getSkillId(), e);
            return false;
        }
    }

    public boolean deleteSkill(int skillId) {
        String sql = "DELETE FROM skills WHERE skill_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, skillId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting skill: " + skillId, e);
            return false;
        }
    }

    private Skill mapSkill(ResultSet rs) throws SQLException {
        Skill s = new Skill();
        s.setSkillId(rs.getInt("skill_id"));
        s.setCategoryId(rs.getInt("category_id"));
        s.setCategoryName(rs.getString("category_name"));
        s.setSkillName(rs.getString("skill_name"));
        s.setAliases(rs.getString("aliases"));
        s.setDescription(rs.getString("description"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}

