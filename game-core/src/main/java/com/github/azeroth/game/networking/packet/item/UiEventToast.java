package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.WorldPacket;

public final class UiEventToast {
    public int uiEventToastID;
    public int asset;

    public void write(WorldPacket data) {
        data.writeInt32(uiEventToastID);
        data.writeInt32(asset);
    }

    public UiEventToast clone() {
        UiEventToast varCopy = new UiEventToast();

        varCopy.uiEventToastID = this.uiEventToastID;
        varCopy.asset = this.asset;

        return varCopy;
    }
}
