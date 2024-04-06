package com.overcomingroom.bellbell.lunch.service;

import com.overcomingroom.bellbell.exception.CustomException;
import com.overcomingroom.bellbell.exception.ErrorCode;
import com.overcomingroom.bellbell.lunch.domain.entity.Menu;
import com.overcomingroom.bellbell.lunch.repository.MenuRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;

    @PostConstruct
    private void initMenu() {

        // 파일 경로 읽기
        String fileLocation = "csv/menu.csv";
        Path path = Paths.get(fileLocation);
        URI uri = path.toUri();

        List<Menu> menuList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new UrlResource(uri).getInputStream()))) {
            String line = "";

            while ((line = br.readLine()) != null) {
                Menu menu = new Menu(line);
                menuList.add(menu);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 데이터 중복 방지
        menuRepository.deleteAll();
        menuRepository.saveAll(menuList);
    }

    // 랜덤으로 메뉴를 선정함.
    public List<Menu> recommendMenus() {

        // id의 처음 ~ 마지막 번호 까지 랜덤 정수 생성
        Random random = new Random();
        List<Menu> findAllMenu = menuRepository.findAll();
        Long min = findAllMenu.get(0).getMenuId();
        Long max = findAllMenu.get(findAllMenu.size() - 1).getMenuId();

        // 메뉴 3가지 추천
        List<Menu> menuList = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            Long randomNumber = random.nextLong(max - min + 1) + min;
            menuList.add(menuRepository.findById(randomNumber).orElseThrow(() -> new CustomException(ErrorCode.FAILED_TO_GENERATE_RANDOM_NUMBER)));
        }
        return menuList;
    }
}
