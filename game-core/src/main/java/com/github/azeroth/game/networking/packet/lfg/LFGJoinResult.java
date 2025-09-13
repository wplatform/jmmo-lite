package com.github.azeroth.game.networking.packet.lfg;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class LFGJoinResult extends ServerPacket {
    public Rideticket ticket = new rideTicket();
    public byte result;
    public byte resultDetail;
    public ArrayList<LFGblackListPkt> blackList = new ArrayList<>();
    public ArrayList<String> blackListNames = new ArrayList<>();

    public LFGJoinResult() {
        super(ServerOpcode.LfgJoinResult);
    }

    @Override
    public void write() {
        ticket.write(this);

        this.writeInt8(result);
        this.writeInt8(resultDetail);
        this.writeInt32(blackList.size());
        this.writeInt32(blackListNames.size());

        for (var blackList : blackList) {
            blackList.write(this);
        }

        for (var str : blackListNames) {
            this.writeBits(str.getBytes().length + 1, 24);
        }

        for (var str : blackListNames) {
            if (!str.isEmpty()) {
                this.writeCString(str);
            }
        }
    }
}
