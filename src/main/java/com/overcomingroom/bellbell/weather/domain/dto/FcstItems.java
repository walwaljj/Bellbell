package com.overcomingroom.bellbell.weather.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FcstItems {

    @JsonProperty("item")
    private List<FcstDto> fcstList;

    public FcstItems(List<FcstDto> fcstList) {
        this.fcstList = fcstList;
    }
}
