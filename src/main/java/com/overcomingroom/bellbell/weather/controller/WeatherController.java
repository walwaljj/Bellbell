package com.overcomingroom.bellbell.weather.controller;


import com.overcomingroom.bellbell.weather.domain.CategoryType;
import com.overcomingroom.bellbell.weather.domain.dto.WeatherResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/v1")
public class WeatherController {

    @Value("${weather.api-key}")
    private String serviceKey;

    @Value("${weather.service-url}")
    private String apiUrl;

    @GetMapping("/weather1") // API 를 호출하고 JSON을 객체로 저장함.
    public ResponseEntity<String> callForecastApi1(
            @RequestParam(value = "base_date") String baseDate,
            @RequestParam(value = "base_time") String baseTime,
            @RequestParam(value = "nx") String nx,
            @RequestParam(value = "ny") String ny

    ) {
        HttpURLConnection urlConnection = null;
        InputStream stream = null;
        String result = null;

        LocalDate now = LocalDate.now();
        baseDate = now.toString().replaceAll("-", "");
        String numOfRows = "3";
        String pageNo = "1";
        String dataType = "json";
//        String nx = "55"; // 사용자 입력으로 변경 예정
//        String ny = "127"; // 사용자 입력으로 변경 예정

        StringBuilder builder = new StringBuilder();

        builder.append(apiUrl + "?");
        builder.append("serviceKey=" + serviceKey);
        builder.append("&numOfRows=" + numOfRows);
        builder.append("&pageNo=" + pageNo);
        builder.append("&base_date=" + baseDate);
        builder.append("&base_time=" + baseTime);
        builder.append("&nx=" + nx);
        builder.append("&ny=" + ny);
        builder.append("&dataType=" + dataType);

        try {
            URL url = new URL(builder.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            stream = getNetworkConnection(urlConnection);
            result = readStreamToString(stream);

            if (stream != null) stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private String readStreamToString(InputStream stream) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

        String readLine;
        while ((readLine = br.readLine()) != null) {
            result.append(readLine + "\n\r");
        }

        br.close();

        return result.toString();
    }

    private InputStream getNetworkConnection(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setConnectTimeout(3000);
        urlConnection.setReadTimeout(3000);
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoInput(true);

        if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code : " + urlConnection.getResponseCode());
        }
        return urlConnection.getInputStream();
    }

    @GetMapping("/weather2") // API 를 호출하고 JSON을 객체로 저장함.
    public ResponseEntity<String> callForecastApi2() throws IOException {

        LocalDate now = LocalDate.now();
        String baseDate = now.toString().replaceAll("-", "");
        String numOfRows = "3";
        String pageNo = "1";
        String baseTime = "0500";
        String dataType = "json";
        String nx = "55"; // 사용자 입력으로 변경 예정
        String ny = "127"; // 사용자 입력으로 변경 예정

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .queryParam("dataType", dataType);

        RestTemplate restTemplate = new RestTemplate();

        log.info(">>>>>>>>>>>>> start reading OpenAPI >>>>>>>>>>>>>");
        ResponseEntity<String> response = restTemplate.exchange(builder.build(true).toUri(), HttpMethod.GET, null, String.class);
        JSONObject jsonObject = new JSONObject(response.getBody()).getJSONObject("response").getJSONObject("body").getJSONObject("items");
        JSONArray jsonArray = jsonObject.getJSONArray("item");
        Map<CategoryType, String> weatherInfo = new HashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonData = jsonArray.getJSONObject(i);
            weatherInfo.put(CategoryType.valueOf(jsonData.get("category").toString()), jsonData.get("fcstValue").toString());
        }

        if (!response.getBody().startsWith("{")) {
            log.warn("예상치 못한 응답 형식: {}", response.getBody());
            return ResponseEntity.badRequest().body("잘못된 응답 형식입니다.");
        }
        return ResponseEntity.ok(new JSONObject(new WeatherResponse(weatherInfo)).toString());
    }

}
