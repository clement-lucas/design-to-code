package com.example.proman.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "PROJECT")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROJECT_ID")
    private Integer projectId;

    @Column(name = "PROJECT_NAME", nullable = false, length = 128)
    private String projectName;

    @Column(name = "PROJECT_TYPE", nullable = false, length = 2)
    private String projectType;

    @Column(name = "PROJECT_CLASS", nullable = false, length = 2)
    private String projectClass;

    @Column(name = "PROJECT_START_DATE", nullable = false)
    private LocalDate projectStartDate;

    @Column(name = "PROJECT_END_DATE", nullable = false)
    private LocalDate projectEndDate;

    @Column(name = "ORGANIZATION_ID", nullable = false)
    private Integer organizationId;

    @Column(name = "CLIENT_ID", nullable = false)
    private Integer clientId;

    @Column(name = "PROJECT_MANAGER", nullable = false, length = 128)
    private String projectManager;

    @Column(name = "PROJECT_LEADER", nullable = false, length = 128)
    private String projectLeader;

    @Column(name = "NOTE", length = 512)
    private String note;

    @Column(name = "SALES")
    private Integer sales;

    @Version
    @Column(name = "VERSION_NO", nullable = false)
    private Long versionNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID", insertable = false, updatable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLIENT_ID", insertable = false, updatable = false)
    private Client client;

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
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
    public String getProjectManager() { return projectManager; }
    public void setProjectManager(String projectManager) { this.projectManager = projectManager; }
    public String getProjectLeader() { return projectLeader; }
    public void setProjectLeader(String projectLeader) { this.projectLeader = projectLeader; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
    public Long getVersionNo() { return versionNo; }
    public void setVersionNo(Long versionNo) { this.versionNo = versionNo; }
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}
