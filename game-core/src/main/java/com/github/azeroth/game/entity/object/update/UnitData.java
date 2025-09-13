package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.domain.creature.CreatureTemplate;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.creature.TempSummon;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.networking.WorldPacket;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class UnitData extends UpdateMaskObject {
    @ChangeMark(blockBit = 0, bit = 1)
    private final List<Integer> stateWorldEffectIds;
    @ChangeMark(blockBit = 0, bit = 2, type = FieldType.DYNAMIC)
    private PassiveSpellHistory passiveSpells;
    @ChangeMark(blockBit = 0, bit = 3, type = FieldType.DYNAMIC)
    private int worldEffects;
    @ChangeMark(blockBit = 0, bit = 4, type = FieldType.DYNAMIC)
    private ObjectGuid channelObjects;
    @ChangeMark(blockBit = 0, bit = 5)
    private long health;
    @ChangeMark(blockBit = 0, bit = 6)
    private long maxHealth;
    @ChangeMark(blockBit = 0, bit = 7)
    private int displayId;
    @ChangeMark(blockBit = 0, bit = 8)
    private int npcFlags;
    @ChangeMark(blockBit = 0, bit = 9)
    private int npcFlags2;
    @ChangeMark(blockBit = 0, bit = 10)
    private int stateSpellVisualId;
    @ChangeMark(blockBit = 0, bit = 11)
    private int stateAnimId;
    @ChangeMark(blockBit = 0, bit = 12)
    private int stateAnimKitId;
    @ChangeMark(blockBit = 0, bit = 13)
    private ObjectGuid charm;
    @ChangeMark(blockBit = 0, bit = 14)
    private ObjectGuid summon;
    @ChangeMark(blockBit = 0, bit = 15)
    private ObjectGuid critter;
    @ChangeMark(blockBit = 0, bit = 16)
    private ObjectGuid charmedBy;
    @ChangeMark(blockBit = 0, bit = 17)
    private ObjectGuid summonedBy;
    @ChangeMark(blockBit = 0, bit = 18)
    private ObjectGuid createdBy;
    @ChangeMark(blockBit = 0, bit = 19)
    private ObjectGuid demonCreator;
    @ChangeMark(blockBit = 0, bit = 20)
    private ObjectGuid lookAtControllerTarget;
    @ChangeMark(blockBit = 0, bit = 21)
    private ObjectGuid target;
    @ChangeMark(blockBit = 0, bit = 22)
    private ObjectGuid battlePetCompanionGUID;
    @ChangeMark(blockBit = 0, bit = 23)
    private long battlePetDBID;
    @ChangeMark(blockBit = 0, bit = 24)
    private UnitChannel channelData;
    @ChangeMark(blockBit = 0, bit = 25)
    private int summonedByHomeRealm;
    @ChangeMark(blockBit = 0, bit = 26)
    private byte race;
    @ChangeMark(blockBit = 0, bit = 27)
    private byte classId;
    @ChangeMark(blockBit = 0, bit = 28)
    private byte playerClassId;
    @ChangeMark(blockBit = 0, bit = 29)
    private byte sex;
    @ChangeMark(blockBit = 0, bit = 30)
    private byte displayPower;
    @ChangeMark(blockBit = 0, bit = 31)
    private int overrideDisplayPowerID;
    @ChangeMark(blockBit = 32, bit = 33)
    private int level;
    @ChangeMark(blockBit = 32, bit = 34)
    private int effectiveLevel;
    @ChangeMark(blockBit = 32, bit = 35)
    private int contentTuningID;
    @ChangeMark(blockBit = 32, bit = 36)
    private int scalingLevelMin;
    @ChangeMark(blockBit = 32, bit = 37)
    private int scalingLevelMax;
    @ChangeMark(blockBit = 32, bit = 38)
    private int scalingLevelDelta;
    @ChangeMark(blockBit = 32, bit = 39)
    private int scalingFactionGroup;
    @ChangeMark(blockBit = 32, bit = 40)
    private int factionTemplate;
    @ChangeMark(blockBit = 32, bit = 41)
    private int flags;
    @ChangeMark(blockBit = 32, bit = 42)
    private int flags2;
    @ChangeMark(blockBit = 32, bit = 43)
    private int flags3;
    @ChangeMark(blockBit = 32, bit = 44)
    private int flags4;
    @ChangeMark(blockBit = 32, bit = 45)
    private int auraState;
    @ChangeMark(blockBit = 32, bit = 46)
    private int rangedAttackRoundBaseTime;
    @ChangeMark(blockBit = 32, bit = 47)
    private float boundingRadius;
    @ChangeMark(blockBit = 32, bit = 48)
    private float combatReach;
    @ChangeMark(blockBit = 32, bit = 49)
    private float displayScale;
    @ChangeMark(blockBit = 32, bit = 50)
    private int nativeDisplayId;
    @ChangeMark(blockBit = 32, bit = 51)
    private float nativeXDisplayScale;
    @ChangeMark(blockBit = 32, bit = 52)
    private int mountDisplayId;
    @ChangeMark(blockBit = 32, bit = 53)
    private float minDamage;
    @ChangeMark(blockBit = 32, bit = 54)
    private float maxDamage;
    @ChangeMark(blockBit = 32, bit = 55)
    private float minOffHandDamage;
    @ChangeMark(blockBit = 32, bit = 56)
    private float maxOffHandDamage;
    @ChangeMark(blockBit = 32, bit = 57)
    private byte standState;
    @ChangeMark(blockBit = 32, bit = 58)
    private byte petTalentPoints;
    @ChangeMark(blockBit = 32, bit = 59)
    private byte visFlags;
    @ChangeMark(blockBit = 32, bit = 60)
    private byte animTier;
    @ChangeMark(blockBit = 32, bit = 61)
    private int petNumber;
    @ChangeMark(blockBit = 32, bit = 62)
    private int petNameTimestamp;
    @ChangeMark(blockBit = 32, bit = 63)
    private int petExperience;
    @ChangeMark(blockBit = 64, bit = 65)
    private int petNextLevelExperience;
    @ChangeMark(blockBit = 64, bit = 66)
    private float modCastingSpeed;
    @ChangeMark(blockBit = 64, bit = 67)
    private float modSpellHaste;
    @ChangeMark(blockBit = 64, bit = 68)
    private float modHaste;
    @ChangeMark(blockBit = 64, bit = 69)
    private float modRangedHaste;
    @ChangeMark(blockBit = 64, bit = 70)
    private float modHasteRegen;
    @ChangeMark(blockBit = 64, bit = 71)
    private float modTimeRate;
    @ChangeMark(blockBit = 64, bit = 72)
    private int createdBySpell;
    @ChangeMark(blockBit = 64, bit = 73)
    private int emoteState;
    @ChangeMark(blockBit = 64, bit = 74)
    private int trainingPointsUsed;
    @ChangeMark(blockBit = 64, bit = 75)
    private int trainingPointsTotal;
    @ChangeMark(blockBit = 64, bit = 76)
    private int baseMana;
    @ChangeMark(blockBit = 64, bit = 77)
    private int baseHealth;
    @ChangeMark(blockBit = 64, bit = 78)
    private byte sheatheState;
    @ChangeMark(blockBit = 64, bit = 79)
    private byte pvpFlags;
    @ChangeMark(blockBit = 64, bit = 80)
    private byte petFlags;
    @ChangeMark(blockBit = 64, bit = 81)
    private byte shapeshiftForm;
    @ChangeMark(blockBit = 64, bit = 82)
    private int attackPower;
    @ChangeMark(blockBit = 64, bit = 83)
    private int attackPowerModPos;
    @ChangeMark(blockBit = 64, bit = 84)
    private int attackPowerModNeg;
    @ChangeMark(blockBit = 64, bit = 85)
    private float attackPowerMultiplier;
    @ChangeMark(blockBit = 64, bit = 86)
    private int rangedAttackPower;
    @ChangeMark(blockBit = 64, bit = 87)
    private int rangedAttackPowerModPos;
    @ChangeMark(blockBit = 64, bit = 88)
    private int rangedAttackPowerModNeg;
    @ChangeMark(blockBit = 64, bit = 89)
    private float rangedAttackPowerMultiplier;
    @ChangeMark(blockBit = 64, bit = 90)
    private int setAttackSpeedAura;
    @ChangeMark(blockBit = 64, bit = 91)
    private float lifeSteal;
    @ChangeMark(blockBit = 64, bit = 92)
    private float minRangedDamage;
    @ChangeMark(blockBit = 64, bit = 93)
    private float maxRangedDamage;
    @ChangeMark(blockBit = 64, bit = 94)
    private float maxHealthModifier;
    @ChangeMark(blockBit = 64, bit = 95)
    private float hoverHeight;
    @ChangeMark(blockBit = 96, bit = 97)
    private int minItemLevelCutoff;
    @ChangeMark(blockBit = 96, bit = 98)
    private int minItemLevel;
    @ChangeMark(blockBit = 96, bit = 99)
    private int maxItemLevel;
    @ChangeMark(blockBit = 96, bit = 100)
    private int wildBattlePetLevel;
    @ChangeMark(blockBit = 96, bit = 101)
    private int battlePetCompanionNameTimestamp;
    @ChangeMark(blockBit = 96, bit = 102)
    private int interactSpellID;
    @ChangeMark(blockBit = 96, bit = 103)
    private int scaleDuration;
    @ChangeMark(blockBit = 96, bit = 104)
    private int looksLikeMountID;
    @ChangeMark(blockBit = 96, bit = 105)
    private int looksLikeCreatureID;
    @ChangeMark(blockBit = 96, bit = 106)
    private int lookAtControllerID;
    @ChangeMark(blockBit = 96, bit = 107)
    private int perksVendorItemID;
    @ChangeMark(blockBit = 96, bit = 108)
    private ObjectGuid guildGUID;
    @ChangeMark(blockBit = 96, bit = 109)
    private ObjectGuid skinningOwnerGUID;
    @ChangeMark(blockBit = 96, bit = 110)
    private int flightCapabilityID;
    @ChangeMark(blockBit = 96, bit = 111)
    private float glideEventSpeedDivisor;                         // Movement speed gets divided by this value when evaluating what GlideEvents to use
    @ChangeMark(blockBit = 96, bit = 112)
    private int silencedSchoolMask;
    @ChangeMark(blockBit = 96, bit = 113)
    private int currentAreaID;
    @ChangeMark(blockBit = 96, bit = 114)
    private ObjectGuid comboTarget;
    @ChangeMark(blockBit = 96, bit = 115)
    private float field_2F0;
    @ChangeMark(blockBit = 96, bit = 116)
    private float field_2F4;
    @ChangeMark(size = 10, bit = 117, firstElementBit = 118, type = FieldType.ARRAY)
    private final List<Float> powerRegenFlatModifier;
    @ChangeMark(size = 10, bit = 117, firstElementBit = 128, type = FieldType.ARRAY)
    private final List<Float> powerRegenInterruptedFlatModifier;
    @ChangeMark(size = 10, bit = 117, firstElementBit = 138, type = FieldType.ARRAY)
    private final List<Integer> power;
    @ChangeMark(size = 10, bit = 117, firstElementBit = 148, type = FieldType.ARRAY)
    private final List<Integer> maxPower;
    @ChangeMark(size = 10, bit = 117, firstElementBit = 158, type = FieldType.ARRAY)
    private final List<Float> modPowerRegen;                        // Applies to power regen only if expansion < 2, hidden from lua
    @ChangeMark(size = 3, bit = 168, firstElementBit = 169, type = FieldType.ARRAY)
    private final List<VisibleItem> virtualItems;
    @ChangeMark(size = 3, bit = 172, firstElementBit = 173, type = FieldType.ARRAY)
    private final List<Integer> attackRoundBaseTime;
    @ChangeMark(size = 5, bit = 176, firstElementBit = 177, type = FieldType.ARRAY)
    private final List<Integer> stats;
    @ChangeMark(size = 5, bit = 176, firstElementBit = 182, type = FieldType.ARRAY)
    private final List<Integer> statPosBuff;
    @ChangeMark(size = 5, bit = 176, firstElementBit = 187, type = FieldType.ARRAY)
    private final List<Integer> statNegBuff;
    @ChangeMark(size = 7, bit = 192, firstElementBit = 193, type = FieldType.ARRAY)
    private final List<Integer> resistances;
    @ChangeMark(size = 7, bit = 192, firstElementBit = 200, type = FieldType.ARRAY)
    private final List<Integer> resistanceBuffModsPositive;
    @ChangeMark(size = 7, bit = 192, firstElementBit = 207, type = FieldType.ARRAY)
    private final List<Integer> resistanceBuffModsNegative;
    @ChangeMark(size = 7, bit = 192, firstElementBit = 214, type = FieldType.ARRAY)
    private final List<Integer> powerCostModifier;
    @ChangeMark(size = 7, bit = 192, firstElementBit = 221, type = FieldType.ARRAY)
    private final List<Float> powerCostMultiplier;

    public UnitData() {
        super(228);
        this.stateWorldEffectIds = newList("stateWorldEffectIds");
        this.powerRegenFlatModifier = newList("powerRegenFlatModifier");
        this.powerRegenInterruptedFlatModifier = newList("powerRegenInterruptedFlatModifier");
        this.power = newList("power");
        this.maxPower = newList("maxPower");
        this.modPowerRegen = newList("modPowerRegen");
        this.virtualItems = newList("virtualItems");
        this.attackRoundBaseTime = newList("attackRoundBaseTime");
        this.stats = newList("stats");
        this.statPosBuff = newList("statPosBuff");
        this.statNegBuff = newList("statNegBuff");
        this.resistances = newList("resistances");
        this.resistanceBuffModsPositive = newList("resistanceBuffModsPositive");
        this.resistanceBuffModsNegative = newList("resistanceBuffModsNegative");
        this.powerCostModifier = newList("powerCostModifier");
        this.powerCostMultiplier = newList("powerCostMultiplier");
    }

    public void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Unit owner, Player receiver) {
        data.writeInt64(health);
        data.writeInt64(maxHealth);
        data.writeInt32(ViewerDependentValue<DisplayIDTag>::GetValue(this, owner, receiver));
        data.writeInt32(ViewerDependentValue<NpcFlagsTag>::GetValue(this, owner, receiver));
        data.writeInt32(ViewerDependentValue<NpcFlags2Tag>::GetValue(this, owner, receiver));
        data.writeInt32(ViewerDependentValue<StateSpellVisualIDTag>::GetValue(this, owner, receiver));
        data.writeInt32(ViewerDependentValue<StateAnimIDTag>::GetValue(this, owner, receiver));
        data.writeInt32(ViewerDependentValue<StateAnimKitIDTag>::GetValue(this, owner, receiver));
        stateWorldEffectIDs = ViewerDependentValue<StateWorldEffectIDsTag>::GetValue(this, owner, receiver);
        data.writeInt32(stateWorldEffectIds.size());
        for (uint32 i = 0; i < stateWorldEffectIDs->size(); ++i)
        {
            data << uint32((*stateWorldEffectIDs)[i]);
        }
        data << Charm;
        data << Summon;
        if (fieldVisibilityFlags.HasFlag(UpdateFieldFlag::Owner))
        {
            data << Critter;
        }
        data << CharmedBy;
        data << SummonedBy;
        data << CreatedBy;
        data << DemonCreator;
        data << LookAtControllerTarget;
        data << Target;
        data << BattlePetCompanionGUID;
        data << uint64(BattlePetDBID);
        ChannelData->WriteCreate(data, owner, receiver);
        data << uint32(SummonedByHomeRealm);
        data << uint8(Race);
        data << uint8(ClassId);
        data << uint8(PlayerClassId);
        data << uint8(Sex);
        data << uint8(DisplayPower);
        data << uint32(OverrideDisplayPowerID);
        if (fieldVisibilityFlags.HasFlag(UpdateFieldFlag::Owner | UpdateFieldFlag::UnitAll))
        {
            for (uint32 i = 0; i < 10; ++i)
            {
                data << float(PowerRegenFlatModifier[i]);
                data << float(PowerRegenInterruptedFlatModifier[i]);
            }
        }
        for (uint32 i = 0; i < 10; ++i)
        {
            data << int32(Power[i]);
            data << int32(MaxPower[i]);
            data << float(ModPowerRegen[i]);
        }
        data << int32(Level);
        data << int32(EffectiveLevel);
        data << int32(ContentTuningID);
        data << int32(ScalingLevelMin);
        data << int32(ScalingLevelMax);
        data << int32(ScalingLevelDelta);
        data << int32(ScalingFactionGroup);
        data << int32(ViewerDependentValue<FactionTemplateTag>::GetValue(this, owner, receiver));
        for (uint32 i = 0; i < 3; ++i)
        {
            VirtualItems[i].WriteCreate(data, owner, receiver);
        }
        data << uint32(ViewerDependentValue<FlagsTag>::GetValue(this, owner, receiver));
        data << uint32(ViewerDependentValue<Flags2Tag>::GetValue(this, owner, receiver));
        data << uint32(ViewerDependentValue<Flags3Tag>::GetValue(this, owner, receiver));
        data << uint32(ViewerDependentValue<Flags4Tag>::GetValue(this, owner, receiver));
        data << uint32(ViewerDependentValue<AuraStateTag>::GetValue(this, owner, receiver));
        for (uint32 i = 0; i < 3; ++i)
        {
            data << uint32(AttackRoundBaseTime[i]);
        }
        if (fieldVisibilityFlags.HasFlag(UpdateFieldFlag::Owner))
        {
            data << uint32(RangedAttackRoundBaseTime);
        }
        data << float(BoundingRadius);
        data << float(CombatReach);
        data << float(DisplayScale);
        data << int32(NativeDisplayID);
        data << float(NativeXDisplayScale);
        data << int32(MountDisplayID);
        if (fieldVisibilityFlags.HasFlag(UpdateFieldFlag::Owner | UpdateFieldFlag::Empath))
        {
            data << float(MinDamage);
            data << float(MaxDamage);
            data << float(MinOffHandDamage);
            data << float(MaxOffHandDamage);
        }
        data << uint8(StandState);
        data << uint8(PetTalentPoints);
        data << uint8(VisFlags);
        data << uint8(AnimTier);
        data << uint32(PetNumber);
        data << uint32(PetNameTimestamp);
        data << uint32(PetExperience);
        data << uint32(PetNextLevelExperience);
        data << float(ModCastingSpeed);
        data << float(ModSpellHaste);
        data << float(ModHaste);
        data << float(ModRangedHaste);
        data << float(ModHasteRegen);
        data << float(ModTimeRate);
        data << int32(CreatedBySpell);
        data << int32(EmoteState);
        data << int16(TrainingPointsUsed);
        data << int16(TrainingPointsTotal);
        if (fieldVisibilityFlags.HasFlag(UpdateFieldFlag::Owner))
        {
            for (uint32 i = 0; i < 5; ++i)
            {
                data << int32(Stats[i]);
                data << int32(StatPosBuff[i]);
                data << int32(StatNegBuff[i]);
            }
        }
        if (fieldVisibilityFlags.HasFlag(UpdateFieldFlag::Owner | UpdateFieldFlag::Empath))
        {
            for (uint32 i = 0; i < 7; ++i)
            {
                data << int32(Resistances[i]);
            }
        }
        for (uint32 i = 0; i < 7; ++i)
        {
            data << int32(ResistanceBuffModsPositive[i]);
            data << int32(ResistanceBuffModsNegative[i]);
        }
        if (fieldVisibilityFlags.HasFlag(UpdateFieldFlag::Owner))
        {
            for (uint32 i = 0; i < 7; ++i)
            {
                data << int32(PowerCostModifier[i]);
                data << float(PowerCostMultiplier[i]);
            }
        }
        data << int32(BaseMana);
        if (fieldVisibilityFlags.HasFlag(UpdateFieldFlag::Owner))
        {
            data << int32(BaseHealth);
        }
        data << uint8(SheatheState);
        data << uint8(ViewerDependentValue<PvpFlagsTag>::GetValue(this, owner, receiver));
        data << uint8(PetFlags);
        data << uint8(ShapeshiftForm);
        if (fieldVisibilityFlags.HasFlag(UpdateFieldFlag::Owner))
        {
            data << int32(AttackPower);
            data << int32(AttackPowerModPos);
            data << int32(AttackPowerModNeg);
            data << float(AttackPowerMultiplier);
            data << int32(RangedAttackPower);
            data << int32(RangedAttackPowerModPos);
            data << int32(RangedAttackPowerModNeg);
            data << float(RangedAttackPowerMultiplier);
            data << int32(SetAttackSpeedAura);
            data << float(Lifesteal);
            data << float(MinRangedDamage);
            data << float(MaxRangedDamage);
        }
        data << float(MaxHealthModifier);
        data << float(HoverHeight);
        data << int32(MinItemLevelCutoff);
        data << int32(MinItemLevel);
        data << int32(MaxItemLevel);
        data << int32(WildBattlePetLevel);
        data << uint32(BattlePetCompanionNameTimestamp);
        data << int32(ViewerDependentValue<InteractSpellIDTag>::GetValue(this, owner, receiver));
        data << int32(ScaleDuration);
        data << int32(LooksLikeMountID);
        data << int32(LooksLikeCreatureID);
        data << int32(LookAtControllerID);
        data << int32(PerksVendorItemID);
        data << GuildGUID;
        data << uint32(PassiveSpells.size());
        data << uint32(WorldEffects.size());
        data << uint32(ChannelObjects.size());
        data << SkinningOwnerGUID;
        data << int32(FlightCapabilityID);
        data << float(GlideEventSpeedDivisor);
        data << uint32(SilencedSchoolMask);
        data << uint32(CurrentAreaID);
        if (fieldVisibilityFlags.HasFlag(UpdateFieldFlag::Owner))
        {
            data << ComboTarget;
        }
        data << float(Field_2F0);
        data << float(Field_2F4);
        for (uint32 i = 0; i < PassiveSpells.size(); ++i)
        {
            PassiveSpells[i].WriteCreate(data, owner, receiver);
        }
        for (uint32 i = 0; i < WorldEffects.size(); ++i)
        {
            data << int32(WorldEffects[i]);
        }
        for (uint32 i = 0; i < ChannelObjects.size(); ++i)
        {
            data << ChannelObjects[i];
        }
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Unit owner, Player receiver) {
        UpdateMask allowedMaskForTarget = new UpdateMask(195, new int[]{0xFFFFDFFF, 0xC3FEFFFF, 0x003DFFFF, 0xFFFFFF01, 0xF8001FFF, 0x00000003, 0x00000000});

        appendAllowedFieldsMaskForFlag(allowedMaskForTarget, fieldVisibilityFlags);
        writeUpdate(data, UpdateMask.opBitwiseAnd(getChangesMask(), allowedMaskForTarget), false, owner, receiver);
    }

    public final void appendAllowedFieldsMaskForFlag(UpdateMask allowedMaskForTarget, UpdateFieldFlag fieldVisibilityFlags) {
        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.owner)) {
            allowedMaskForTarget.OR(new UpdateMask(195, new int[]{0x00002000, 0x3C010000, 0xFFC20000, 0x400000FE, 0x03FFF000, 0xFFFFFFFE, 0x0000000F}));
        }

        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.UnitAll)) {
            allowedMaskForTarget.OR(new UpdateMask(195, new int[]{0x00000000, 0x00000000, 0x00000000, 0x40000000, 0x07FFE000, 0x00000000, 0x00000000}));
        }

        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.Empath)) {
            allowedMaskForTarget.OR(new UpdateMask(195, new int[]{0x00000000, 0x3C000000, 0x00000000, 0x00000000, 0x00000000, 0x007F8000, 0x00000000}));
        }
    }

    public final void filterDisallowedFieldsMaskForFlag(UpdateMask changesMask, UpdateFieldFlag fieldVisibilityFlags) {
        UpdateMask allowedMaskForTarget = new UpdateMask(195, new Object[]{0xFFFFDFFF, 0xC3FEFFFF, 0x003DFFFF, 0xFFFFFF01, 0xF8001FFF, 0x00000003, 0x00000000});

        appendAllowedFieldsMaskForFlag(allowedMaskForTarget, fieldVisibilityFlags);
        changesMask.AND(allowedMaskForTarget);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, Unit owner, Player receiver) {
        data.writeBits(changesMask.getBlocksMask(0), 7);

        for (int i = 0; i < 7; ++i) {
            if (changesMask.getBlock(i) != 0) {
                data.writeBits(changesMask.getBlock(i), 32);
            }
        }

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeBits(stateWorldEffectIDs.get().size(), 32);

                for (var i = 0; i < stateWorldEffectIDs.get().size(); ++i) {
                    data.writeint(stateWorldEffectIDs.get().get(i));
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(2)) {
                if (!ignoreNestedChangesMask) {
                    passiveSpells.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(passiveSpells.size(), data);
                }
            }

            if (changesMask.get(3)) {
                if (!ignoreNestedChangesMask) {
                    worldEffects.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(worldEffects.size(), data);
                }
            }

            if (changesMask.get(4)) {
                if (!ignoreNestedChangesMask) {
                    channelObjects.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(channelObjects.size(), data);
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(2)) {
                for (var i = 0; i < passiveSpells.size(); ++i) {
                    if (passiveSpells.hasChanged(i) || ignoreNestedChangesMask) {
                        passiveSpells.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(3)) {
                for (var i = 0; i < worldEffects.size(); ++i) {
                    if (worldEffects.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeint(worldEffects.get(i));
                    }
                }
            }

            if (changesMask.get(4)) {
                for (var i = 0; i < channelObjects.size(); ++i) {
                    if (channelObjects.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeGuid(channelObjects.get(i));
                    }
                }
            }

            if (changesMask.get(5)) {
                data.writeint(getViewerDependentDisplayId(this, owner, receiver));
            }

            if (changesMask.get(6)) {
                data.writeint(stateSpellVisualID);
            }

            if (changesMask.get(7)) {
                data.writeint(stateAnimID);
            }

            if (changesMask.get(8)) {
                data.writeint(stateAnimKitID);
            }

            if (changesMask.get(9)) {
                data.writeint(stateWorldEffectsQuestObjectiveID);
            }

            if (changesMask.get(10)) {
                data.writeint(spellOverrideNameID);
            }

            if (changesMask.get(11)) {
                data.writeGuid(charm);
            }

            if (changesMask.get(12)) {
                data.writeGuid(summon);
            }

            if (changesMask.get(13)) {
                data.writeGuid(critter);
            }

            if (changesMask.get(14)) {
                data.writeGuid(charmedBy);
            }

            if (changesMask.get(15)) {
                data.writeGuid(summonedBy);
            }

            if (changesMask.get(16)) {
                data.writeGuid(createdBy);
            }

            if (changesMask.get(17)) {
                data.writeGuid(demonCreator);
            }

            if (changesMask.get(18)) {
                data.writeGuid(lookAtControllerTarget);
            }

            if (changesMask.get(19)) {
                data.writeGuid(target);
            }

            if (changesMask.get(20)) {
                data.writeGuid(battlePetCompanionGUID);
            }

            if (changesMask.get(21)) {
                data.writeInt64(battlePetDBID);
            }

            if (changesMask.get(22)) {
                channelData.get().writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
            }

            if (changesMask.get(23)) {
                data.writeInt8(spellEmpowerStage);
            }

            if (changesMask.get(24)) {
                data.writeint(summonedByHomeRealm);
            }

            if (changesMask.get(25)) {
                data.writeInt8(race);
            }

            if (changesMask.get(26)) {
                data.writeInt8(classId);
            }

            if (changesMask.get(27)) {
                data.writeInt8(playerClassId);
            }

            if (changesMask.get(28)) {
                data.writeInt8(sex);
            }

            if (changesMask.get(29)) {
                data.writeInt8(displayPower);
            }

            if (changesMask.get(30)) {
                data.writeint(overrideDisplayPowerID);
            }

            if (changesMask.get(31)) {
                data.write(health);
            }
        }

        if (changesMask.get(32)) {
            if (changesMask.get(33)) {
                data.write(maxHealth);
            }

            if (changesMask.get(34)) {
                data.writeint(level);
            }

            if (changesMask.get(35)) {
                data.writeint(effectiveLevel);
            }

            if (changesMask.get(36)) {
                data.writeint(contentTuningID);
            }

            if (changesMask.get(37)) {
                data.writeint(scalingLevelMin);
            }

            if (changesMask.get(38)) {
                data.writeint(scalingLevelMax);
            }

            if (changesMask.get(39)) {
                data.writeint(scalingLevelDelta);
            }

            if (changesMask.get(40)) {
                data.writeint(scalingFactionGroup);
            }

            if (changesMask.get(41)) {
                data.writeint(scalingHealthItemLevelCurveID);
            }

            if (changesMask.get(42)) {
                data.writeint(scalingDamageItemLevelCurveID);
            }

            if (changesMask.get(43)) {
                data.writeint(getViewerDependentFactionTemplate(this, owner, receiver));
            }

            if (changesMask.get(44)) {
                data.writeint(getViewerDependentFlags(this, owner, receiver));
            }

            if (changesMask.get(45)) {
                data.writeint(flags2);
            }

            if (changesMask.get(46)) {
                data.writeint(getViewerDependentFlags3(this, owner, receiver));
            }

            if (changesMask.get(47)) {
                data.writeint(getViewerDependentAuraState(this, owner, receiver));
            }

            if (changesMask.get(48)) {
                data.writeint(rangedAttackRoundBaseTime);
            }

            if (changesMask.get(49)) {
                data.writeFloat(boundingRadius);
            }

            if (changesMask.get(50)) {
                data.writeFloat(combatReach);
            }

            if (changesMask.get(51)) {
                data.writeFloat(displayScale);
            }

            if (changesMask.get(52)) {
                data.writeint(creatureFamily);
            }

            if (changesMask.get(53)) {
                data.writeint(creatureType);
            }

            if (changesMask.get(54)) {
                data.writeint(nativeDisplayID);
            }

            if (changesMask.get(55)) {
                data.writeFloat(nativeXDisplayScale);
            }

            if (changesMask.get(56)) {
                data.writeint(mountDisplayID);
            }

            if (changesMask.get(57)) {
                data.writeint(cosmeticMountDisplayID);
            }

            if (changesMask.get(58)) {
                data.writeFloat(minDamage);
            }

            if (changesMask.get(59)) {
                data.writeFloat(maxDamage);
            }

            if (changesMask.get(60)) {
                data.writeFloat(minOffHandDamage);
            }

            if (changesMask.get(61)) {
                data.writeFloat(maxOffHandDamage);
            }

            if (changesMask.get(62)) {
                data.writeInt8(standState);
            }

            if (changesMask.get(63)) {
                data.writeInt8(petTalentPoints);
            }
        }

        if (changesMask.get(64)) {
            if (changesMask.get(65)) {
                data.writeInt8(visFlags);
            }

            if (changesMask.get(66)) {
                data.writeInt8(animTier);
            }

            if (changesMask.get(67)) {
                data.writeint(petNumber);
            }

            if (changesMask.get(68)) {
                data.writeint(petNameTimestamp);
            }

            if (changesMask.get(69)) {
                data.writeint(petExperience);
            }

            if (changesMask.get(70)) {
                data.writeint(petNextLevelExperience);
            }

            if (changesMask.get(71)) {
                data.writeFloat(modCastingSpeed);
            }

            if (changesMask.get(72)) {
                data.writeFloat(modCastingSpeedNeg);
            }

            if (changesMask.get(73)) {
                data.writeFloat(modSpellHaste);
            }

            if (changesMask.get(74)) {
                data.writeFloat(modHaste);
            }

            if (changesMask.get(75)) {
                data.writeFloat(modRangedHaste);
            }

            if (changesMask.get(76)) {
                data.writeFloat(modHasteRegen);
            }

            if (changesMask.get(77)) {
                data.writeFloat(modTimeRate);
            }

            if (changesMask.get(78)) {
                data.writeint(createdBySpell);
            }

            if (changesMask.get(79)) {
                data.writeint(emoteState);
            }

            if (changesMask.get(80)) {
                data.writeint(baseMana);
            }

            if (changesMask.get(81)) {
                data.writeint(baseHealth);
            }

            if (changesMask.get(82)) {
                data.writeInt8(sheatheState);
            }

            if (changesMask.get(83)) {
                data.writeInt8(getViewerDependentPvpFlags(this, owner, receiver));
            }

            if (changesMask.get(84)) {
                data.writeInt8(petFlags);
            }

            if (changesMask.get(85)) {
                data.writeInt8(shapeshiftForm);
            }

            if (changesMask.get(86)) {
                data.writeint(attackPower);
            }

            if (changesMask.get(87)) {
                data.writeint(attackPowerModPos);
            }

            if (changesMask.get(88)) {
                data.writeint(attackPowerModNeg);
            }

            if (changesMask.get(89)) {
                data.writeFloat(attackPowerMultiplier);
            }

            if (changesMask.get(90)) {
                data.writeint(rangedAttackPower);
            }

            if (changesMask.get(91)) {
                data.writeint(rangedAttackPowerModPos);
            }

            if (changesMask.get(92)) {
                data.writeint(rangedAttackPowerModNeg);
            }

            if (changesMask.get(93)) {
                data.writeFloat(rangedAttackPowerMultiplier);
            }

            if (changesMask.get(94)) {
                data.writeint(mainHandWeaponAttackPower);
            }

            if (changesMask.get(95)) {
                data.writeint(offHandWeaponAttackPower);
            }
        }

        if (changesMask.get(96)) {
            if (changesMask.get(97)) {
                data.writeint(rangedWeaponAttackPower);
            }

            if (changesMask.get(98)) {
                data.writeint(setAttackSpeedAura);
            }

            if (changesMask.get(99)) {
                data.writeFloat(lifesteal);
            }

            if (changesMask.get(100)) {
                data.writeFloat(minRangedDamage);
            }

            if (changesMask.get(101)) {
                data.writeFloat(maxRangedDamage);
            }

            if (changesMask.get(102)) {
                data.writeFloat(manaCostMultiplier);
            }

            if (changesMask.get(103)) {
                data.writeFloat(maxHealthModifier);
            }

            if (changesMask.get(104)) {
                data.writeFloat(hoverHeight);
            }

            if (changesMask.get(105)) {
                data.writeint(minItemLevelCutoff);
            }

            if (changesMask.get(106)) {
                data.writeint(minItemLevel);
            }

            if (changesMask.get(107)) {
                data.writeint(maxItemLevel);
            }

            if (changesMask.get(108)) {
                data.writeint(azeriteItemLevel);
            }

            if (changesMask.get(109)) {
                data.writeint(wildBattlePetLevel);
            }

            if (changesMask.get(110)) {
                data.writeint(battlePetCompanionExperience);
            }

            if (changesMask.get(111)) {
                data.writeint(battlePetCompanionNameTimestamp);
            }

            if (changesMask.get(112)) {
                data.writeint(interactSpellID);
            }

            if (changesMask.get(113)) {
                data.writeint(scaleDuration);
            }

            if (changesMask.get(114)) {
                data.writeint(looksLikeMountID);
            }

            if (changesMask.get(115)) {
                data.writeint(looksLikeCreatureID);
            }

            if (changesMask.get(116)) {
                data.writeint(lookAtControllerID);
            }

            if (changesMask.get(117)) {
                data.writeint(perksVendorItemID);
            }

            if (changesMask.get(118)) {
                data.writeint(taxiNodesID);
            }

            if (changesMask.get(119)) {
                data.writeGuid(guildGUID);
            }

            if (changesMask.get(120)) {
                data.writeint(flightCapabilityID);
            }

            if (changesMask.get(121)) {
                data.writeint(silencedSchoolMask);
            }

            if (changesMask.get(122)) {
                data.writeGuid(nameplateAttachToGUID);
            }
        }

        if (changesMask.get(123)) {
            for (var i = 0; i < 2; ++i) {
                if (changesMask.get(124 + i)) {
                    data.writeint(getViewerDependentNpcFlags(this, i, owner, receiver));
                }
            }
        }

        if (changesMask.get(126)) {
            for (var i = 0; i < 7; ++i) {
                if (changesMask.get(127 + i)) {
                    data.writeint(power.get(i));
                }

                if (changesMask.get(134 + i)) {
                    data.writeint(maxPower.get(i));
                }

                if (changesMask.get(141 + i)) {
                    data.writeFloat(powerRegenFlatModifier.get(i));
                }

                if (changesMask.get(148 + i)) {
                    data.writeFloat(powerRegenInterruptedFlatModifier.get(i));
                }
            }
        }

        if (changesMask.get(155)) {
            for (var i = 0; i < 3; ++i) {
                if (changesMask.get(156 + i)) {
                    virtualItems.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                }
            }
        }

        if (changesMask.get(159)) {
            for (var i = 0; i < 2; ++i) {
                if (changesMask.get(160 + i)) {
                    data.writeint(attackRoundBaseTime.get(i));
                }
            }
        }

        if (changesMask.get(162)) {
            for (var i = 0; i < 4; ++i) {
                if (changesMask.get(163 + i)) {
                    data.writeint(stats.get(i));
                }

                if (changesMask.get(167 + i)) {
                    data.writeint(statPosBuff.get(i));
                }

                if (changesMask.get(171 + i)) {
                    data.writeint(statNegBuff.get(i));
                }
            }
        }

        if (changesMask.get(175)) {
            for (var i = 0; i < 7; ++i) {
                if (changesMask.get(176 + i)) {
                    data.writeint(resistances.get(i));
                }

                if (changesMask.get(183 + i)) {
                    data.writeint(bonusResistanceMods.get(i));
                }

                if (changesMask.get(190 + i)) {
                    data.writeint(manaCostModifier.get(i));
                }
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(stateWorldEffectIDs);
        clearChangesMask(passiveSpells);
        clearChangesMask(worldEffects);
        clearChangesMask(channelObjects);
        clearChangesMask(displayID);
        clearChangesMask(stateSpellVisualID);
        clearChangesMask(stateAnimID);
        clearChangesMask(stateAnimKitID);
        clearChangesMask(stateWorldEffectsQuestObjectiveID);
        clearChangesMask(spellOverrideNameID);
        clearChangesMask(charm);
        clearChangesMask(summon);
        clearChangesMask(critter);
        clearChangesMask(charmedBy);
        clearChangesMask(summonedBy);
        clearChangesMask(createdBy);
        clearChangesMask(demonCreator);
        clearChangesMask(lookAtControllerTarget);
        clearChangesMask(target);
        clearChangesMask(battlePetCompanionGUID);
        clearChangesMask(battlePetDBID);
        clearChangesMask(channelData);
        clearChangesMask(spellEmpowerStage);
        clearChangesMask(summonedByHomeRealm);
        clearChangesMask(race);
        clearChangesMask(classId);
        clearChangesMask(playerClassId);
        clearChangesMask(sex);
        clearChangesMask(displayPower);
        clearChangesMask(overrideDisplayPowerID);
        clearChangesMask(health);
        clearChangesMask(maxHealth);
        clearChangesMask(level);
        clearChangesMask(effectiveLevel);
        clearChangesMask(contentTuningID);
        clearChangesMask(scalingLevelMin);
        clearChangesMask(scalingLevelMax);
        clearChangesMask(scalingLevelDelta);
        clearChangesMask(scalingFactionGroup);
        clearChangesMask(scalingHealthItemLevelCurveID);
        clearChangesMask(scalingDamageItemLevelCurveID);
        clearChangesMask(factionTemplate);
        clearChangesMask(flags);
        clearChangesMask(flags2);
        clearChangesMask(flags3);
        clearChangesMask(auraState);
        clearChangesMask(rangedAttackRoundBaseTime);
        clearChangesMask(boundingRadius);
        clearChangesMask(combatReach);
        clearChangesMask(displayScale);
        clearChangesMask(creatureFamily);
        clearChangesMask(creatureType);
        clearChangesMask(nativeDisplayID);
        clearChangesMask(nativeXDisplayScale);
        clearChangesMask(mountDisplayID);
        clearChangesMask(cosmeticMountDisplayID);
        clearChangesMask(minDamage);
        clearChangesMask(maxDamage);
        clearChangesMask(minOffHandDamage);
        clearChangesMask(maxOffHandDamage);
        clearChangesMask(standState);
        clearChangesMask(petTalentPoints);
        clearChangesMask(visFlags);
        clearChangesMask(animTier);
        clearChangesMask(petNumber);
        clearChangesMask(petNameTimestamp);
        clearChangesMask(petExperience);
        clearChangesMask(petNextLevelExperience);
        clearChangesMask(modCastingSpeed);
        clearChangesMask(modCastingSpeedNeg);
        clearChangesMask(modSpellHaste);
        clearChangesMask(modHaste);
        clearChangesMask(modRangedHaste);
        clearChangesMask(modHasteRegen);
        clearChangesMask(modTimeRate);
        clearChangesMask(createdBySpell);
        clearChangesMask(emoteState);
        clearChangesMask(baseMana);
        clearChangesMask(baseHealth);
        clearChangesMask(sheatheState);
        clearChangesMask(pvpFlags);
        clearChangesMask(petFlags);
        clearChangesMask(shapeshiftForm);
        clearChangesMask(attackPower);
        clearChangesMask(attackPowerModPos);
        clearChangesMask(attackPowerModNeg);
        clearChangesMask(attackPowerMultiplier);
        clearChangesMask(rangedAttackPower);
        clearChangesMask(rangedAttackPowerModPos);
        clearChangesMask(rangedAttackPowerModNeg);
        clearChangesMask(rangedAttackPowerMultiplier);
        clearChangesMask(mainHandWeaponAttackPower);
        clearChangesMask(offHandWeaponAttackPower);
        clearChangesMask(rangedWeaponAttackPower);
        clearChangesMask(setAttackSpeedAura);
        clearChangesMask(lifesteal);
        clearChangesMask(minRangedDamage);
        clearChangesMask(maxRangedDamage);
        clearChangesMask(manaCostMultiplier);
        clearChangesMask(maxHealthModifier);
        clearChangesMask(hoverHeight);
        clearChangesMask(minItemLevelCutoff);
        clearChangesMask(minItemLevel);
        clearChangesMask(maxItemLevel);
        clearChangesMask(azeriteItemLevel);
        clearChangesMask(wildBattlePetLevel);
        clearChangesMask(battlePetCompanionExperience);
        clearChangesMask(battlePetCompanionNameTimestamp);
        clearChangesMask(interactSpellID);
        clearChangesMask(scaleDuration);
        clearChangesMask(looksLikeMountID);
        clearChangesMask(looksLikeCreatureID);
        clearChangesMask(lookAtControllerID);
        clearChangesMask(perksVendorItemID);
        clearChangesMask(taxiNodesID);
        clearChangesMask(guildGUID);
        clearChangesMask(flightCapabilityID);
        clearChangesMask(silencedSchoolMask);
        clearChangesMask(nameplateAttachToGUID);
        clearChangesMask(npcFlags);
        clearChangesMask(power);
        clearChangesMask(maxPower);
        clearChangesMask(powerRegenFlatModifier);
        clearChangesMask(powerRegenInterruptedFlatModifier);
        clearChangesMask(virtualItems);
        clearChangesMask(attackRoundBaseTime);
        clearChangesMask(stats);
        clearChangesMask(statPosBuff);
        clearChangesMask(statNegBuff);
        clearChangesMask(resistances);
        clearChangesMask(bonusResistanceMods);
        clearChangesMask(manaCostModifier);
        getChangesMask().resetAll();
    }

    private int getViewerDependentDisplayId(UnitData unitData, Unit unit, Player receiver) {
        int displayId = unitData.displayID;

        if (unit.isCreature()) {
            var cinfo = unit.toCreature().getCreatureTemplate();
            var summon = unit.toTempSummon();

            if (summon != null) {
                if (Objects.equals(summon.getSummonerGUID(), receiver.getGUID())) {
                    if (summon.getCreatureIdVisibleToSummoner() != null) {
                        cinfo = global.getObjectMgr().getCreatureTemplate(summon.getCreatureIdVisibleToSummoner().intValue());
                    }

                    if (summon.getDisplayIdVisibleToSummoner() != null) {
                        displayId = summon.getDisplayIdVisibleToSummoner().intValue();
                    }
                }
            }

            // this also applies for transform auras
            var transform = global.getSpellMgr().getSpellInfo(unit.getTransformSpell(), unit.getMap().getDifficultyID());

            if (transform != null) {
                for (var spellEffectInfo : transform.getEffects()) {
                    if (spellEffectInfo.isAura(AuraType.Transform)) {
                        var transformInfo = global.getObjectMgr().getCreatureTemplate((int) spellEffectInfo.miscValue);

                        if (transformInfo != null) {
                            cinfo = transformInfo;

                            break;
                        }
                    }
                }
            }

            if (cinfo.flagsExtra.hasFlag(CreatureFlagExtra.trigger)) {
                if (receiver.isGameMaster()) {
                    displayId = cinfo.getFirstVisibleModel().creatureDisplayId;
                }
            }
        }

        return displayId;
    }

    private int getViewerDependentNpcFlags(UnitData unitData, int i, Unit unit, Player receiver) {
        var npcFlag = unitData.npcFlags.get(i);

        if (i == 0 && unit.isCreature() && !receiver.canSeeSpellClickOn(unit.toCreature())) {
            npcFlag &= ~(int) NPCFlags.SpellClick.getValue();
        }

        return npcFlag;
    }

    private int getViewerDependentFactionTemplate(UnitData unitData, Unit unit, Player receiver) {
        int factionTemplate = unitData.factionTemplate;

        if (unit.isControlledByPlayer() && receiver != unit && WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGroup) && unit.isInRaidWith(receiver)) {
            var ft1 = unit.getFactionTemplateEntry();
            var ft2 = receiver.getFactionTemplateEntry();

            if (ft1 != null && ft2 != null && !ft1.isFriendlyTo(ft2)) {
                // pretend that all other HOSTILE players have own faction, to allow follow, heal, rezz (trade wont work)
                factionTemplate = receiver.getFaction();
            }
        }

        return factionTemplate;
    }

    private int getViewerDependentFlags(UnitData unitData, Unit unit, Player receiver) {
        int flags = unitData.flags;

        // Update fields of triggers, transformed units or uninteractible units (values dependent on GM state)
        if (receiver.isGameMaster()) {
            flags &= ~(int) UnitFlag.Uninteractible.getValue();
        }

        return flags;
    }

    private int getViewerDependentFlags3(UnitData unitData, Unit unit, Player receiver) {
        int flags = unitData.flags3;

        if ((flags & (int) unitFlags3.AlreadySkinned.getValue()) != 0 && unit.isCreature() && !unit.toCreature().isSkinnedBy(receiver)) {
            flags &= ~(int) unitFlags3.AlreadySkinned.getValue();
        }

        return flags;
    }

    private int getViewerDependentAuraState(UnitData unitData, Unit unit, Player receiver) {
        // Check per caster aura states to not enable using a spell in client if specified aura is not by target
        return unit.buildAuraStateUpdateForTarget(receiver);
    }

    private byte getViewerDependentPvpFlags(UnitData unitData, Unit unit, Player receiver) {
        byte pvpFlags = unitData.pvpFlags;

        if (unit.isControlledByPlayer() && receiver != unit && WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGroup) && unit.isInRaidWith(receiver)) {
            var ft1 = unit.getFactionTemplateEntry();
            var ft2 = receiver.getFactionTemplateEntry();

            if (ft1 != null && ft2 != null && !ft1.isFriendlyTo(ft2)) {
                // Allow targeting opposite faction in party when enabled in config
                pvpFlags &= (byte) UnitPVPStateFlags.Sanctuary.getValue();
            }
        }

        return pvpFlags;
    }
}
