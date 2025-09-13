package com.github.azeroth.game.networking.packet.clientconfig;


public class AccountDataTimes extends ServerPacket {
    public ObjectGuid playerGuid = ObjectGuid.EMPTY;
    public long serverTime;
    public long[] accountTimes = new long[AccountDataTypes.max.getValue()];

    public AccountDataTimes() {
        super(ServerOpcode.AccountDataTimes);
    }

    @Override
    public void write() {
        this.writeGuid(playerGuid);
        this.writeInt64(serverTime);

        for (var accounttime : accountTimes) {
            this.writeInt64(accounttime);
        }
    }
}
