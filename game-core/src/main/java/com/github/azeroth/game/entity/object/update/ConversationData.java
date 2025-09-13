package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

public final class ConversationData extends UpdateMaskObject {
    public FieldType<Boolean> dontPlayBroadcastTextSounds = new FieldType<>(0, 1);
    public FieldType<ArrayList<ConversationLine>> lines = new FieldType<ArrayList<ConversationLine>>(0, 2);
    public Dynamic<ConversationActorField> actors = new Dynamic<ConversationActorField>(0, 3);

    public FieldType<Integer> lastLineEndTime = new FieldType<>(0, 4);

    public FieldType<Integer> progress = new FieldType<>(0, 5);

    public FieldType<Integer> flags = new FieldType<>(0, 6);

    public ConversationData() {
        super(0, TypeId.conversation, 7);
    }

    public final void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Conversation owner, Player receiver) {
        data.writeInt32(lines.get().size());
        data.writeInt32(getViewerLastLineEndTime(this, owner, receiver));
        data.writeInt32(progress);

        for (var i = 0; i < lines.get().size(); ++i) {
            lines.get().get(i).writeCreate(data, owner, receiver);
        }

        data.writeBit(dontPlayBroadcastTextSounds);
        data.writeInt32(actors.size());

        for (var i = 0; i < actors.size(); ++i) {
            actors.get(i).writeCreate(data, owner, receiver);
        }

        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Conversation owner, Player receiver) {
        writeUpdate(data, getChangesMask(), false, owner, receiver);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, Conversation owner, Player receiver) {
        data.writeBits(getChangesMask().getBlock(0), 6);

        if (getChangesMask().get(0)) {
            if (getChangesMask().get(1)) {
                data.writeBit(dontPlayBroadcastTextSounds);
            }

            if (changesMask.get(2)) {
                ArrayList<ConversationLine> list = lines;
                data.writeBits(list.size(), 32);

                for (var i = 0; i < list.size(); ++i) {
                    list.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                }
            }
        }

        data.flushBits();

        if (getChangesMask().get(0)) {
            if (getChangesMask().get(3)) {
                if (!ignoreNestedChangesMask) {
                    actors.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(actors.size(), data);
                }
            }
        }

        data.flushBits();

        if (getChangesMask().get(0)) {
            if (getChangesMask().get(3)) {
                for (var i = 0; i < actors.size(); ++i) {
                    if (actors.hasChanged(i) || ignoreNestedChangesMask) {
                        actors.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (getChangesMask().get(4)) {
                data.writeInt32(getViewerLastLineEndTime(this, owner, receiver));
            }

            if (getChangesMask().get(5)) {
                data.writeInt32(progress);
            }
        }

        data.flushBits();
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(dontPlayBroadcastTextSounds);
        clearChangesMask(lines);
        clearChangesMask(actors);
        clearChangesMask(lastLineEndTime);
        clearChangesMask(progress);
        getChangesMask().resetAll();
    }


    public final int getViewerLastLineEndTime(ConversationData conversationLineData, Conversation conversation, Player receiver) {
        var locale = receiver.getSession().getSessionDbLocaleIndex();

        return (int) conversation.GetLastLineEndTime(locale).TotalMilliseconds;
    }
}
