package com.github.azeroth.game.networking.packet.hotfix;


import com.github.azeroth.game.networking.ServerPacket;

class AvailableHotfixes extends ServerPacket {

    public int virtualRealmAddress;
    public MultiMap<Integer, HotfixRecord> hotfixes;


    public AvailableHotfixes(int virtualRealmAddress, MultiMap<Integer, HotfixRecord> hotfixes) {
        super(ServerOpcode.AvailableHotfixes);
        virtualRealmAddress = virtualRealmAddress;
        hotfixes = hotfixes;
    }

    @Override
    public void write() {
        this.writeInt32(virtualRealmAddress);
        this.writeInt32(hotfixes.keySet().size());

        for (var key : hotfixes.keySet()) {
            hotfixes.get(key)[0].ID.write(this);
        }
    }
}
