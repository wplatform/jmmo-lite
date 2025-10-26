package com.github.azeroth.dbc;

import com.github.azeroth.dbc.domain.*;
import com.github.azeroth.dbc.gtable.GameTable;
import com.github.azeroth.defines.Expansion;
import com.github.azeroth.defines.UnitClass;

public interface GameTableManager {
    default GameTable<GtArmorMitigationByLvl> armorMitigationByLvl() {
        return table(GameTables.ArmorMitigationByLvl);
    }

    default GameTable<GtArtifactKnowledgeMultiplier> artifactKnowledgeMultiplier() {
        return table(GameTables.ArtifactKnowledgeMultiplier);
    }

    default GameTable<GtArtifactLevelXP> artifactLevelXP() {
        return table(GameTables.ArtifactLevelXP);
    }

    default GameTable<GtBarberShopCostBase> barberShopCostBase() {
        return table(GameTables.BarberShopCostBase);
    }

    default GameTable<GtBaseMP> baseMP() {
        return table(GameTables.BaseMP);
    }

    default GameTable<GtBattlePetXP> battlePetXP() {
        return table(GameTables.BattlePetXP);
    }

    default GameTable<GtCombatRatings> combatRatings() {
        return table(GameTables.CombatRatings);
    }

    default GameTable<GtCombatRatingsMultByILvl> combatRatingsMultByILvl() {
        return table(GameTables.CombatRatingsMultByILvl);
    }

    default GameTable<GtHonorLevel> honorLevel() {
        return table(GameTables.HonorLevel);
    }

    default GameTable<GtHpPerSta> hpPerSta() {
        return table(GameTables.HpPerSta);
    }

    default GameTable<GtItemSocketCostPerLevel> itemSocketCostPerLevel() {
        return table(GameTables.ItemSocketCostPerLevel);
    }

    default GameTable<GtNpcDamageByClass> npcDamageByClass(Expansion expansion) {
        return switch (expansion) {
            case CLASSIC -> table(GameTables.NpcDamageByClass_0);
            case THE_BURNING_CRUSADE -> table(GameTables.NpcDamageByClass_1);
            case WRATH_OF_THE_LICH_KING -> table(GameTables.NpcDamageByClass_2);
            case CATACLYSM -> table(GameTables.NpcDamageByClass_3);
            case MISTS_OF_PANDARIA -> table(GameTables.NpcDamageByClass_4);
            case WARLORDS_OF_DRAENOR -> table(GameTables.NpcDamageByClass_5);
            case LEGION -> table(GameTables.NpcDamageByClass_6);
        };
    }

    default GameTable<GtNpcManaCostScaler> npcManaCostScaler() {
        return table(GameTables.NpcManaCostScaler);
    }

    default GameTable<GtNpcTotalHp> npcTotalHp(Expansion expansion) {
        return switch (expansion) {
            case CLASSIC -> table(GameTables.NpcTotalHp_0);
            case THE_BURNING_CRUSADE -> table(GameTables.NpcTotalHp_1);
            case WRATH_OF_THE_LICH_KING -> table(GameTables.NpcTotalHp_2);
            case CATACLYSM -> table(GameTables.NpcTotalHp_3);
            case MISTS_OF_PANDARIA -> table(GameTables.NpcTotalHp_4);
            case WARLORDS_OF_DRAENOR -> table(GameTables.NpcTotalHp_5);
            case LEGION -> table(GameTables.NpcTotalHp_6);
        };
    }

    default GameTable<GtSpellScaling> spellScaling() {
        return table(GameTables.SpellScaling);
    }

    default GameTable<GtXp> xp() {
        return table(GameTables.Xp);
    }

    default float getBaseMPValueForClass(int row, UnitClass class_) {
        GtBaseMP baseMP = baseMP().getRow(row);
        return switch (class_) {
            case WARRIOR -> baseMP.warrior;
            case PALADIN -> baseMP.paladin;
            case HUNTER -> baseMP.hunter;
            case ROGUE -> baseMP.rogue;
            case PRIEST -> baseMP.priest;
            case DEATH_KNIGHT -> baseMP.deathKnight;
            case SHAMAN -> baseMP.shaman;
            case MAGE -> baseMP.mage;
            case WARLOCK -> baseMP.warlock;
            case MONK -> baseMP.monk;
            case DRUID -> baseMP.druid;
            case DEMON_HUNTER -> baseMP.demonHunter;
            case NONE -> 0.0f;
        };

    }


    <T> GameTable<T> table(GameTables gameTable);

}
