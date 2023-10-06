package com.ie.naukri.search.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MentorSearchRequestModel {

    private String resId;
    private String totalExp;

    private Integer ctc;

    private String skillId;

    private String designationId;

    private String skill;
}
