package com.github.azeroth.game.event;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public final class AccountEvent extends WorldEvent {
    // Called when an account logged in succesfully
    public static final byte ON_ACCOUNT_LOGIN = 0;

    // Called when an account login failed
    public static final byte ON_FAILED_ACCOUNT_LOGIN = 1;

    // Called when Email is successfully changed for Account
    public static final byte onEmailChange = 2;

    // Called when Email failed to change for Account
    public static final byte onFailedEmailChange = 3;

    // Called when Password is successfully changed for Account
    public static final byte onPasswordChange = 4;

    // Called when Password failed to change for Account
    public static final byte onFailedPasswordChange = 5;

    public final int eventType;

    public AccountEvent(int source, int eventType) {
        super(source);
        this.eventType = eventType;
    }
}

