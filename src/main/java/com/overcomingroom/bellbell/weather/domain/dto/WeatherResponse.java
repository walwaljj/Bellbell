package com.overcomingroom.bellbell.weather.domain.dto;

import com.overcomingroom.bellbell.weather.domain.CategoryType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class WeatherResponse {

  private Map<CategoryType, String> weatherInfo;
}
