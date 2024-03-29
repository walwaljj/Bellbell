package com.overcomingroom.bellbell.resolver;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginUser {

    private String accessToken;

    public static LoginUser of(String accessToken) {
        return new LoginUser(accessToken.substring(7));
    }
}
