package com.ie.naukri.search.model;

import com.ie.naukri.search.commons.core.dtos.SearchResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MentorDto implements SearchResponseDTO {


    private String totalCount;

    private String resId;

    private String currentDesignation;

    private String location;

    private String higherEducation;

    private String skills;

    private String totalExp;

    private String name;

    private String currentOrg;

    private String rating;

}
