package com.example.proman.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECTS_BY_USER")
public class ProjectsByUser {

    @Id
    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "REQUEST_ID")
    private Long requestId;

    @Column(name = "FILE_NAME", length = 50)
    private String fileName;

    @Column(name = "CREATE_DATETIME")
    private LocalDateTime createDatetime;

    @Column(name = "EXPIRE_DATETIME")
    private LocalDateTime expireDatetime;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public LocalDateTime getCreateDatetime() { return createDatetime; }
    public void setCreateDatetime(LocalDateTime createDatetime) { this.createDatetime = createDatetime; }
    public LocalDateTime getExpireDatetime() { return expireDatetime; }
    public void setExpireDatetime(LocalDateTime expireDatetime) { this.expireDatetime = expireDatetime; }
}
