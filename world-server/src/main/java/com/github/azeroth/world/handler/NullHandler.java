package com.github.azeroth.world.handler;

import com.github.azeroth.common.Logs;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.world.network.WorldRequest;
import com.github.azeroth.world.network.WorldResponse;

public class NullHandler {

    public void handleNull(WorldRequest request, WorldResponse response) {
        var aNull = request.receiveObject(ClientPacket.Null.class);
        Logs.NETWORK_OPCODE.error("Received unhandled opcode {} from {}", aNull.getOpcode(), request.getSession().getPlayer());
    }


}
