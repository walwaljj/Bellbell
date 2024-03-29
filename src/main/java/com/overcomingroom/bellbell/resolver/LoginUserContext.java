package com.overcomingroom.bellbell.resolver;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginUserContext {
    private static final ThreadLocal<LoginUser> loginUserContext = new ThreadLocal<>();

    public LoginUser getLoginUser() {
        return loginUserContext.get();
    }

    public void save(String accessToken){
        loginUserContext.set(LoginUser.of(accessToken));
    }

    public void remove(){
        loginUserContext.remove();
    }

}
