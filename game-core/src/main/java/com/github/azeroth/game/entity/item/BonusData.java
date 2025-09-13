package com.github.azeroth.game.entity.item;


public class BonusData {
    public Itemquality quality = itemQuality.values()[0];
    public int itemLevelBonus;
    public int requiredLevel;
    public int[] itemStatType = new int[ItemConst.MaxStats];
    public int[] statPercentEditor = new int[ItemConst.MaxStats];
    public float[] itemStatSocketCostMultiplier = new float[ItemConst.MaxStats];
    public SocketColor[] socketColor = new SocketColor[ItemConst.MaxGemSockets];
    public ItembondingType bonding = ItemBondingType.values()[0];
    public int appearanceModID;
    public float repairCostMultiplier;
    public int contentTuningId;
    public int playerLevelToItemLevelCurveId;
    public int disenchantLootId;
    public int[] gemItemLevelBonus = new int[ItemConst.MaxGemSockets];
    public int[] gemRelicType = new int[ItemConst.MaxGemSockets];
    public short[] gemRelicRankBonus = new short[ItemConst.MaxGemSockets];
    public int relicType;
    public int requiredLevelOverride;
    public int azeriteTierUnlockSetId;
    public int suffix;
    public int requiredLevelCurve;
    public ItemEffectRecord[] effects = new ItemEffectRecord[13];
    public int effectCount;
    public boolean canDisenchant;
    public boolean canScrap;
    public boolean hasFixedLevel;
    private state state = new state();

    public bonusData(ItemTemplate proto) {
        if (proto == null) {
            return;
        }

        quality = proto.getQuality();
        itemLevelBonus = 0;
        requiredLevel = proto.getBaseRequiredLevel();

        for (int i = 0; i < ItemConst.MaxStats; ++i) {
            ItemStatType[i] = proto.getStatModifierBonusStat(i);
        }

        for (int i = 0; i < ItemConst.MaxStats; ++i) {
            StatPercentEditor[i] = proto.getStatPercentEditor(i);
        }

        for (int i = 0; i < ItemConst.MaxStats; ++i) {
            ItemStatSocketCostMultiplier[i] = proto.getStatPercentageOfSocket(i);
        }

        for (int i = 0; i < ItemConst.MaxGemSockets; ++i) {
            socketColor[i] = proto.getSocketColor(i);
            GemItemLevelBonus[i] = 0;
            GemRelicType[i] = -1;
            GemRelicRankBonus[i] = 0;
        }

        bonding = proto.getBonding();

        appearanceModID = 0;
        repairCostMultiplier = 1.0f;
        contentTuningId = proto.getScalingStatContentTuning();
        playerLevelToItemLevelCurveId = proto.getPlayerLevelToItemLevelCurveId();
        relicType = -1;
        hasFixedLevel = false;
        requiredLevelOverride = 0;
        azeriteTierUnlockSetId = 0;

        var azeriteEmpoweredItem = global.getDB2Mgr().GetAzeriteEmpoweredItem(proto.getId());

        if (azeriteEmpoweredItem != null) {
            azeriteTierUnlockSetId = azeriteEmpoweredItem.AzeriteTierUnlockSetID;
        }

        effectCount = 0;

        for (var itemEffect : proto.getEffects()) {
            Effects[EffectCount++] = itemEffect;
        }

        for (var i = effectCount; i < effects.length; ++i) {
            Effects[i] = null;
        }

        canDisenchant = !proto.hasFlag(ItemFlags.NoDisenchant);
        canScrap = proto.hasFlag(ItemFlags4.Scrapable);

        state.suffixPriority = Integer.MAX_VALUE;
        state.appearanceModPriority = Integer.MAX_VALUE;
        state.scalingStatDistributionPriority = Integer.MAX_VALUE;
        state.azeriteTierUnlockSetPriority = Integer.MAX_VALUE;
        state.requiredLevelCurvePriority = Integer.MAX_VALUE;
        state.hasQualityBonus = false;
    }

    public bonusData(ItemInstance itemInstance) {
        this(global.getObjectMgr().getItemTemplate(itemInstance.itemID));
        if (itemInstance.itemBonus != null) {
            for (var bonusListID : itemInstance.itemBonus.bonusListIDs) {
                addBonusList(bonusListID);
            }
        }
    }

    public final void addBonusList(int bonusListId) {
        var bonuses = global.getDB2Mgr().GetItemBonusList(bonusListId);

        if (bonuses != null) {
            for (var bonus : bonuses) {
                addBonus(bonus.BonusType, bonus.value);
            }
        }
    }

    public final void addBonus(ItemBonusType type, int[] values) {
        switch (type) {
            case ItemLevel:
                itemLevelBonus += values[0];

                break;
            case Stat: {
                int statIndex;

                for (statIndex = 0; statIndex < ItemConst.MaxStats; ++statIndex) {
                    if (ItemStatType[statIndex] == values[0] || ItemStatType[statIndex] == -1) {
                        break;
                    }
                }

                if (statIndex < ItemConst.MaxStats) {
                    ItemStatType[statIndex] = values[0];
                    StatPercentEditor[statIndex] += values[1];
                }

                break;
            }
            case Quality:
                if (!state.hasQualityBonus) {
                    quality = itemQuality.forValue(values[0]);
                    state.hasQualityBonus = true;
                } else if ((int) quality.getValue() < values[0]) {
                    quality = itemQuality.forValue(values[0]);
                }

                break;
            case Suffix:
                if (values[1] < state.suffixPriority) {
                    suffix = (int) values[0];
                    state.suffixPriority = values[1];
                }

                break;
            case Socket: {
                var socketCount = (int) values[0];

                for (int i = 0; i < ItemConst.MaxGemSockets && socketCount != 0; ++i) {
                    if (socketColor[i] == 0) {
                        socketColor[i] = SocketColor.forValue(values[1]);
                        --socketCount;
                    }
                }

                break;
            }
            case Appearance:
                if (values[1] < state.appearanceModPriority) {
                    appearanceModID = (int) values[0];
                    state.appearanceModPriority = values[1];
                }

                break;
            case RequiredLevel:
                requiredLevel += values[0];

                break;
            case RepairCostMuliplier:
                RepairCostMultiplier *= (float) values[0] * 0.01f;

                break;
            case ScalingStatDistribution:
            case ScalingStatDistributionFixed:
                if (values[1] < state.scalingStatDistributionPriority) {
                    contentTuningId = (int) values[2];
                    playerLevelToItemLevelCurveId = (int) values[3];
                    state.scalingStatDistributionPriority = values[1];
                    hasFixedLevel = type == ItemBonusType.ScalingStatDistributionFixed;
                }

                break;
            case Bounding:
                bonding = ItemBondingType.forValue(values[0]);

                break;
            case RelicType:
                relicType = values[0];

                break;
            case OverrideRequiredLevel:
                requiredLevelOverride = values[0];

                break;
            case AzeriteTierUnlockSet:
                if (values[1] < state.azeriteTierUnlockSetPriority) {
                    azeriteTierUnlockSetId = (int) values[0];
                    state.azeriteTierUnlockSetPriority = values[1];
                }

                break;
            case OverrideCanDisenchant:
                canDisenchant = values[0] != 0;

                break;
            case OverrideCanScrap:
                canScrap = values[0] != 0;

                break;
            case ItemEffectId:
                var itemEffect = CliDB.ItemEffectStorage.get(values[0]);

                if (itemEffect != null) {
                    Effects[EffectCount++] = itemEffect;
                }

                break;
            case RequiredLevelCurve:
                if (values[2] < state.requiredLevelCurvePriority) {
                    requiredLevelCurve = (int) values[0];
                    state.requiredLevelCurvePriority = values[2];

                    if (values[1] != 0) {
                        contentTuningId = (int) values[1];
                    }
                }

                break;
        }
    }

    private final static class State {
        public int suffixPriority;
        public int appearanceModPriority;
        public int scalingStatDistributionPriority;
        public int azeriteTierUnlockSetPriority;
        public int requiredLevelCurvePriority;
        public boolean hasQualityBonus;

        public State clone() {
            State varCopy = new state();

            varCopy.suffixPriority = this.suffixPriority;
            varCopy.appearanceModPriority = this.appearanceModPriority;
            varCopy.scalingStatDistributionPriority = this.scalingStatDistributionPriority;
            varCopy.azeriteTierUnlockSetPriority = this.azeriteTierUnlockSetPriority;
            varCopy.requiredLevelCurvePriority = this.requiredLevelCurvePriority;
            varCopy.hasQualityBonus = this.hasQualityBonus;

            return varCopy;
        }
    }
}
