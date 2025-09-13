package com.github.azeroth.game.spell;


import com.github.azeroth.game.battlefield.BattleFieldTypes;
import com.github.azeroth.game.entity.player.Player;

public class SpellArea {

    public int spellId;

    public int areaId; // zone/subzone/or 0 is not limited to zone

    public int questStart; // quest start (quest must be active or rewarded for spell apply)

    public int questEnd; // quest end (quest must not be rewarded for spell apply)
    public int auraSpell; // spell aura must be applied for spell apply)if possitive) and it must not be applied in other case

    public long raceMask; // can be applied only to races
    public gender gender = Framework.Constants.gender.values()[0]; // can be applied only to gender

    public int questStartStatus; // QuestStatus that quest_start must have in order to keep the spell

    public int questEndStatus; // QuestStatus that the quest_end must have in order to keep the spell (if the quest_end's status is different than this, the spell will be dropped)
    public SpellAreaFlag flags = SpellAreaFlag.values()[0]; // if SPELL_AREA_FLAG_AUTOCAST then auto applied at area enter, in other case just allowed to cast || if SPELL_AREA_FLAG_AUTOREMOVE then auto removed inside area (will allways be removed on leaved even without flag)

    // helpers
    public final boolean isFitToRequirements(Player player, int newZone, int newArea) {
        if (gender != gender.NONE) // not in expected gender
        {
            if (player == null || gender != player.getNativeGender()) {
                return false;
            }
        }

        if (raceMask != 0) // not in expected race
        {
            if (player == null || !(boolean) (raceMask & (long) SharedConst.GetMaskForRace(player.getRace()))) {
                return false;
            }
        }

        if (areaId != 0) // not in expected zone
        {
            if (newZone != areaId && newArea != areaId) {
                return false;
            }
        }

        if (questStart != 0) // not in expected required quest state
        {
            if (player == null || (((1 << player.getQuestStatus(questStart).getValue()) & questStartStatus) == 0)) {
                return false;
            }
        }

        if (questEnd != 0) // not in expected forbidden quest state
        {
            if (player == null || (((1 << player.getQuestStatus(questEnd).getValue()) & questEndStatus) == 0)) {
                return false;
            }
        }

        if (auraSpell != 0) // not have expected aura
        {
            if (player == null || (auraSpell > 0 && !player.hasAura(auraSpell)) || (auraSpell < 0 && player.hasAura(-AuraSpell))) {
                return false;
            }
        }

        if (player) {
            var bg = player.getBattleground();

            if (bg) {
                return bg.isSpellAllowed(spellId, player);
            }
        }

        // Extra conditions -- leaving the possibility add extra conditions...
        switch (spellId) {
            case 91604: // No fly Zone - Wintergrasp
            {
                if (!player) {
                    return false;
                }

                var Bf = global.getBattleFieldMgr().getBattlefieldToZoneId(player.getMap(), player.getZoneId());

                if (Bf == null || Bf.canFlyIn() || (!player.hasAuraType(AuraType.ModIncreaseMountedFlightSpeed) && !player.hasAuraType(AuraType.Fly))) {
                    return false;
                }

                break;
            }
            case 56618: // Horde Controls Factory Phase Shift
            case 56617: // Alliance Controls Factory Phase Shift
            {
                if (!player) {
                    return false;
                }

                var bf = global.getBattleFieldMgr().getBattlefieldToZoneId(player.getMap(), player.getZoneId());

                if (bf == null || bf.getTypeId() != BattleFieldTypes.WinterGrasp.getValue()) {
                    return false;
                }

                // team that controls the workshop in the specified area
                var team = bf.getData(newArea);

                if (team == TeamId.HORDE) {
                    return spellId == 56618;
                } else if (team == TeamId.ALLIANCE) {
                    return spellId == 56617;
                }

                break;
            }
            case 57940: // Essence of Wintergrasp - Northrend
            case 58045: // Essence of Wintergrasp - Wintergrasp
            {
                if (!player) {
                    return false;
                }

                var battlefieldWG = global.getBattleFieldMgr().getBattlefieldByBattleId(player.getMap(), 1);

                if (battlefieldWG != null) {
                    return battlefieldWG.isEnabled() && (player.getTeamId() == battlefieldWG.getDefenderTeam()) && !battlefieldWG.isWarTime();
                }

                break;
            }
            case 74411: // Battleground- Dampening
            {
                if (!player) {
                    return false;
                }

                var bf = global.getBattleFieldMgr().getBattlefieldToZoneId(player.getMap(), player.getZoneId());

                if (bf != null) {
                    return bf.isWarTime();
                }

                break;
            }
        }

        return true;
    }
}
