package com.overcomingroom.bellbell.weather.domain.dto;

import com.overcomingroom.bellbell.basicNotification.domain.dto.AbstractBasicNotificationDto;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WeatherInfoDto extends AbstractBasicNotificationDto {

  private final String address;

  @Builder
  public WeatherInfoDto(Boolean isActivated, String day, String time, String address) {
    super(isActivated, day, time);
    this.address = address;
  }

  @Override
  public String toString() {
    return "WeatherInfoDto{" +
        "isActivated='" + getIsActivated() + '\'' +
        "address='" + address + '\'' +
        "day='" + getDay() + '\'' +
        "time='" + getTime() + '\'' +
        '}';
  }
}
