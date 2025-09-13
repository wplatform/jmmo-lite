package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class QuestGiverOfferReward {
    public ObjectGuid questGiverGUID = ObjectGuid.EMPTY;
    public int questGiverCreatureID = 0;
    public int questID = 0;
    public boolean autoLaunched = false;
    public int suggestedPartyMembers = 0;
    public QuestRewards rewards = new QuestRewards();
    public ArrayList<QuestDescEmote> emotes = new ArrayList<>();
    public int[] questFlags = new int[3]; // Flags and FlagsEx

    public final void write(WorldPacket data) {
        data.writeGuid(questGiverGUID);
        data.writeInt32(questGiverCreatureID);
        data.writeInt32(questID);
        data.writeInt32(QuestFlags[0]); // Flags
        data.writeInt32(QuestFlags[1]); // FlagsEx
        data.writeInt32(suggestedPartyMembers);

        data.writeInt32(emotes.size());

        for (var emote : emotes) {
            data.writeInt32(emote.type);
            data.writeInt32(emote.delay);
        }

        data.writeBit(autoLaunched);
        data.writeBit(false); // Unused
        data.flushBits();

        rewards.write(data);
    }
}
