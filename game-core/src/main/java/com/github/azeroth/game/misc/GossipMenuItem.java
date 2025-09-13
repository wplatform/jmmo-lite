package com.github.azeroth.game.misc;


public class GossipMenuItem {
    private int gossipOptionId;
    private int orderIndex;
    private GossipoptionNpc optionNpc = GossipOptionNpc.values()[0];
    private String optionText;
    private int language;
    private GossipOptionflags flags = GossipOptionFlags.values()[0];
    private Integer gossipNpcOptionId = null;
    private boolean boxCoded;
    private int boxMoney;
    private String boxText;
    private Integer spellId = null;
    private Integer overrideIconId = null;
    // action data
    private int actionMenuId;
    private int actionPoiId;
    // additional scripting identifiers
    private int sender;
    private int action;

    public final int getGossipOptionId() {
        return gossipOptionId;
    }

    public final void setGossipOptionId(int value) {
        gossipOptionId = value;
    }

    public final int getOrderIndex() {
        return orderIndex;
    }

    public final void setOrderIndex(int value) {
        orderIndex = value;
    }

    public final GossipOptionNpc getOptionNpc() {
        return optionNpc;
    }

    public final void setOptionNpc(GossipOptionNpc value) {
        optionNpc = value;
    }

    public final String getOptionText() {
        return optionText;
    }

    public final void setOptionText(String value) {
        optionText = value;
    }

    public final int getLanguage() {
        return language;
    }

    public final void setLanguage(int value) {
        language = value;
    }

    public final GossipOptionFlags getFlags() {
        return flags;
    }

    public final void setFlags(GossipOptionFlags value) {
        flags = value;
    }

    public final Integer getGossipNpcOptionId() {
        return gossipNpcOptionId;
    }

    public final void setGossipNpcOptionId(Integer value) {
        gossipNpcOptionId = value;
    }

    public final boolean getBoxCoded() {
        return boxCoded;
    }

    public final void setBoxCoded(boolean value) {
        boxCoded = value;
    }

    public final int getBoxMoney() {
        return boxMoney;
    }

    public final void setBoxMoney(int value) {
        boxMoney = value;
    }

    public final String getBoxText() {
        return boxText;
    }

    public final void setBoxText(String value) {
        boxText = value;
    }

    public final Integer getSpellId() {
        return spellId;
    }

    public final void setSpellId(Integer value) {
        spellId = value;
    }

    public final Integer getOverrideIconId() {
        return overrideIconId;
    }

    public final void setOverrideIconId(Integer value) {
        overrideIconId = value;
    }

    public final int getActionMenuId() {
        return actionMenuId;
    }

    public final void setActionMenuId(int value) {
        actionMenuId = value;
    }

    public final int getActionPoiId() {
        return actionPoiId;
    }

    public final void setActionPoiId(int value) {
        actionPoiId = value;
    }

    public final int getSender() {
        return sender;
    }

    public final void setSender(int value) {
        sender = value;
    }

    public final int getAction() {
        return action;
    }

    public final void setAction(int value) {
        action = value;
    }
}
