package com.overcomingroom.bellbell.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CronExpression {

  private int minute;
  private int hour;
  private String day;

  @Override
  public String toString() {
    return String.format("0 %d %d ? * %s", minute, hour, day);
  }
}
