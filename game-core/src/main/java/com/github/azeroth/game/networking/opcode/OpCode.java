package com.github.azeroth.game.networking.opcode;

public sealed interface OpCode permits ClientOpCode, ServerOpCode {

    int MAX_OPCODE = 0x3FFF;
    int NUM_OPCODE_HANDLERS = (MAX_OPCODE + 1);
    int UNKNOWN_OPCODE = 0xFFFF;
    int NULL_OPCODE = 0xBADD;


    default boolean isClientToServer() {
        return this instanceof ClientOpCode;
    }

    default boolean isServerToClient() {
        return this instanceof ServerOpCode;
    }

    default <T> T as(Class<T> enumClass) {
        return enumClass.cast(this);
    }

    default boolean isNullOpCode() {
        return getValue() == 0xBADD;
    }


    int getValue();
}
