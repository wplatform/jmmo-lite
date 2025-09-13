package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.mythicplus.DungeonScoreSummary;
import lombok.Getter;

import java.util.List;

@Getter
public final class PlayerData extends UpdateMaskObject {

    @ChangeMark(blockBit = 0, bit = 1)
    private final List<ChrCustomizationChoice> customizations = new Dynamic<>(0, 1, this);
    @ChangeMark(blockBit = 0, bit = 2)
    private final List<ArenaCooldown> arenaCooldowns = new Dynamic<>(0, 2, this);
    @ChangeMark(blockBit = 0, bit = 3)
    private final List<Integer> visualItemReplacements = new Dynamic<>(0, 3, this);
    @ChangeMark(blockBit = 0, bit = 4)
    private ObjectGuid duelArbiter;
    @ChangeMark(blockBit = 0, bit = 5)
    private ObjectGuid wowAccount;
    @ChangeMark(blockBit = 0, bit = 6)
    private ObjectGuid bnetAccount;
    @ChangeMark(blockBit = 0, bit = 7)
    private long guildClubMemberID;
    @ChangeMark(blockBit = 0, bit = 8)
    private ObjectGuid lootTargetGUID;
    @ChangeMark(blockBit = 0, bit = 9)
    private int playerFlags;
    @ChangeMark(blockBit = 0, bit = 10)
    private int playerFlagsEx;
    @ChangeMark(blockBit = 0, bit = 11)
    private int guildRankID;
    @ChangeMark(blockBit = 0, bit = 12)
    private int guildDeleteDate;
    @ChangeMark(blockBit = 0, bit = 13)
    private int guildLevel;
    @ChangeMark(blockBit = 0, bit = 14)
    private byte numBankSlots;
    @ChangeMark(blockBit = 0, bit = 15)
    private byte nativeSex;
    @ChangeMark(blockBit = 0, bit = 16)
    private byte inebriation;
    @ChangeMark(blockBit = 0, bit = 17)
    private byte pvpTitle;
    @ChangeMark(blockBit = 0, bit = 18)
    private byte arenaFaction;
    @ChangeMark(blockBit = 0, bit = 19)
    private byte pvpRank;
    @ChangeMark(blockBit = 0, bit = 20)
    private int field_88;
    @ChangeMark(blockBit = 0, bit = 21)
    private int duelTeam;
    @ChangeMark(blockBit = 0, bit = 22)
    private int guildTimeStamp;
    @ChangeMark(blockBit = 0, bit = 23)
    private int playerTitle;
    @ChangeMark(blockBit = 0, bit = 24)
    private int fakeInebriation;
    @ChangeMark(blockBit = 0, bit = 25)
    private int virtualPlayerRealm;
    @ChangeMark(blockBit = 0, bit = 26)
    private int currentSpecID;
    @ChangeMark(blockBit = 0, bit = 27)
    private int taxiMountAnimKitID;
    @ChangeMark(blockBit = 0, bit = 28)
    private byte currentBattlePetBreedQuality;
    @ChangeMark(blockBit = 0, bit = 29)
    private int honorLevel;
    @ChangeMark(blockBit = 0, bit = 30)
    private long logoutTime;
    @ChangeMark(blockBit = 0, bit = 31)
    private String name;
    @ChangeMark(blockBit = 32, bit = 33)
    private int field_13C;
    @ChangeMark(blockBit = 32, bit = 34)
    private int field_140;
    @ChangeMark(blockBit = 32, bit = 35)
    private int currentBattlePetSpeciesID;
    @ChangeMark(blockBit = 32, bit = 36)
    private DungeonScoreSummary dungeonScore;
    @ChangeMark(blockBit = 32, bit = 37)
    OptionalUpdateField<UF::DeclinedNames, 32, 37> DeclinedNames;
    FieldType<CustomTabardInfo, 32, 38> PersonalTabard;
    Array<Byte> PartyType = new Array<>(2, 39, 40, this);
    Array<QuestLog> QuestLog = new Array<>(25, 42, 43, this);
    Array<VisibleItem> VisibleItems = new Array<>(19, 68, 69, this);
    Array<Float> AvgItemLevel = new Array<>(6, 88, 89, this);
    Array<ZonePlayerForcedReaction> ForcedReactions = new Array<>(32, 95, 96, this);
    Array<Integer> Field_3120 = new Array<>(19, 128, 129, this);




    public FieldType<Boolean> hasQuestSession = new FieldType<>(0, 1);
    public FieldType<Boolean> hasLevelLink = new FieldType<>(0, 2);
    public Dynamic<ChrCustomizationChoice> customizations = new Dynamic<ChrCustomizationChoice>(0, 3);
    public Dynamic<questLog> questSessionQuestLog = new Dynamic<questLog>(0, 4);
    public Dynamic<ArenaCooldown> arenaCooldowns = new Dynamic<ArenaCooldown>(0, 5);
    public Dynamic<Integer> visualItemReplacements = new Dynamic<Integer>(0, 6);
    public FieldType<ObjectGuid> duelArbiter = new FieldType<>(0, 7);
    public FieldType<ObjectGuid> wowAccount = new FieldType<>(0, 8);
    public FieldType<ObjectGuid> lootTargetGUID = new FieldType<>(0, 9);
    public FieldType<Integer> playerFlags = new FieldType<>(0, 10);
    public FieldType<Integer> playerFlagsEx = new FieldType<>(0, 11);
    public FieldType<Integer> guildRankID = new FieldType<>(0, 12);
    public FieldType<Integer> guildDeleteDate = new FieldType<>(0, 13);
    public FieldType<Integer> guildLevel = new FieldType<>(0, 14);
    public FieldType<Byte> partyType = new FieldType<>(0, 15);
    public FieldType<Byte> nativeSex = new FieldType<>(0, 16);
    public FieldType<Byte> inebriation = new FieldType<>(0, 17);
    public FieldType<Byte> pvpTitle = new FieldType<>(0, 18);
    public FieldType<Byte> arenaFaction = new FieldType<>(0, 19);
    public FieldType<Integer> duelTeam = new FieldType<>(0, 20);
    public FieldType<Integer> guildTimeStamp = new FieldType<>(0, 21);
    public FieldType<Integer> playerTitle = new FieldType<>(0, 22);
    public FieldType<Integer> fakeInebriation = new FieldType<>(0, 23);
    public FieldType<Integer> virtualPlayerRealm = new FieldType<>(0, 24);
    public FieldType<Integer> currentSpecID = new FieldType<>(0, 25);
    public FieldType<Integer> taxiMountAnimKitID = new FieldType<>(0, 26);
    public FieldType<Byte> currentBattlePetBreedQuality = new FieldType<>(0, 27);
    public FieldType<Integer> honorLevel = new FieldType<>(0, 28);
    public FieldType<Long> logoutTime = new FieldType<>(0, 29);
    public FieldType<Integer> field_B0 = new FieldType<>(0, 30);
    public FieldType<Integer> field_B4 = new FieldType<>(0, 31);
    public FieldType<CTROptions> ctrOptions = new FieldType<>(32, 33);
    public FieldType<Integer> covenantID = new FieldType<>(32, 34);
    public FieldType<Integer> soulbindID = new FieldType<>(32, 35);
    public FieldType<dungeonScoreSummary> dungeonScore = new FieldType<>(32, 36);
    public Array<questLog> questLog = new Array<questLog>(125, 37, 38);
    public Array<VisibleItem> visibleItems = new Array<VisibleItem>(19, 163, 164);
    public Array<Float> avgItemLevel = new Array<Float>(6, 183, 184);


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
