package com.ie.naukri.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ie.naukri.search.commons.core.dtos.SearchRequestDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MentorSearchRequestDto implements SearchRequestDTO {

    @JsonProperty(value = "totalExp")
    private String totalExp;

    @JsonProperty(value = "ctc")
    private Integer ctc;

    @JsonProperty(value = "skillId")
    private String skillId;

    @JsonProperty(value = "designation")
    private String designation;

    @JsonProperty(value = "currentOrg")
    private String orgn;

}
