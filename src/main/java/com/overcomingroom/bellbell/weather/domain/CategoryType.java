package com.overcomingroom.bellbell.weather.domain;

/**
 * 코드값 정보
 */
public enum CategoryType {
    POP("강수확률", "강수확률"),
    R06("6시간 강수량", "범주 (1 mm)"),
    S06("6시간 신적설", "범주 (1 cm)"),
    SKY("하늘상태", "코드값"),
    T3H("3시간 기온", "℃"),
    TMN("아침 최저기온", "℃"),
    TMX("낮 최저기온", "℃"),
    WAV("파고", "M"),
    T1H("기온", "℃"),
    RN1("1시간 강수량", "mm"),
    UUU("동서바람성분", "m/s"),
    VVV("남북바람성분", "m/s"),
    REH("습도", "%"),
    PTY("강수형태", "코드값"),
    VEC("풍향", "m/s"),
    WSD("풍속", "1"),
    TMP("1시간 기온", "℃");

    private String name;

    private String unit;

    private CategoryType(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }

    public static String getCodeInfo(String name, String value) {
        CategoryType c = CategoryType.valueOf(name);
        if (c == CategoryType.PTY) {
            switch (value) {
                case "0":
                    return "없음";
                case "1":
                    return "비";
                case "2":
                    return "비/눈";
                case "3":
                    return "눈";
                case "4":
                    return "소나기";
            }
        } else if (c == CategoryType.SKY) {
            switch (value) {
                case "1":
                    return "맑음";
                case "3":
                    return "구름많음";
                case "4":
                    return "흐림";
            }
        }
        return value;
    }
}
