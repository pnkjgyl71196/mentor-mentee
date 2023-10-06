package com.ie.naukri.search.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ie.naukri.search.commons.core.dtos.SearchRequestDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class MentorSearchRequestDto implements SearchRequestDTO {

    @JsonProperty(value = "totalExp")
    private String totalExp;

    @JsonProperty(value = "ctc")
    private Integer ctc;

    @JsonProperty(value = "skillId")
    private String skillId;

    @JsonProperty(value = "skill")
    private String skill;

    @JsonProperty(value = "designationId")
    private String designationId;

    @JsonProperty(value = "resId")
    private String resId;
}
