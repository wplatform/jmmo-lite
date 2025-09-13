package com.github.azeroth.game.entity.item;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.dbc.defines.ItemSpecStat;
import com.github.azeroth.dbc.domain.ItemEntry;
import com.github.azeroth.dbc.domain.ItemSparse;
import com.github.azeroth.game.entity.item.enums.*;

import static com.github.azeroth.dbc.defines.DbcDefine.MAX_ITEM_PROTO_STATS;


public class ItemSpecStats {

    public int itemType;
    public ItemSpecStat[] itemSpecStatTypes = new ItemSpecStat[MAX_ITEM_PROTO_STATS];
    public int itemSpecStatCount;

    public ItemSpecStats(ItemEntry item, ItemSparse sparse, DbcObjectManager dbcObjectManager) {
        ItemClass itemClass = ItemClass.values()[item.getClassID()];
        ItemSubclassWeapon itemSubclassWeapon = ItemSubclassWeapon.values()[item.getSubclassID()];
        if (itemClass == ItemClass.WEAPON) {
            itemType = 5;

            switch (itemSubclassWeapon) {
                case AXE:
                    addStat(ItemSpecStat.ONE_HANDED_AXE);
                    break;
                case AXE2:
                    addStat(ItemSpecStat.TWO_HANDED_AXE);
                    break;
                case BOW:
                    addStat(ItemSpecStat.BOW);
                    break;
                case GUN:
                    addStat(ItemSpecStat.GUN);
                    break;
                case MACE:
                    addStat(ItemSpecStat.ONE_HANDED_MACE);
                    break;
                case MACE2:
                    addStat(ItemSpecStat.TWO_HANDED_MACE);
                    break;
                case POLEARM:
                    addStat(ItemSpecStat.POLEARM);
                    break;
                case SWORD:
                    addStat(ItemSpecStat.ONE_HANDED_SWORD);
                    break;
                case SWORD2:
                    addStat(ItemSpecStat.TWO_HANDED_SWORD);

                    break;
                case WARGLAIVES:
                    addStat(ItemSpecStat.WARGLAIVES);

                    break;
                case STAFF:
                    addStat(ItemSpecStat.STAFF);

                    break;
                case FIST_WEAPON:
                    addStat(ItemSpecStat.FIST_WEAPON);

                    break;
                case DAGGER:
                    addStat(ItemSpecStat.DAGGER);

                    break;
                case THROWN:
                    addStat(ItemSpecStat.THROWN);

                    break;
                case CROSSBOW:
                    addStat(ItemSpecStat.CROSSBOW);

                    break;
                case WAND:
                    addStat(ItemSpecStat.WAND);

                    break;
                default:
                    break;
            }
        } else if (itemClass == ItemClass.ARMOR) {
            ItemSubClassArmor itemSubClassArmor = ItemSubClassArmor.values()[item.getSubclassID()];
            switch (itemSubClassArmor) {
                case CLOTH:
                    if (sparse.getInventoryType() != InventoryType.CLOAK.ordinal()) {
                        itemType = 1;

                        break;
                    }

                    itemType = 0;
                    addStat(ItemSpecStat.CLOAK);

                    break;
                case LEATHER:
                    itemType = 2;

                    break;
                case MAIL:
                    itemType = 3;

                    break;
                case PLATE:
                    itemType = 4;

                    break;
                default:
                    if (itemSubClassArmor == ItemSubClassArmor.SHIELD) {
                        itemType = 6;
                        addStat(ItemSpecStat.SHIELD);
                    } else if (item.getSubclassID() > ItemSubClassArmor.SHIELD.ordinal()
                            && item.getSubclassID() <= ItemSubClassArmor.RELIC.ordinal()) {
                        itemType = 6;
                        addStat(ItemSpecStat.RELIC);
                    } else {
                        itemType = 0;
                    }

                    break;
            }
        } else if (itemClass == ItemClass.GEM) {
            itemType = 7;

            var gem = dbcObjectManager.gemProperty(sparse.getGemProperties());
            if (gem != null) {
                EnumFlag<SocketColor> gemType = EnumFlag.of(SocketColor.class, gem.getType());
                if (gemType.hasFlag(SocketColor.RELIC_IRON)) {
                    addStat(ItemSpecStat.RELIC_IRON);
                }

                if (gemType.hasFlag(SocketColor.RELIC_BLOOD)) {
                    addStat(ItemSpecStat.RELIC_BLOOD);
                }

                if (gemType.hasFlag(SocketColor.RELIC_SHADOW)) {
                    addStat(ItemSpecStat.RELIC_SHADOW);
                }

                if (gemType.hasFlag(SocketColor.RELIC_FEL)) {
                    addStat(ItemSpecStat.RELIC_FEL);
                }

                if (gemType.hasFlag(SocketColor.RELIC_ARCANE)) {
                    addStat(ItemSpecStat.RELIC_ARCANE);
                }

                if (gemType.hasFlag(SocketColor.RELIC_FROST)) {
                    addStat(ItemSpecStat.RELIC_FROST);
                }

                if (gemType.hasFlag(SocketColor.RELIC_FIRE)) {
                    addStat(ItemSpecStat.RELIC_FIRE);
                }

                if (gemType.hasFlag(SocketColor.RELIC_WATER)) {
                    addStat(ItemSpecStat.RELIC_WATER);
                }

                if (gemType.hasFlag(SocketColor.RELIC_LIFE)) {
                    addStat(ItemSpecStat.RELIC_LIFE);
                }

                if (gemType.hasFlag(SocketColor.RELIC_WIND)) {
                    addStat(ItemSpecStat.RELIC_WIND);
                }

                if (gemType.hasFlag(SocketColor.RELIC_HOLY)) {
                    addStat(ItemSpecStat.RELIC_HOLY);
                }
            }
        } else {
            itemType = 0;
        }

        short[] statModifierBonusStat = sparse.getStatModifierBonusStat();
        for (int i = 0; i < MAX_ITEM_PROTO_STATS; ++i) {
            if (statModifierBonusStat[i] != -1) {
                addModStat(statModifierBonusStat[i]);
            }
        }
    }

    private void addStat(ItemSpecStat statType) {
        if (itemSpecStatCount >= MAX_ITEM_PROTO_STATS) {
            return;
        }
        for (int i = 0; i < MAX_ITEM_PROTO_STATS; ++i) {
            if (itemSpecStatTypes[i] == statType) {
                return;
            }
        }
        itemSpecStatTypes[itemSpecStatCount++] = statType;
    }

    private void addModStat(int itemStatType) {

        ItemModType itemModType = ItemModType.values()[itemStatType];

        switch (itemModType) {
            case AGILITY:
                addStat(ItemSpecStat.AGILITY);

                break;
            case STRENGTH:
                addStat(ItemSpecStat.STRENGTH);

                break;
            case INTELLECT:
                addStat(ItemSpecStat.INTELLECT);

                break;
            case DODGE_RATING:
                addStat(ItemSpecStat.DODGE);

                break;
            case PARRY_RATING:
                addStat(ItemSpecStat.PARRY);

                break;
            case CRIT_MELEE_RATING:
            case CRIT_RANGED_RATING:
            case CRIT_SPELL_RATING:
            case CRIT_RATING:
                addStat(ItemSpecStat.CRIT);

                break;
            case HASTE_RATING:
                addStat(ItemSpecStat.HASTE);

                break;
            case HIT_RATING:
                addStat(ItemSpecStat.HIT);

                break;
            case EXTRA_ARMOR:
                addStat(ItemSpecStat.BONUS_ARMOR);

                break;
            case AGI_STR_INT:
                addStat(ItemSpecStat.AGILITY);
                addStat(ItemSpecStat.STRENGTH);
                addStat(ItemSpecStat.INTELLECT);

                break;
            case AGI_STR:
                addStat(ItemSpecStat.AGILITY);
                addStat(ItemSpecStat.STRENGTH);

                break;
            case AGI_INT:
                addStat(ItemSpecStat.AGILITY);
                addStat(ItemSpecStat.INTELLECT);

                break;
            case STR_INT:
                addStat(ItemSpecStat.STRENGTH);
                addStat(ItemSpecStat.INTELLECT);

                break;
        }
    }
}
