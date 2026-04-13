package com.example.proman.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECTS_BY_USER_REQUEST")
public class ProjectsByUserRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "STATUS", nullable = false, length = 2)
    private String status;

    @Column(name = "REQUEST_DATETIME", nullable = false)
    private LocalDateTime requestDatetime;

    @Column(name = "USER_ID", nullable = false)
    private Integer userId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRequestDatetime() { return requestDatetime; }
    public void setRequestDatetime(LocalDateTime requestDatetime) { this.requestDatetime = requestDatetime; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
