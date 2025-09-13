package com.github.azeroth.game.entity.conversation;

import Framework.Constants.*;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class ConversationData extends BaseUpdateData<Conversation> {
    public UpdateField<Boolean> dontPlayBroadcastTextSounds = new UpdateField<>(0, 1);
    public UpdateField<ArrayList<ConversationLine>> lines = new UpdateField<ArrayList<ConversationLine>>(0, 2);
    public DynamicUpdateField<ConversationActorField> actors = new DynamicUpdateField<ConversationActorField>(0, 3);
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public UpdateField<uint> LastLineEndTime = new(0, 4);
    public UpdateField<Integer> lastLineEndTime = new UpdateField<>(0, 4);
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public UpdateField<uint> Progress = new(0, 5);
    public UpdateField<Integer> progress = new UpdateField<>(0, 5);
    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public UpdateField<uint> Flags = new(0, 6);
    public UpdateField<Integer> flags = new UpdateField<>(0, 6);

    public ConversationData() {
        super(0, TypeId.Conversation, 7);
    }

    public final void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Conversation owner, Player receiver) {
        data.WriteInt32(lines.getValue().size());
        data.WriteUInt32(getViewerLastLineEndTime(this, owner, receiver));
        data.WriteUInt32(progress);

        for (var i = 0; i < lines.getValue().size(); ++i) {
            lines.getValue().get(i).WriteCreate(data, owner, receiver);
        }

        data.WriteBit(dontPlayBroadcastTextSounds);
        data.WriteInt32(actors.size());

        for (var i = 0; i < actors.size(); ++i) {
            actors.get(i).writeCreate(data, owner, receiver);
        }

        data.FlushBits();
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Conversation owner, Player receiver) {
        writeUpdate(data, changesMask, false, owner, receiver);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, Conversation owner, Player receiver) {
        data.WriteBits(this.changesMask.getBlock(0), 6);

        if (this.changesMask.get(0)) {
            if (this.changesMask.get(1)) {
                data.WriteBit(dontPlayBroadcastTextSounds);
            }

            if (changesMask.get(2)) {
                ArrayList<ConversationLine> list = lines;
                data.WriteBits(list.size(), 32);

                for (var i = 0; i < list.size(); ++i) {
                    list.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                }
            }
        }

        data.FlushBits();

        if (this.changesMask.get(0)) {
            if (this.changesMask.get(3)) {
                if (!ignoreNestedChangesMask) {
                    actors.writeUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(actors.size(), data);
                }
            }
        }

        data.FlushBits();

        if (this.changesMask.get(0)) {
            if (this.changesMask.get(3)) {
                for (var i = 0; i < actors.size(); ++i) {
                    if (actors.hasChanged(i) || ignoreNestedChangesMask) {
                        actors.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (this.changesMask.get(4)) {
                data.WriteUInt32(getViewerLastLineEndTime(this, owner, receiver));
            }

            if (this.changesMask.get(5)) {
                data.WriteUInt32(progress);
            }
        }

        data.FlushBits();
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(dontPlayBroadcastTextSounds);
        clearChangesMask(lines);
        clearChangesMask(actors);
        clearChangesMask(lastLineEndTime);
        clearChangesMask(progress);
        changesMask.resetAll();
    }

    //C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public uint GetViewerLastLineEndTime(ConversationData conversationLineData, Conversation conversation, Player receiver)
    public final int getViewerLastLineEndTime(ConversationData conversationLineData, Conversation conversation, Player receiver) {
        var locale = receiver.getSession().getSessionDbLocaleIndex();

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: return (uint)conversation.GetLastLineEndTime(locale).TotalMilliseconds;
        return (int) conversation.getLastLineEndTime(locale).getTotalMilliseconds();
    }
}