package com.overcomingroom.bellbell.weather.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.overcomingroom.bellbell.weather.domain.CategoryType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class FcstDto {
    /** 발표일자 */
    private String baseDate;

    /** 발표시각 */
    private String baseTime;

    /** 예보일자 */
    private String fcstDate;

    /** 예보시각 */
    private String fcstTime;

    /** 자료구분문자 */
    private CategoryType category;

    /** 예보지점 X 좌표 */
    private float nx;

    /** 예보지점 Y 좌표 */
    private float ny;


    /** 예보 값 */
    @JsonProperty("fcstValue")
    @Setter
    private String fcstValue;

    @Setter
    private String categoryName;
}
