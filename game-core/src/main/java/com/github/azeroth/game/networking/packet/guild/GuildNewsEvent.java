package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class GuildNewsEvent {
    public int id;
    public int completedDate;
    public int type;
    public int flags;
    public int[] data = new int[2];
    public ObjectGuid memberGuid = ObjectGuid.EMPTY;
    public ArrayList<ObjectGuid> memberList = new ArrayList<>();
    public itemInstance item;

    public final void write(WorldPacket data) {
        data.writeInt32(id);
        data.writePackedTime(completedDate);
        data.writeInt32(type);
        data.writeInt32(flags);

        for (byte i = 0; i < 2; i++) {
            data.writeInt32(Data[i]);
        }

        data.writeGuid(memberGuid);
        data.writeInt32(memberList.size());

        for (var memberGuid : memberList) {
            data.writeGuid(memberGuid);
        }

        data.writeBit(item != null);
        data.flushBits();

        if (item != null) {
            item.write(data);
        }
    }
}
