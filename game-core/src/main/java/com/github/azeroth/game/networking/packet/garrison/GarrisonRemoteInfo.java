package com.github.azeroth.game.networking.packet.garrison;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class GarrisonRemoteInfo extends ServerPacket {
    public ArrayList<GarrisonRemoteSiteInfo> sites = new ArrayList<>();

    public GarrisonRemoteInfo() {
        super(ServerOpcode.GarrisonRemoteInfo);
    }

    @Override
    public void write() {
        this.writeInt32(sites.size());

        for (var site : sites) {
            site.write(this);
        }
    }
}
