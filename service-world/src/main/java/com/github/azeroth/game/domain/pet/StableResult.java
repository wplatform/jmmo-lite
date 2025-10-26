package com.github.azeroth.game.domain.pet;

public enum StableResult {
    NotEnoughMoney        (1),                              // "you don't have enough money"
    InvalidSlot           (3),                              // "That slot is locked"
    StableSuccess         (8),                              // stable success
    UnstableSuccess       (9),                              // unstable/swap success
    BuySlotSuccess        (10),                             // buy slot success
    CantControlExotic     (11),                             // "you are unable to control exotic creatures"
    InternalError         (12);                             // "Internal pet error"

    public final byte value;

    StableResult(int value) {
        this.value = (byte) value;
    }
}
