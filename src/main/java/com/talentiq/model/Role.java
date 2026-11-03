package com.talentiq.model;

import java.io.Serializable;
import java.util.Objects;

public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int ROLE_ADMIN_ID = 1;
    public static final int ROLE_RECRUITER_ID = 2;
    public static final int ROLE_CANDIDATE_ID = 3;

    public static final String ADMIN = "ADMIN";
    public static final String RECRUITER = "RECRUITER";
    public static final String CANDIDATE = "CANDIDATE";

    private int roleId;
    private String roleName;
    private String description;

    public Role() {}

    public Role(int roleId, String roleName, String description) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return roleId == role.roleId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId);
    }

    @Override
    public String toString() {
        return roleName != null ? roleName : "";
    }
}

