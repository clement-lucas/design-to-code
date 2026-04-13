package com.example.proman.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "BUSINESS_DATE")
public class BusinessDate {

    @Id
    @Column(name = "SEGMENT_ID", length = 2)
    private String segmentId;

    @Column(name = "BIZ_DATE", nullable = false, length = 8)
    private String bizDate;

    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }
    public String getBizDate() { return bizDate; }
    public void setBizDate(String bizDate) { this.bizDate = bizDate; }
}
