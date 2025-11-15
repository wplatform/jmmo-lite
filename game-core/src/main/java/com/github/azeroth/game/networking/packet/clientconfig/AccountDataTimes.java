package com.github.azeroth.game.networking.packet.clientconfig;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class AccountDataTimes extends ServerPacket {
    public ObjectGuid playerGuid;
    public long serverTime;
    public long[] accountTimes;

    public AccountDataTimes() {
        super(ServerOpCode.SMSG_ACCOUNT_DATA_TIMES);
    }

    @Override
    public void write() {
        this.writeGuid(playerGuid);
        this.writeInt64(serverTime);

        for (var accountTime : accountTimes) {
            this.writeInt64(accountTime);
        }
    }
}
