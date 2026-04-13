package com.example.proman.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SYSTEM_ACCOUNT")
public class SystemAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "LOGIN_ID", nullable = false, length = 20)
    private String loginId;

    @Column(name = "USER_PASSWORD", nullable = false, length = 256)
    private String userPassword;

    @Column(name = "PASSWORD_EXPIRATION_DATE", nullable = false)
    private LocalDate passwordExpirationDate;

    @Column(name = "FAILED_COUNT", nullable = false)
    private Short failedCount;

    @Column(name = "EFFECTIVE_DATE_FROM")
    private LocalDate effectiveDateFrom;

    @Column(name = "EFFECTIVE_DATE_TO")
    private LocalDate effectiveDateTo;

    @Column(name = "LAST_LOGIN_DATE_TIME")
    private LocalDateTime lastLoginDateTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", insertable = false, updatable = false)
    private Users user;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }
    public LocalDate getPasswordExpirationDate() { return passwordExpirationDate; }
    public void setPasswordExpirationDate(LocalDate passwordExpirationDate) { this.passwordExpirationDate = passwordExpirationDate; }
    public Short getFailedCount() { return failedCount; }
    public void setFailedCount(Short failedCount) { this.failedCount = failedCount; }
    public LocalDate getEffectiveDateFrom() { return effectiveDateFrom; }
    public void setEffectiveDateFrom(LocalDate effectiveDateFrom) { this.effectiveDateFrom = effectiveDateFrom; }
    public LocalDate getEffectiveDateTo() { return effectiveDateTo; }
    public void setEffectiveDateTo(LocalDate effectiveDateTo) { this.effectiveDateTo = effectiveDateTo; }
    public LocalDateTime getLastLoginDateTime() { return lastLoginDateTime; }
    public void setLastLoginDateTime(LocalDateTime lastLoginDateTime) { this.lastLoginDateTime = lastLoginDateTime; }
    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
}
