package com.example.proman.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "CODE_NAME")
@IdClass(CodeName.CodeNameId.class)
public class CodeName {

    @Id
    @Column(name = "CODE_ID", length = 8)
    private String codeId;

    @Id
    @Column(name = "CODE_VALUE", length = 2)
    private String codeValue;

    @Id
    @Column(name = "LANG", length = 2)
    private String lang;

    @Column(name = "SORT_ORDER", length = 1)
    private String sortOrder;

    @Column(name = "CODE_NAME", length = 50)
    private String codeName;

    @Column(name = "SHORT_NAME", length = 50)
    private String shortName;

    @Column(name = "OPTION01", length = 40)
    private String option01;
    @Column(name = "OPTION02", length = 40)
    private String option02;
    @Column(name = "OPTION03", length = 40)
    private String option03;
    @Column(name = "OPTION04", length = 40)
    private String option04;
    @Column(name = "OPTION05", length = 40)
    private String option05;
    @Column(name = "OPTION06", length = 40)
    private String option06;
    @Column(name = "OPTION07", length = 40)
    private String option07;
    @Column(name = "OPTION08", length = 40)
    private String option08;
    @Column(name = "OPTION09", length = 40)
    private String option09;
    @Column(name = "OPTION10", length = 40)
    private String option10;

    public String getCodeId() { return codeId; }
    public void setCodeId(String codeId) { this.codeId = codeId; }
    public String getCodeValue() { return codeValue; }
    public void setCodeValue(String codeValue) { this.codeValue = codeValue; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public String getOption01() { return option01; }
    public void setOption01(String option01) { this.option01 = option01; }
    public String getOption02() { return option02; }
    public void setOption02(String option02) { this.option02 = option02; }
    public String getOption03() { return option03; }
    public void setOption03(String option03) { this.option03 = option03; }
    public String getOption04() { return option04; }
    public void setOption04(String option04) { this.option04 = option04; }
    public String getOption05() { return option05; }
    public void setOption05(String option05) { this.option05 = option05; }
    public String getOption06() { return option06; }
    public void setOption06(String option06) { this.option06 = option06; }
    public String getOption07() { return option07; }
    public void setOption07(String option07) { this.option07 = option07; }
    public String getOption08() { return option08; }
    public void setOption08(String option08) { this.option08 = option08; }
    public String getOption09() { return option09; }
    public void setOption09(String option09) { this.option09 = option09; }
    public String getOption10() { return option10; }
    public void setOption10(String option10) { this.option10 = option10; }

    public static class CodeNameId implements Serializable {
        private String codeId;
        private String codeValue;
        private String lang;

        public CodeNameId() {}
        public CodeNameId(String codeId, String codeValue, String lang) {
            this.codeId = codeId;
            this.codeValue = codeValue;
            this.lang = lang;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CodeNameId that = (CodeNameId) o;
            return Objects.equals(codeId, that.codeId) && Objects.equals(codeValue, that.codeValue) && Objects.equals(lang, that.lang);
        }

        @Override
        public int hashCode() { return Objects.hash(codeId, codeValue, lang); }
    }
}
