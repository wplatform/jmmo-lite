package com.github.azeroth.game.networking.packet.who;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class WhoRequest {
    public int minLevel;
    public int maxLevel;
    public String name;
    public String virtualRealmName;
    public String guild;
    public String guildVirtualRealmName;
    public long raceFilter;
    public int classFilter = -1;
    public ArrayList<String> words = new ArrayList<>();
    public boolean showEnemies;
    public boolean showArenaPlayers;
    public boolean exactName;
    public WhoRequestserverInfo serverInfo = null;

    public final void read(WorldPacket data) {
        minLevel = data.readInt32();
        maxLevel = data.readInt32();
        raceFilter = data.readInt64();
        classFilter = data.readInt32();

        var nameLength = data.<Integer>readBit(6);
        var virtualRealmNameLength = data.<Integer>readBit(9);
        var guildNameLength = data.<Integer>readBit(7);
        var guildVirtualRealmNameLength = data.<Integer>readBit(9);
        var wordsCount = data.<Integer>readBit(3);

        showEnemies = data.readBit();
        showArenaPlayers = data.readBit();
        exactName = data.readBit();

        if (data.readBit()) {
            serverInfo = new WhoRequestServerInfo();
        }

        data.resetBitPos();

        for (var i = 0; i < wordsCount; ++i) {
            words.add(data.readString(data.<Integer>readBit(7)));
            data.resetBitPos();
        }

        name = data.readString(nameLength);
        virtualRealmName = data.readString(virtualRealmNameLength);
        guild = data.readString(guildNameLength);
        guildVirtualRealmName = data.readString(guildVirtualRealmNameLength);

        if (serverInfo != null) {
            serverInfo.getValue().read(data);
        }
    }
}
