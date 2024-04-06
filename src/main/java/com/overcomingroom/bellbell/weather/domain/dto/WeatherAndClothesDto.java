package com.overcomingroom.bellbell.weather.domain.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Builder
@Getter
public class WeatherAndClothesDto {

    private String fcstDate; // 예보 일자
    private String baseTime; // 발표 시각
    private LocalTime now; // 현재 시각
    private String location; // 발표 지역
    private String weather;
    private Float temp;
    private String clothes;

    public static String weatherAndClothesInfo(WeatherAndClothesDto weatherAndClothesDto) {
        StringBuilder sb = new StringBuilder();
        String date = weatherAndClothesDto.getFcstDate().concat(weatherAndClothesDto.getBaseTime());
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");
        LocalDateTime dateTime = LocalDateTime.parse(date, inputFormat);
        String outputDateStr = dateTime.format(outputFormat);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시 mm분");

        sb.append(outputDateStr).append(" 기준 날씨 입니다.\n")
                .append("현재 시각 : ").append(LocalTime.now().format(formatter)).append("\n")
                .append("우리 동네 <")
                .append(weatherAndClothesDto.getLocation())
                .append("> 의 ")
                .append("현재 날씨는 ")
                .append(weatherAndClothesDto.getWeather())
                .append(" 이며 온도는 ")
                .append(weatherAndClothesDto.getTemp())
                .append("℃ 입니다.\n")
                .append("오늘 추천 옷차림은 ")
                .append(weatherAndClothesDto.getClothes())
                .append("입니다.\n")
                .append("좋은 하루 보내세요!");

        return sb.toString();
    }
}
