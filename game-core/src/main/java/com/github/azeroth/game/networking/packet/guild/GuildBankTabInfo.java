package com.github.azeroth.game.networking.packet.guild;

public final class GuildBankTabInfo {
    public int tabIndex;
    public String name;
    public String icon;

    public GuildBankTabInfo clone() {
        GuildBankTabInfo varCopy = new GuildBankTabInfo();

        varCopy.tabIndex = this.tabIndex;
        varCopy.name = this.name;
        varCopy.icon = this.icon;

        return varCopy;
    }
}
