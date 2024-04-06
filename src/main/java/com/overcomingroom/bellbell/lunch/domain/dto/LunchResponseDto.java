package com.overcomingroom.bellbell.lunch.domain.dto;

import com.overcomingroom.bellbell.basicNotification.domain.dto.AbstractBasicNotificationDto;
import com.overcomingroom.bellbell.lunch.domain.entity.Menu;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class LunchResponseDto extends AbstractBasicNotificationDto {

    private final List<Menu> menuList;

    @Builder
    public LunchResponseDto(Boolean isActivated, String day, String time, List<Menu> menuList) {
        super(isActivated, day, time);
        this.menuList = menuList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n현재 시각 : ").append(getTime() + "\n")
                .append("점심 추천 메뉴 입니다.\n")
                .append(menuList.stream()
                        .map(menu -> "[" + menu.getMenu() + "]\n")
                        .collect(Collectors.joining()));

        return sb.toString();
    }
}
