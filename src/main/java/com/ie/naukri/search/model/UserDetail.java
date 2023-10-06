package com.ie.naukri.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetail {

    @JsonProperty(value = "ABSOLUTE_CTC")
int ABSOLUTE_CTC;
    @JsonProperty(value = "TOTAL_EXP")
String TOTAL_EXP;

}
