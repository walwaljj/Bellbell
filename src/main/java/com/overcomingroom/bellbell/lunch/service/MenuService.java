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

}
