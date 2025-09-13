package com.github.azeroth.game.map;

public final class UpdateAdditionalSaveDataEvent {
    public String key;
    public Object value;

    public UpdateAdditionalSaveDataEvent() {
    }

    public UpdateAdditionalSaveDataEvent(String key, Object value) {
        key = key;
        value = value;
    }

    public UpdateAdditionalSaveDataEvent clone() {
        UpdateAdditionalSaveDataEvent varCopy = new UpdateAdditionalSaveDataEvent();

        varCopy.key = this.key;
        varCopy.value = this.value;

        return varCopy;
    }
}
