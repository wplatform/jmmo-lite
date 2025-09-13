package com.github.azeroth.game.networking.packet.garrison;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class GetGarrisonInfoResult extends ServerPacket {
    public int factionIndex;
    public ArrayList<GarrisonInfo> garrisons = new ArrayList<>();
    public ArrayList<FollowerSoftCapInfo> followerSoftCaps = new ArrayList<>();

    public GetGarrisonInfoResult() {
        super(ServerOpcode.GetGarrisonInfoResult);
    }

    @Override
    public void write() {
        this.writeInt32(factionIndex);
        this.writeInt32(garrisons.size());
        this.writeInt32(followerSoftCaps.size());

        for (var followerSoftCapInfo : followerSoftCaps) {
            followerSoftCapInfo.write(this);
        }

        for (var garrison : garrisons) {
            garrison.write(this);
        }
    }
}
