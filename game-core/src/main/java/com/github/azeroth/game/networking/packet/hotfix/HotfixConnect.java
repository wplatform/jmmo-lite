package com.github.azeroth.game.networking.packet.hotfix;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

class HotfixConnect extends ServerPacket {
    public ArrayList<HotfixData> hotfixes = new ArrayList<>();
    public byteBuffer hotfixContent = new byteBuffer();

    public HotfixConnect() {
        super(ServerOpcode.HotfixConnect);
    }

    @Override
    public void write() {
        this.writeInt32(hotfixes.size());

        for (var hotfix : hotfixes) {
            hotfix.write(this);
        }

        this.writeInt32(hotfixContent.getSize());
        this.writeBytes(hotfixContent);
    }

    public static class HotfixData {
        public Hotfixrecord record = new hotfixRecord();

        public int size;

        public final void write(WorldPacket data) {
            record.write(data);
            data.writeInt32(size);
            data.writeBits((byte) record.HotfixStatus.getValue(), 3);
            data.flushBits();
        }
    }
}
