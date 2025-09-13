package com.github.azeroth.game.globals;

public class ProductAddon {
    private int displayInfoEntry;
    private byte disableListing;
    private byte disableBuy;
    private byte nameColorIndex;
    private String scriptName = "";
    private String comment = "";

    public final int getDisplayInfoEntry() {
        return displayInfoEntry;
    }

    public final void setDisplayInfoEntry(int value) {
        displayInfoEntry = value;
    }

    public final byte getDisableListing() {
        return disableListing;
    }

    public final void setDisableListing(byte value) {
        disableListing = value;
    }

    public final byte getDisableBuy() {
        return disableBuy;
    }

    public final void setDisableBuy(byte value) {
        disableBuy = value;
    }

    public final byte getNameColorIndex() {
        return nameColorIndex;
    }

    public final void setNameColorIndex(byte value) {
        nameColorIndex = value;
    }

    public final String getScriptName() {
        return scriptName;
    }

    public final void setScriptName(String value) {
        scriptName = value;
    }

    public final String getComment() {
        return comment;
    }

    public final void setComment(String value) {
        comment = value;
    }
}
