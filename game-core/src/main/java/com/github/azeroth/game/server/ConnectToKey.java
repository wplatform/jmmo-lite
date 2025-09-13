package com.github.azeroth.game.server;


public final class ConnectToKey {
    public int accountId;
    public ConnectionType connectionType = ConnectionType.values()[0];
    public long key;

    public long getRaw() {
        return ((long) accountId | ((long) connectionType.getValue() << 32) | (key << 33));
    }

    public void setRaw(long value) {
        accountId = (int) (value & 0xFFFFFFFF);
        connectionType = ConnectionType.forValue((value >>> 32) & 1);
        key = (value >>> 33);
    }

    public ConnectToKey clone() {
        ConnectToKey varCopy = new connectToKey();

        varCopy.accountId = this.accountId;
        varCopy.connectionType = this.connectionType;
        varCopy.key = this.key;

        return varCopy;
    }
}
