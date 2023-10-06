package com.ie.naukri.search.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ie.naukri.search.commons.core.models.IndexingDocument;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ElasticSearchDocument extends IndexingDocument {

    @JsonProperty(value = "NAME")
    private String name;

    @JsonProperty(value = "TOTAL_EXP")
    private String totalExp;

    @JsonProperty(value = "ABSOLUTE_CTC")
    private Integer absoluteCtc;

    @JsonProperty(value = "ACTIVE")
    private String active;

    @JsonProperty(value = "MOD_DT")
    private String modDt;

    @JsonProperty(value = "PROFILE_TITLE")
    private String profileTitle;

    @JsonProperty(value = "PROFILE_KEYWORDS_ID")
    private List<String> profileKeywordsId;

    @JsonProperty(value = "PROFILE_KEYWORDS")
    private String profileKeywords;

    @JsonProperty(value = "EXPID")
    private Integer expId;

    @JsonProperty(value = "ORGN")
    private String orgn;

    @JsonProperty(value = "EXP_DESIG")
    private String expDesig;

    @JsonProperty(value = "EXP_PROFILE")
    private String expProfile;

    @JsonProperty(value = "EXP_KEYWORDS")
    private String expKeywords;

    @JsonProperty(value = "ORGNID")
    private String orgnId;

    @JsonProperty(value = "EXP_KEYWORDS_ID")
    private List<String> expKeywordsId;

    @JsonProperty(value = "DESIG_ID")
    private String desigId;

    @JsonProperty(value = "EXP_TYPE")
    private String expType;

    @JsonProperty(value = "PRJ_DETAILS")
    private String prjDetails;

    @JsonProperty(value = "PRJ_ROLE")
    private String prjRole;

    @JsonProperty(value = "PRJ_TITLE")
    private String prjTitle;

    @JsonProperty(value = "PRJ_SKILLS")
    private String prjSkills;

    @JsonProperty(value = "PRJ_SKILLS_ID")
    private List<String> prjSkillsId;

    @JsonProperty(value = "RESID")
    private Integer resId;

    @JsonProperty(value = "CITY_ID")
    private List<String> cityIds;

    @JsonProperty(value = "CITY")
    private String cityLabels;

    @JsonProperty(value = "COURSE_ID")
    private String courseId;

    @JsonProperty(value = "COURSE_LABEL")
    private String courseLabel;

    @JsonProperty(value = "EDUCATION_TYPE")
    private String educationType;

    @JsonProperty(value = "SPEC_ID")
    private String specId;

    @JsonProperty(value = "SPEC_LABEL")
    private String specificationLabel;

    @JsonProperty(value = "COURSE_TYPE")
    private String courseType;

    @JsonProperty(value = "ENTITY_INSTITUTE_ID")
    private String instituteId;

    @JsonProperty(value = "ENTITY_INSTITUTE_LABEL")
    private String instituteLabel;

    @JsonProperty(value = "IS_PREMIUM")
    private boolean isPremium;

}


