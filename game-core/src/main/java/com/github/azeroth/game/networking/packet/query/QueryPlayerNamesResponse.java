package com.github.azeroth.game.networking.packet.query;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class QueryPlayerNamesResponse extends ServerPacket {
    public ArrayList<NameCacheLookupResult> players = new ArrayList<>();

    public QueryPlayerNamesResponse() {
        super(ServerOpcode.QueryPlayerNamesResponse);
    }

    @Override
    public void write() {
        this.writeInt32(players.size());

        for (var lookupResult : players) {
            lookupResult.write(this);
        }
    }
}
