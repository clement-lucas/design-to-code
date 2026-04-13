package com.example.proman.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "USERS")
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "KANJI_NAME", nullable = false, length = 128)
    private String kanjiName;

    @Column(name = "KANA_NAME", nullable = false, length = 128)
    private String kanaName;

    @Column(name = "PM_FLAG", nullable = false)
    private Boolean pmFlag;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getKanjiName() { return kanjiName; }
    public void setKanjiName(String kanjiName) { this.kanjiName = kanjiName; }
    public String getKanaName() { return kanaName; }
    public void setKanaName(String kanaName) { this.kanaName = kanaName; }
    public Boolean getPmFlag() { return pmFlag; }
    public void setPmFlag(Boolean pmFlag) { this.pmFlag = pmFlag; }
}
