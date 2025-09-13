package com.github.azeroth.game.server;// C# TO JAVA CONVERTER WARNING: Java does not allow user-defined second types. The behavior of this class may differ from the original:

final class PacketCounter {
    public long lastReceiveTime;
    public int amountCounter;

    public PacketCounter clone() {
        PacketCounter varCopy = new PacketCounter();

        varCopy.lastReceiveTime = this.lastReceiveTime;
        varCopy.amountCounter = this.amountCounter;

        return varCopy;
    }
}
