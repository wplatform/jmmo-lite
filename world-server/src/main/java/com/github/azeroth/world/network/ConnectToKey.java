package com.github.azeroth.world.network;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConnectToKey {

    public int accountId;
    public ConnectionType connectionType;
    public long key;


    public ConnectToKey(long raw) {
        this.accountId = (int)(raw & 0xFFFFFFFF);
        this.connectionType = ConnectionType.values()[(int)((raw >>> 32) & 1)];
        this.key = (raw >>> 33);
    }

    public long getRaw() {
        return ((long) accountId | ((long)connectionType.ordinal() << 32) | (key << 33));
    }


}
