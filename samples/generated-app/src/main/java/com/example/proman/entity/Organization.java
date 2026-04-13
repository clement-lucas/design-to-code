package com.example.proman.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ORGANIZATION")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORGANIZATION_ID")
    private Integer organizationId;

    @Column(name = "ORGANIZATION_NAME", nullable = false, length = 128)
    private String organizationName;

    @Column(name = "UPPER_ORGANIZATION")
    private Integer upperOrganization;

    public Integer getOrganizationId() { return organizationId; }
    public void setOrganizationId(Integer organizationId) { this.organizationId = organizationId; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public Integer getUpperOrganization() { return upperOrganization; }
    public void setUpperOrganization(Integer upperOrganization) { this.upperOrganization = upperOrganization; }
}
