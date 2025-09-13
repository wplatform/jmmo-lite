package com.github.azeroth.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResult {

    public static final int LOGIN = 1;
    public static final int LEGAL = 2;
    public static final int AUTHENTICATOR = 3;
    public static final int DONE = 4;

    private State authentication_state;
    private String error_code;
    private String error_message;
    private String url;
    private String login_ticket;

    public enum State {
        LOGIN,
        LEGAL,
        AUTHENTICATOR,
        DONE;
    }

}
