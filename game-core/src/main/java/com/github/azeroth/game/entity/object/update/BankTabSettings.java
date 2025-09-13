package com.github.azeroth.game.entity.object.update;

public final class BankTabSettings extends UpdateMaskObject {

    private String name;
    private String icon;
    private String description;
    private int depositFlags;

    public BankTabSettings() {
        super(4);
    }

    @Override
    public void clearChangesMask() {

    }
}
