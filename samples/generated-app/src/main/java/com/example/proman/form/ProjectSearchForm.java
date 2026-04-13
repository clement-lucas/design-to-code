package com.example.proman.form;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class ProjectSearchForm {

    private String projectName;
    private String projectType;
    private String projectClass;
    private Integer organizationId;
    private Integer clientId;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate projectStartDateFrom;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate projectStartDateTo;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate projectEndDateFrom;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate projectEndDateTo;

    private Integer sortId;
    private Integer pageNumber;

    // Getters and Setters
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public String getProjectClass() { return projectClass; }
    public void setProjectClass(String projectClass) { this.projectClass = projectClass; }
    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }
    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }
    public LocalDate getProjectStartDateFrom() { return projectStartDateFrom; }
    public void setProjectStartDateFrom(LocalDate projectStartDateFrom) { this.projectStartDateFrom = projectStartDateFrom; }
    public LocalDate getProjectStartDateTo() { return projectStartDateTo; }
    public void setProjectStartDateTo(LocalDate projectStartDateTo) { this.projectStartDateTo = projectStartDateTo; }
    public LocalDate getProjectEndDateFrom() { return projectEndDateFrom; }
    public void setProjectEndDateFrom(LocalDate projectEndDateFrom) { this.projectEndDateFrom = projectEndDateFrom; }
    public LocalDate getProjectEndDateTo() { return projectEndDateTo; }
    public void setProjectEndDateTo(LocalDate projectEndDateTo) { this.projectEndDateTo = projectEndDateTo; }
    public Integer getSortId() { return sortId; }
    public void setSortId(Integer sortId) { this.sortId = sortId; }
    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
}
