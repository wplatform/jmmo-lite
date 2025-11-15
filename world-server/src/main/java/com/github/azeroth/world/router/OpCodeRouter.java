package com.github.azeroth.world.router;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.opcode.ClientOpCode;
import com.github.azeroth.net.router.Router;
import com.github.azeroth.world.network.WorldRequest;
import com.github.azeroth.world.network.WorldResponse;

import java.util.EnumMap;
import java.util.function.BiConsumer;

public final class OpCodeRouter extends Router<OpCodeRouter, WorldRequest, WorldResponse> {
    private final EnumMap<ClientOpCode, BiConsumer<WorldRequest, WorldResponse>> handler = new EnumMap<>(ClientOpCode.class);

    @Override
    protected BiConsumer<WorldRequest, WorldResponse> identity(WorldRequest request) {
        ClientPacket worldPacket = request.receiveObject(ClientPacket.class);
        return handler.get((ClientOpCode) worldPacket.getOpcode());
    }

    public OpCodeRouter route(ClientOpCode opCode, BiConsumer<WorldRequest, WorldResponse> consumer) {
        handler.put(opCode, consumer);
        return this;
    }

}
