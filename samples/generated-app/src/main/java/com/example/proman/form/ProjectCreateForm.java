package com.example.proman.form;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ProjectCreateForm {

    @NotBlank
    @Size(max = 128)
    private String projectName;

    @NotBlank
    @Size(max = 2)
    private String projectType;

    @NotBlank
    @Size(max = 2)
    private String projectClass;

    @NotNull
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate projectStartDate;

    @NotNull
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate projectEndDate;

    @NotNull
    private Integer organizationId;

    @NotNull
    private Integer clientId;

    private String clientName;

    @NotBlank
    @Size(max = 128)
    private String projectManager;

    @NotBlank
    @Size(max = 128)
    private String projectLeader;

    @Size(max = 512)
    private String note;

    @Min(0)
    @Max(999999999)
    private Integer sales;

    // Getters and Setters
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public String getProjectClass() { return projectClass; }
    public void setProjectClass(String projectClass) { this.projectClass = projectClass; }
    public LocalDate getProjectStartDate() { return projectStartDate; }
    public void setProjectStartDate(LocalDate projectStartDate) { this.projectStartDate = projectStartDate; }
    public LocalDate getProjectEndDate() { return projectEndDate; }
    public void setProjectEndDate(LocalDate projectEndDate) { this.projectEndDate = projectEndDate; }
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }
    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getProjectManager() { return projectManager; }
    public void setProjectManager(String projectManager) { this.projectManager = projectManager; }
    public String getProjectLeader() { return projectLeader; }
    public void setProjectLeader(String projectLeader) { this.projectLeader = projectLeader; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
}
