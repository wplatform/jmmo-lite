package com.github.azeroth.game.networking.opcode;

public sealed interface OpCode permits ClientOpCode, ServerOpCode {

    short MAX_OPCODE = 0x3FFF;
    short NUM_OPCODE_HANDLERS = (MAX_OPCODE + 1);
    short UNKNOWN_OPCODE = (short) 0xFFFF;
    short NULL_OPCODE = (short) 0xBADD;


    default boolean isClientToServer() {
        return this instanceof ClientOpCode;
    }

    default boolean isServerToClient() {
        return this instanceof ServerOpCode;
    }

    int getCode();
}
