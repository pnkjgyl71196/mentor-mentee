package com.ie.naukri.search.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ie.naukri.search.commons.core.models.IndexingDocument;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ElasticSearchDocument extends IndexingDocument {

    //t1.KEYWORDS_ID as EXP_KEYWORDS_ID, DESIG_ID, EXP_TYPE, DETAILS as PRJ_DETAILS,t3.ROLE as PRJ_ROLE,t3.TITLE as PRJ_TITLE,SKILLS as PRJ_SKILLS,SKILLS_ID as PRJ_SKILLS_ID

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

}


