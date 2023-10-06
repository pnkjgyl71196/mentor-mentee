package com.ie.naukri.search.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ie.naukri.search.commons.core.dtos.SearchResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MentorSearchResponseDto implements SearchResponseDTO {

    List<MentorDto> mentors;

    long totalCount;
}
