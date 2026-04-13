package com.example.proman.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "PROJECT_USER")
@IdClass(ProjectUser.ProjectUserId.class)
public class ProjectUser {

    @Id
    @Column(name = "PROJECT_ID")
    private Integer projectId;

    @Id
    @Column(name = "USER_ID")
    private Integer userId;

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public static class ProjectUserId implements Serializable {
        private Integer projectId;
        private Integer userId;

        public ProjectUserId() {}

        public ProjectUserId(Integer projectId, Integer userId) {
            this.projectId = projectId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProjectUserId that = (ProjectUserId) o;
            return Objects.equals(projectId, that.projectId) && Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectId, userId);
        }
    }
}
