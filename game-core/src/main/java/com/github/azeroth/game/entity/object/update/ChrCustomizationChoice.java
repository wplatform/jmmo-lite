package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class ChrCustomizationChoice implements Comparable<ChrCustomizationChoice> {
    public int chrCustomizationOptionID;
    public int chrCustomizationChoiceID;

    public final int compareTo(ChrCustomizationChoice other) {
        return (new integer(chrCustomizationOptionID)).compareTo(other.chrCustomizationOptionID);
    }

    public final void writeCreate(WorldPacket data, WorldObject owner, Player receiver) {
        data.writeInt32(chrCustomizationOptionID);
        data.writeInt32(chrCustomizationChoiceID);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, WorldObject owner, Player receiver) {
        data.writeInt32(chrCustomizationOptionID);
        data.writeInt32(chrCustomizationChoiceID);
    }
}
