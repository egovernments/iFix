package org.digit.program.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SanctionSearchResponse {

    @JsonProperty("header")
    RequestHeader header;

    @JsonProperty("sanctions")
    List<Sanction> sanctions;

}