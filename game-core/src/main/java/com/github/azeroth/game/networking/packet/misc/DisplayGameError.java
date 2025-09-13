package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ServerPacket;

public class DisplayGameError extends ServerPacket {
    private final Gameerror error;
    private final Integer arg;
    private final Integer arg2;

    public DisplayGameError(GameError error) {
        super(ServerOpcode.DisplayGameError);
        error = error;
    }

    public DisplayGameError(GameError error, int arg) {
        this(error);
        arg = arg;
    }

    public DisplayGameError(GameError error, int arg1, int arg2) {
        this(error);
        arg = arg1;
        arg2 = arg2;
    }

    @Override
    public void write() {
        this.writeInt32((int) error.getValue());
        this.writeBit(arg != null);
        this.writeBit(arg2 != null);
        this.flushBits();

        if (arg != null) {
            this.writeInt32(arg.intValue());
        }

        if (arg2 != null) {
            this.writeInt32(arg2.intValue());
        }
    }
}
