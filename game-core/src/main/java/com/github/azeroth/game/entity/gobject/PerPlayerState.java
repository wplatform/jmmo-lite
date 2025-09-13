package com.github.azeroth.game.entity.gobject;


import java.time.LocalDateTime;


public class PerPlayerState {
    public GOState state = null;
    private LocalDateTime validUntil = LocalDateTime.MIN;
    private boolean despawned;

    public final LocalDateTime getValidUntil() {
        return validUntil;
    }

    public final void setValidUntil(LocalDateTime value) {
        validUntil = value;
    }

    public final boolean getDespawned() {
        return despawned;
    }

    public final void setDespawned(boolean value) {
        despawned = value;
    }
}
