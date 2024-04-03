package com.overcomingroom.bellbell.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CronExpression {

    private int minute;
    private int hour;
    private String day;

    @Override
    public String toString() {
        return String.format("0 %d %d ? * %s", minute, hour, day);
    }

    /**
     * 알림 설정 요일과 시간을 CronExpression 필드에 맞게 변환합니다.
     * @param day 요일
     * @param time hh : mm 형식의 시간
     * @return cronExpression
     */
    public static String getCronExpression(String day, String time) {
        String[] split = time.split(":");
        return CronExpression.builder()
                .day(day)
                .hour(Integer.parseInt(split[0]))
                .minute(Integer.parseInt(split[1]))
                .build().toString();
    }
}
