package com.ie.naukri.search.model;

import com.ie.naukri.search.commons.core.dtos.SearchResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MentorSearchResponseDto implements SearchResponseDTO {

    List<MentorDto> mentors;

    long totalCount;
}
