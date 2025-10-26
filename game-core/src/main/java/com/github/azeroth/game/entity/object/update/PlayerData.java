package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.mythicplus.DungeonScoreSummary;
import lombok.Getter;

import java.util.List;

@Getter
public final class PlayerData extends UpdateMaskObject {

    @ChangeMark(blockBit = 0, bit = 1, type = FieldType.DYNAMIC)
    List<ChrCustomizationChoice> customizations;
    @ChangeMark(blockBit = 0, bit = 2, type = FieldType.DYNAMIC)
    List<ArenaCooldown> arenaCooldowns;
    @ChangeMark(blockBit = 0, bit = 3, type = FieldType.DYNAMIC)
    List<Integer> visualItemReplacements;
    @ChangeMark(blockBit = 0, bit = 4)
    ObjectGuid duelArbiter;
    @ChangeMark(blockBit = 0, bit = 5)
    ObjectGuid wowAccount;
    @ChangeMark(blockBit = 0, bit = 6)
    ObjectGuid bnetAccount;
    @ChangeMark(blockBit = 0, bit = 7)
    long guildClubMemberID;
    @ChangeMark(blockBit = 0, bit = 8)
    ObjectGuid lootTargetGUID;
    @ChangeMark(blockBit = 0, bit = 9)
    int playerFlags;
    @ChangeMark(blockBit = 0, bit = 10)
    int playerFlagsEx;
    @ChangeMark(blockBit = 0, bit = 11)
    int guildRankID;
    @ChangeMark(blockBit = 0, bit = 12)
    int guildDeleteDate;
    @ChangeMark(blockBit = 0, bit = 13)
    int guildLevel;
    @ChangeMark(blockBit = 0, bit = 14)
    int numBankSlots;
    @ChangeMark(blockBit = 0, bit = 15)
    int nativeSex;
    @ChangeMark(blockBit = 0, bit = 16)
    int inebriation;
    @ChangeMark(blockBit = 0, bit = 17)
    int pvpTitle;
    @ChangeMark(blockBit = 0, bit = 18)
    byte arenaFaction;
    @ChangeMark(blockBit = 0, bit = 19)
    byte pvpRank;
    @ChangeMark(blockBit = 0, bit = 20)
    int field_88;
    @ChangeMark(blockBit = 0, bit = 21)
    int duelTeam;
    @ChangeMark(blockBit = 0, bit = 22)
    int guildTimeStamp;
    @ChangeMark(blockBit = 0, bit = 23)
    int playerTitle;
    @ChangeMark(blockBit = 0, bit = 24)
    int fakeInebriation;
    @ChangeMark(blockBit = 0, bit = 25)
    int virtualPlayerRealm;
    @ChangeMark(blockBit = 0, bit = 26)
    int currentSpecID;
    @ChangeMark(blockBit = 0, bit = 27)
    int taxiMountAnimKitID;
    @ChangeMark(blockBit = 0, bit = 28)
    byte currentBattlePetBreedQuality;
    @ChangeMark(blockBit = 0, bit = 29)
    int honorLevel;
    @ChangeMark(blockBit = 0, bit = 30)
    long logoutTime;
    @ChangeMark(blockBit = 0, bit = 31)
    String name;
    @ChangeMark(blockBit = 32, bit = 33)
    int field_13C;
    @ChangeMark(blockBit = 32, bit = 34)
    int field_140;
    @ChangeMark(blockBit = 32, bit = 35)
    int currentBattlePetSpeciesID;
    @ChangeMark(blockBit = 32, bit = 36)
    DungeonScoreSummary dungeonScore;
    @ChangeMark(blockBit = 32, bit = 37, type = FieldType.OPTIONAL)
    DeclinedNames declinedNames;
    @ChangeMark(blockBit = 32, bit = 38)
    CustomTabardInfo personalTabard;
    @ChangeMark(size = 2, bit = 39, firstElementBit = 40, type = FieldType.ARRAY)
    List<Byte> partyType;
    @ChangeMark(size = 25, bit = 39, firstElementBit = 40, type = FieldType.ARRAY)
    List<QuestLog> questLog;
    @ChangeMark(size = 19, bit = 68, firstElementBit = 69, type = FieldType.ARRAY)
    List<VisibleItem> visibleItems;
    @ChangeMark(size = 6, bit = 88, firstElementBit = 89, type = FieldType.ARRAY)
    List<Float> avgItemLevel;
    @ChangeMark(size = 32, bit = 95, firstElementBit = 96, type = FieldType.ARRAY)
    List<ZonePlayerForcedReaction> forcedReactions;
    @ChangeMark(size = 19, bit = 128, firstElementBit = 129, type = FieldType.ARRAY)
    List<Integer> field_3120;


    public PlayerData() {
        super(148);
    }

    public final void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Player owner, Player receiver) {
        data.writeGuid(duelArbiter);
        data.writeGuid(wowAccount);
        data.writeGuid(lootTargetGUID);
        data.writeInt32(playerFlags);
        data.writeInt32(playerFlagsEx);
        data.writeInt32(guildRankID);
        data.writeInt32(guildDeleteDate);
        data.writeInt32(guildLevel);
        data.writeInt32(customizations.size());
        data.writeInt8(partyType);
        data.writeInt8(nativeSex);
        data.writeInt8(inebriation);
        data.writeInt8(pvpTitle);
        data.writeInt8(arenaFaction);
        data.writeInt32(duelTeam);
        data.writeInt32(guildTimeStamp);

        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.PartyMember)) {
            for (var i = 0; i < 125; ++i) {
                questLog.get(i).writeCreate(data, owner, receiver);
            }

            data.writeInt32(questSessionQuestLog.size());
        }

        for (var i = 0; i < 19; ++i) {
            visibleItems.get(i).writeCreate(data, owner, receiver);
        }

        data.writeInt32(playerTitle);
        data.writeInt32(fakeInebriation);
        data.writeInt32(virtualPlayerRealm);
        data.writeInt32(currentSpecID);
        data.writeInt32(taxiMountAnimKitID);

        for (var i = 0; i < 6; ++i) {
            data.writeFloat(avgItemLevel.get(i));
        }

        data.writeInt8(currentBattlePetBreedQuality);
        data.writeInt32(honorLevel);
        data.writeInt64(logoutTime);
        data.writeInt32(arenaCooldowns.size());
        data.writeInt32(field_B0);
        data.writeInt32(field_B4);
        ((CTROptions) ctrOptions).writeCreate(data, owner, receiver);
        data.writeInt32(covenantID);
        data.writeInt32(soulbindID);
        data.writeInt32(visualItemReplacements.size());

        for (var i = 0; i < customizations.size(); ++i) {
            customizations.get(i).writeCreate(data, owner, receiver);
        }

        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.PartyMember)) {
            for (var i = 0; i < questSessionQuestLog.size(); ++i) {
                questSessionQuestLog.get(i).writeCreate(data, owner, receiver);
            }
        }

        for (var i = 0; i < arenaCooldowns.size(); ++i) {
            arenaCooldowns.get(i).writeCreate(data, owner, receiver);
        }

        for (var i = 0; i < visualItemReplacements.size(); ++i) {
            data.writeInt32(visualItemReplacements.get(i));
        }

        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.PartyMember)) {
            data.writeBit(hasQuestSession);
        }

        data.writeBit(hasLevelLink);
        dungeonScore.get().write(data);
        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Player owner, Player receiver) {
        UpdateMask allowedMaskForTarget = new UpdateMask(188, new Object[]{0xFFFFFFED, 0x0000001F, 0x00000000, 0x00000000, 0x00000000, 0x3FFFFFF8});

        appendAllowedFieldsMaskForFlag(allowedMaskForTarget, fieldVisibilityFlags);
        writeUpdate(data, UpdateMask.opBitwiseAnd(getChangesMask(), allowedMaskForTarget), false, owner, receiver);
    }

    public final void appendAllowedFieldsMaskForFlag(UpdateMask allowedMaskForTarget, UpdateFieldFlag fieldVisibilityFlags) {
        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.PartyMember)) {
            allowedMaskForTarget.OR(new UpdateMask(188, new int[]{0x00000012, 0xFFFFFFE0, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0x00000007}));
        }
    }

    public final void filterDisallowedFieldsMaskForFlag(UpdateMask changesMask, UpdateFieldFlag fieldVisibilityFlags) {
        UpdateMask allowedMaskForTarget = new UpdateMask(188, new Object[]{0xFFFFFFED, 0x0000001F, 0x00000000, 0x00000000, 0x00000000, 0x3FFFFFF8});

        appendAllowedFieldsMaskForFlag(allowedMaskForTarget, fieldVisibilityFlags);
        changesMask.AND(allowedMaskForTarget);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, Player owner, Player receiver) {
        data.writeBits(changesMask.getBlocksMask(0), 6);

        for (int i = 0; i < 6; ++i) {
            if (changesMask.getBlock(i) != 0) {
                data.writeBits(changesMask.getBlock(i), 32);
            }
        }

        var noQuestLogChangesMask = data.writeBit(isQuestLogChangesMaskSkipped());

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeBit(hasQuestSession);
            }

            if (changesMask.get(2)) {
                data.writeBit(hasLevelLink);
            }

            if (changesMask.get(3)) {
                if (!ignoreNestedChangesMask) {
                    customizations.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(customizations.size(), data);
                }
            }

            if (changesMask.get(4)) {
                if (!ignoreNestedChangesMask) {
                    questSessionQuestLog.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(questSessionQuestLog.size(), data);
                }
            }

            if (changesMask.get(5)) {
                if (!ignoreNestedChangesMask) {
                    arenaCooldowns.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(arenaCooldowns.size(), data);
                }
            }

            if (changesMask.get(6)) {
                if (!ignoreNestedChangesMask) {
                    visualItemReplacements.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(visualItemReplacements.size(), data);
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(3)) {
                for (var i = 0; i < customizations.size(); ++i) {
                    if (customizations.hasChanged(i) || ignoreNestedChangesMask) {
                        customizations.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(4)) {
                for (var i = 0; i < questSessionQuestLog.size(); ++i) {
                    if (questSessionQuestLog.hasChanged(i) || ignoreNestedChangesMask) {
                        if (noQuestLogChangesMask) {
                            questSessionQuestLog.get(i).writeCreate(data, owner, receiver);
                        } else {
                            questSessionQuestLog.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                        }
                    }
                }
            }

            if (changesMask.get(5)) {
                for (var i = 0; i < arenaCooldowns.size(); ++i) {
                    if (arenaCooldowns.hasChanged(i) || ignoreNestedChangesMask) {
                        arenaCooldowns.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(6)) {
                for (var i = 0; i < visualItemReplacements.size(); ++i) {
                    if (visualItemReplacements.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(visualItemReplacements.get(i));
                    }
                }
            }

            if (changesMask.get(7)) {
                data.writeGuid(duelArbiter);
            }

            if (changesMask.get(8)) {
                data.writeGuid(wowAccount);
            }

            if (changesMask.get(9)) {
                data.writeGuid(lootTargetGUID);
            }

            if (changesMask.get(10)) {
                data.writeInt32(playerFlags);
            }

            if (changesMask.get(11)) {
                data.writeInt32(playerFlagsEx);
            }

            if (changesMask.get(12)) {
                data.writeInt32(guildRankID);
            }

            if (changesMask.get(13)) {
                data.writeInt32(guildDeleteDate);
            }

            if (changesMask.get(14)) {
                data.writeInt32(guildLevel);
            }

            if (changesMask.get(15)) {
                data.writeInt8(partyType);
            }

            if (changesMask.get(16)) {
                data.writeInt8(nativeSex);
            }

            if (changesMask.get(17)) {
                data.writeInt8(inebriation);
            }

            if (changesMask.get(18)) {
                data.writeInt8(pvpTitle);
            }

            if (changesMask.get(19)) {
                data.writeInt8(arenaFaction);
            }

            if (changesMask.get(20)) {
                data.writeInt32(duelTeam);
            }

            if (changesMask.get(21)) {
                data.writeInt32(guildTimeStamp);
            }

            if (changesMask.get(22)) {
                data.writeInt32(playerTitle);
            }

            if (changesMask.get(23)) {
                data.writeInt32(fakeInebriation);
            }

            if (changesMask.get(24)) {
                data.writeInt32(virtualPlayerRealm);
            }

            if (changesMask.get(25)) {
                data.writeInt32(currentSpecID);
            }

            if (changesMask.get(26)) {
                data.writeInt32(taxiMountAnimKitID);
            }

            if (changesMask.get(27)) {
                data.writeInt8(currentBattlePetBreedQuality);
            }

            if (changesMask.get(28)) {
                data.writeInt32(honorLevel);
            }

            if (changesMask.get(29)) {
                data.writeInt64(logoutTime);
            }

            if (changesMask.get(30)) {
                data.writeInt32(field_B0);
            }

            if (changesMask.get(31)) {
                data.writeInt32(field_B4);
            }
        }

        if (changesMask.get(32)) {
            if (changesMask.get(33)) {
                ctrOptions.get().writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
            }

            if (changesMask.get(34)) {
                data.writeInt32(covenantID);
            }

            if (changesMask.get(35)) {
                data.writeInt32(soulbindID);
            }

            if (changesMask.get(36)) {
                dungeonScore.get().write(data);
            }
        }

        if (changesMask.get(37)) {
            for (var i = 0; i < 125; ++i) {
                if (changesMask.get(38 + i)) {
                    if (noQuestLogChangesMask) {
                        questLog.get(i).writeCreate(data, owner, receiver);
                    } else {
                        questLog.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }
        }

        if (changesMask.get(163)) {
            for (var i = 0; i < 19; ++i) {
                if (changesMask.get(164 + i)) {
                    visibleItems.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                }
            }
        }

        if (changesMask.get(183)) {
            for (var i = 0; i < 6; ++i) {
                if (changesMask.get(184 + i)) {
                    data.writeFloat(avgItemLevel.get(i));
                }
            }
        }

        data.flushBits();
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(hasQuestSession);
        clearChangesMask(hasLevelLink);
        clearChangesMask(customizations);
        clearChangesMask(questSessionQuestLog);
        clearChangesMask(arenaCooldowns);
        clearChangesMask(visualItemReplacements);
        clearChangesMask(duelArbiter);
        clearChangesMask(wowAccount);
        clearChangesMask(lootTargetGUID);
        clearChangesMask(playerFlags);
        clearChangesMask(playerFlagsEx);
        clearChangesMask(guildRankID);
        clearChangesMask(guildDeleteDate);
        clearChangesMask(guildLevel);
        clearChangesMask(partyType);
        clearChangesMask(nativeSex);
        clearChangesMask(inebriation);
        clearChangesMask(pvpTitle);
        clearChangesMask(arenaFaction);
        clearChangesMask(duelTeam);
        clearChangesMask(guildTimeStamp);
        clearChangesMask(playerTitle);
        clearChangesMask(fakeInebriation);
        clearChangesMask(virtualPlayerRealm);
        clearChangesMask(currentSpecID);
        clearChangesMask(taxiMountAnimKitID);
        clearChangesMask(currentBattlePetBreedQuality);
        clearChangesMask(honorLevel);
        clearChangesMask(logoutTime);
        clearChangesMask(field_B0);
        clearChangesMask(field_B4);
        clearChangesMask(ctrOptions);
        clearChangesMask(covenantID);
        clearChangesMask(soulbindID);
        clearChangesMask(dungeonScore);
        clearChangesMask(questLog);
        clearChangesMask(visibleItems);
        clearChangesMask(avgItemLevel);
        getChangesMask().resetAll();
    }

    private boolean isQuestLogChangesMaskSkipped() {
        return false;
    } // bandwidth savings aren't worth the cpu time
}
