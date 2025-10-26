package com.github.azeroth.game.spell;


import com.github.azeroth.common.Flag128;
import com.github.azeroth.common.YieldResult;
import com.github.azeroth.dbc.defines.ExpectedStatType;
import com.github.azeroth.dbc.defines.SpellEffectAttributes;
import com.github.azeroth.dbc.domain.SpellEffect;
import com.github.azeroth.dbc.domain.SpellRadius;
import com.github.azeroth.defines.*;
import com.github.azeroth.game.domain.condition.Condition;
import com.github.azeroth.game.entity.item.ItemEnchantmentManager;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.spell.enums.SpellEffectImplicitTargetTypes;
import com.github.azeroth.game.spell.enums.SpellTargetObjectTypes;
import com.github.azeroth.utils.RandomUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class SpellEffectInfo {


    private static final StaticData[] DATA = {
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 0
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 1 SPELL_EFFECT_INSTAKILL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 2 SPELL_EFFECT_SCHOOL_DAMAGE
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 3 SPELL_EFFECT_DUMMY
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 4 SPELL_EFFECT_PORTAL_TELEPORT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 5 SPELL_EFFECT_5
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 6 SPELL_EFFECT_APPLY_AURA
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 7 SPELL_EFFECT_ENVIRONMENTAL_DAMAGE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 8 SPELL_EFFECT_POWER_DRAIN
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 9 SPELL_EFFECT_HEALTH_LEECH
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 10 SPELL_EFFECT_HEAL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 11 SPELL_EFFECT_BIND
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 12 SPELL_EFFECT_PORTAL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT_AND_DEST),   // 13 SPELL_EFFECT_TELEPORT_TO_RETURN_POINT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 14 SPELL_EFFECT_INCREASE_CURRENCY_CAP
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT_AND_DEST),   // 15 SPELL_EFFECT_TELEPORT_WITH_SPELL_VISUAL_KIT_LOADING_SCREEN
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 16 SPELL_EFFECT_QUEST_COMPLETE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 17 SPELL_EFFECT_WEAPON_DAMAGE_NOSCHOOL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.CORPSE_ALLY),     // 18 SPELL_EFFECT_RESURRECT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 19 SPELL_EFFECT_ADD_EXTRA_ATTACKS
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 20 SPELL_EFFECT_DODGE
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 21 SPELL_EFFECT_EVADE
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 22 SPELL_EFFECT_PARRY
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 23 SPELL_EFFECT_BLOCK
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 24 SPELL_EFFECT_CREATE_ITEM
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 25 SPELL_EFFECT_WEAPON
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 26 SPELL_EFFECT_DEFENSE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 27 SPELL_EFFECT_PERSISTENT_AREA_AURA
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 28 SPELL_EFFECT_SUMMON
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT_AND_DEST),   // 29 SPELL_EFFECT_LEAP
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 30 SPELL_EFFECT_ENERGIZE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 31 SPELL_EFFECT_WEAPON_PERCENT_DAMAGE
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 32 SPELL_EFFECT_TRIGGER_MISSILE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.GOBJ_ITEM),       // 33 SPELL_EFFECT_OPEN_LOCK
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 34 SPELL_EFFECT_SUMMON_CHANGE_ITEM
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 35 SPELL_EFFECT_APPLY_AREA_AURA_PARTY
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 36 SPELL_EFFECT_LEARN_SPELL
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 37 SPELL_EFFECT_SPELL_DEFENSE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 38 SPELL_EFFECT_DISPEL
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 39 SPELL_EFFECT_LANGUAGE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 40 SPELL_EFFECT_DUAL_WIELD
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 41 SPELL_EFFECT_JUMP
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 42 SPELL_EFFECT_JUMP_DEST
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT_AND_DEST),   // 43 SPELL_EFFECT_TELEPORT_UNITS_FACE_CASTER
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 44 SPELL_EFFECT_SKILL_STEP
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 45 SPELL_EFFECT_ADD_HONOR
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 46 SPELL_EFFECT_SPAWN
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 47 SPELL_EFFECT_TRADE_SKILL
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 48 SPELL_EFFECT_STEALTH
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 49 SPELL_EFFECT_DETECT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 50 SPELL_EFFECT_TRANS_DOOR
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 51 SPELL_EFFECT_FORCE_CRITICAL_HIT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 52 SPELL_EFFECT_SET_MAX_BATTLE_PET_COUNT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 53 SPELL_EFFECT_ENCHANT_ITEM
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 54 SPELL_EFFECT_ENCHANT_ITEM_TEMPORARY
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 55 SPELL_EFFECT_TAMECREATURE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 56 SPELL_EFFECT_SUMMON_PET
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 57 SPELL_EFFECT_LEARN_PET_SPELL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 58 SPELL_EFFECT_WEAPON_DAMAGE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 59 SPELL_EFFECT_CREATE_RANDOM_ITEM
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 60 SPELL_EFFECT_PROFICIENCY
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 61 SPELL_EFFECT_SEND_EVENT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 62 SPELL_EFFECT_POWER_BURN
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 63 SPELL_EFFECT_THREAT
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 64 SPELL_EFFECT_TRIGGER_SPELL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 65 SPELL_EFFECT_APPLY_AREA_AURA_RAID
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 66 SPELL_EFFECT_RECHARGE_ITEM
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 67 SPELL_EFFECT_HEAL_MAX_HEALTH
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 68 SPELL_EFFECT_INTERRUPT_CAST
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT_AND_DEST),   // 69 SPELL_EFFECT_DISTRACT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 70 SPELL_EFFECT_COMPLETE_AND_REWARD_WORLD_QUEST
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 71 SPELL_EFFECT_PICKPOCKET
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 72 SPELL_EFFECT_ADD_FARSIGHT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 73 SPELL_EFFECT_UNTRAIN_TALENTS
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 74 SPELL_EFFECT_APPLY_GLYPH
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 75 SPELL_EFFECT_HEAL_MECHANICAL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 76 SPELL_EFFECT_SUMMON_OBJECT_WILD
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 77 SPELL_EFFECT_SCRIPT_EFFECT
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 78 SPELL_EFFECT_ATTACK
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 79 SPELL_EFFECT_SANCTUARY
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 80 SPELL_EFFECT_MODIFY_FOLLOWER_ITEM_LEVEL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 81 SPELL_EFFECT_PUSH_ABILITY_TO_ACTION_BAR
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 82 SPELL_EFFECT_BIND_SIGHT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT_AND_DEST),   // 83 SPELL_EFFECT_DUEL
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 84 SPELL_EFFECT_STUCK
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 85 SPELL_EFFECT_SUMMON_PLAYER
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.GOBJ),            // 86 SPELL_EFFECT_ACTIVATE_OBJECT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.GOBJ),            // 87 SPELL_EFFECT_GAMEOBJECT_DAMAGE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.GOBJ),            // 88 SPELL_EFFECT_GAMEOBJECT_REPAIR
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.GOBJ),            // 89 SPELL_EFFECT_GAMEOBJECT_SET_DESTRUCTION_STATE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 90 SPELL_EFFECT_KILL_CREDIT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 91 SPELL_EFFECT_THREAT_ALL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 92 SPELL_EFFECT_ENCHANT_HELD_ITEM
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 93 SPELL_EFFECT_FORCE_DESELECT
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 94 SPELL_EFFECT_SELF_RESURRECT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 95 SPELL_EFFECT_SKINNING
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 96 SPELL_EFFECT_CHARGE
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 97 SPELL_EFFECT_CAST_BUTTON
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 98 SPELL_EFFECT_KNOCK_BACK
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 99 SPELL_EFFECT_DISENCHANT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 100 SPELL_EFFECT_INEBRIATE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 101 SPELL_EFFECT_FEED_PET
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 102 SPELL_EFFECT_DISMISS_PET
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 103 SPELL_EFFECT_REPUTATION
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 104 SPELL_EFFECT_SUMMON_OBJECT_SLOT1
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 105 SPELL_EFFECT_SURVEY
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 106 SPELL_EFFECT_CHANGE_RAID_MARKER
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 107 SPELL_EFFECT_SHOW_CORPSE_LOOT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 108 SPELL_EFFECT_DISPEL_MECHANIC
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 109 SPELL_EFFECT_RESURRECT_PET
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 110 SPELL_EFFECT_DESTROY_ALL_TOTEMS
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 111 SPELL_EFFECT_DURABILITY_DAMAGE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 112 SPELL_EFFECT_112
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 113 SPELL_EFFECT_CANCEL_CONVERSATION
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 114 SPELL_EFFECT_ATTACK_ME
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 115 SPELL_EFFECT_DURABILITY_DAMAGE_PCT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.CORPSE_ENEMY),    // 116 SPELL_EFFECT_SKIN_PLAYER_CORPSE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 117 SPELL_EFFECT_SPIRIT_HEAL
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 118 SPELL_EFFECT_SKILL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 119 SPELL_EFFECT_APPLY_AREA_AURA_PET
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 120 SPELL_EFFECT_TELEPORT_GRAVEYARD
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 121 SPELL_EFFECT_NORMALIZED_WEAPON_DMG
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 122 SPELL_EFFECT_122
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 123 SPELL_EFFECT_SEND_TAXI
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 124 SPELL_EFFECT_PULL_TOWARDS
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 125 SPELL_EFFECT_MODIFY_THREAT_PERCENT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 126 SPELL_EFFECT_STEAL_BENEFICIAL_BUFF
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 127 SPELL_EFFECT_PROSPECTING
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 128 SPELL_EFFECT_APPLY_AREA_AURA_FRIEND
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 129 SPELL_EFFECT_APPLY_AREA_AURA_ENEMY
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 130 SPELL_EFFECT_REDIRECT_THREAT
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 131 SPELL_EFFECT_PLAY_SOUND
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 132 SPELL_EFFECT_PLAY_MUSIC
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 133 SPELL_EFFECT_UNLEARN_SPECIALIZATION
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 134 SPELL_EFFECT_KILL_CREDIT2
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 135 SPELL_EFFECT_CALL_PET
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 136 SPELL_EFFECT_HEAL_PCT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 137 SPELL_EFFECT_ENERGIZE_PCT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 138 SPELL_EFFECT_LEAP_BACK
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 139 SPELL_EFFECT_CLEAR_QUEST
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 140 SPELL_EFFECT_FORCE_CAST
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 141 SPELL_EFFECT_FORCE_CAST_WITH_VALUE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 142 SPELL_EFFECT_TRIGGER_SPELL_WITH_VALUE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 143 SPELL_EFFECT_APPLY_AREA_AURA_OWNER
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT_AND_DEST),   // 144 SPELL_EFFECT_KNOCK_BACK_DEST
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT_AND_DEST),   // 145 SPELL_EFFECT_PULL_TOWARDS_DEST
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 146 SPELL_EFFECT_RESTORE_GARRISON_TROOP_VITALITY
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 147 SPELL_EFFECT_QUEST_FAIL
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 148 SPELL_EFFECT_TRIGGER_MISSILE_SPELL_WITH_VALUE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 149 SPELL_EFFECT_CHARGE_DEST
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 150 SPELL_EFFECT_QUEST_START
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 151 SPELL_EFFECT_TRIGGER_SPELL_2
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 152 SPELL_EFFECT_SUMMON_RAF_FRIEND
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 153 SPELL_EFFECT_CREATE_TAMED_PET
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 154 SPELL_EFFECT_DISCOVER_TAXI
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.UNIT),                // 155 SPELL_EFFECT_TITAN_GRIP
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 156 SPELL_EFFECT_ENCHANT_ITEM_PRISMATIC
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 157 SPELL_EFFECT_CREATE_LOOT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 158 SPELL_EFFECT_MILLING
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 159 SPELL_EFFECT_ALLOW_RENAME_PET
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 160 SPELL_EFFECT_FORCE_CAST_2
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 161 SPELL_EFFECT_TALENT_SPEC_COUNT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 162 SPELL_EFFECT_TALENT_SPEC_SELECT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 163 SPELL_EFFECT_OBLITERATE_ITEM
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 164 SPELL_EFFECT_REMOVE_AURA
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 165 SPELL_EFFECT_DAMAGE_FROM_MAX_HEALTH_PCT
            new StaticData(SpellEffectImplicitTargetTypes.CASTER, SpellTargetObjectTypes.UNIT),              // 166 SPELL_EFFECT_GIVE_CURRENCY
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 167 SPELL_EFFECT_UPDATE_PLAYER_PHASE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 168 SPELL_EFFECT_ALLOW_CONTROL_PET
            new StaticData(SpellEffectImplicitTargetTypes.CASTER, SpellTargetObjectTypes.UNIT),              // 169 SPELL_EFFECT_DESTROY_ITEM
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 170 SPELL_EFFECT_UPDATE_ZONE_AURAS_AND_PHASES
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 171 SPELL_EFFECT_SUMMON_PERSONAL_GAMEOBJECT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.CORPSE_ALLY),     //ALLY}, // 172 SPELL_EFFECT_RESURRECT_WITH_AURA
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 173 SPELL_EFFECT_UNLOCK_GUILD_VAULT_TAB
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 174 SPELL_EFFECT_APPLY_AURA_ON_PET
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 175 SPELL_EFFECT_175
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 176 SPELL_EFFECT_SANCTUARY_2
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 177 SPELL_EFFECT_DESPAWN_PERSISTENT_AREA_AURA
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 178 SPELL_EFFECT_178
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 179 SPELL_EFFECT_CREATE_AREATRIGGER
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 180 SPELL_EFFECT_UPDATE_AREATRIGGER
            new StaticData(SpellEffectImplicitTargetTypes.CASTER, SpellTargetObjectTypes.UNIT),              // 181 SPELL_EFFECT_REMOVE_TALENT
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 182 SPELL_EFFECT_DESPAWN_AREATRIGGER
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 183 SPELL_EFFECT_183
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 184 SPELL_EFFECT_REPUTATION_2
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 185 SPELL_EFFECT_185
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 186 SPELL_EFFECT_186
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 187 SPELL_EFFECT_RANDOMIZE_ARCHAEOLOGY_DIGSITES
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.DEST),            // 188 SPELL_EFFECT_SUMMON_STABLED_PET_AS_GUARDIAN
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 189 SPELL_EFFECT_LOOT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 190 SPELL_EFFECT_CHANGE_PARTY_MEMBERS
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 191 SPELL_EFFECT_TELEPORT_TO_DIGSITE
            new StaticData(SpellEffectImplicitTargetTypes.CASTER, SpellTargetObjectTypes.UNIT),              // 192 SPELL_EFFECT_UNCAGE_BATTLEPET
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 193 SPELL_EFFECT_START_PET_BATTLE
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 194 SPELL_EFFECT_194
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 195 SPELL_EFFECT_PLAY_SCENE_SCRIPT_PACKAGE
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 196 SPELL_EFFECT_CREATE_SCENE_OBJECT
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 197 SPELL_EFFECT_CREATE_PERSONAL_SCENE_OBJECT
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 198 SPELL_EFFECT_PLAY_SCENE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 199 SPELL_EFFECT_DESPAWN_SUMMON
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 200 SPELL_EFFECT_HEAL_BATTLEPET_PCT
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 201 SPELL_EFFECT_ENABLE_BATTLE_PETS
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 202 SPELL_EFFECT_APPLY_AREA_AURA_SUMMONS
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 203 SPELL_EFFECT_REMOVE_AURA_2
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 204 SPELL_EFFECT_CHANGE_BATTLEPET_QUALITY
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 205 SPELL_EFFECT_LAUNCH_QUEST_CHOICE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 206 SPELL_EFFECT_ALTER_IETM
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 207 SPELL_EFFECT_LAUNCH_QUEST_TASK
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 208 SPELL_EFFECT_SET_REPUTATION
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 209 SPELL_EFFECT_209
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 210 SPELL_EFFECT_LEARN_GARRISON_BUILDING
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 211 SPELL_EFFECT_LEARN_GARRISON_SPECIALIZATION
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 212 SPELL_EFFECT_REMOVE_AURA_BY_SPELL_LABEL
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 213 SPELL_EFFECT_JUMP_DEST_2
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 214 SPELL_EFFECT_CREATE_GARRISON
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 215 SPELL_EFFECT_UPGRADE_CHARACTER_SPELLS
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 216 SPELL_EFFECT_CREATE_SHIPMENT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 217 SPELL_EFFECT_UPGRADE_GARRISON
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 218 SPELL_EFFECT_218
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 219 SPELL_EFFECT_CREATE_CONVERSATION
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 220 SPELL_EFFECT_ADD_GARRISON_FOLLOWER
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 221 SPELL_EFFECT_ADD_GARRISON_MISSION
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 222 SPELL_EFFECT_CREATE_HEIRLOOM_ITEM
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 223 SPELL_EFFECT_CHANGE_ITEM_BONUSES
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 224 SPELL_EFFECT_ACTIVATE_GARRISON_BUILDING
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 225 SPELL_EFFECT_GRANT_BATTLEPET_LEVEL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 226 SPELL_EFFECT_TRIGGER_ACTION_SET
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 227 SPELL_EFFECT_TELEPORT_TO_LFG_DUNGEON
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 228 SPELL_EFFECT_228
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 229 SPELL_EFFECT_SET_FOLLOWER_QUALITY
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 230 SPELL_EFFECT_230
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 231 SPELL_EFFECT_INCREASE_FOLLOWER_EXPERIENCE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 232 SPELL_EFFECT_REMOVE_PHASE
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 233 SPELL_EFFECT_RANDOMIZE_FOLLOWER_ABILITIES
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 234 SPELL_EFFECT_234
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 235 SPELL_EFFECT_235
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 236 SPELL_EFFECT_GIVE_EXPERIENCE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 237 SPELL_EFFECT_GIVE_RESTED_EXPERIENCE_BONUS
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 238 SPELL_EFFECT_INCREASE_SKILL
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 239 SPELL_EFFECT_END_GARRISON_BUILDING_CONSTRUCTION
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 240 SPELL_EFFECT_GIVE_ARTIFACT_POWER
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 241 SPELL_EFFECT_241
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 242 SPELL_EFFECT_GIVE_ARTIFACT_POWER_NO_BONUS
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),            // 243 SPELL_EFFECT_APPLY_ENCHANT_ILLUSION
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 244 SPELL_EFFECT_LEARN_FOLLOWER_ABILITY
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 245 SPELL_EFFECT_UPGRADE_HEIRLOOM
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 246 SPELL_EFFECT_FINISH_GARRISON_MISSION
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 247 SPELL_EFFECT_ADD_GARRISON_MISSION_SET
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),                // 248 SPELL_EFFECT_FINISH_SHIPMENT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 249 SPELL_EFFECT_FORCE_EQUIP_ITEM
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 250 SPELL_EFFECT_TAKE_SCREENSHOT
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 251 SPELL_EFFECT_SET_GARRISON_CACHE_SIZE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT_AND_DEST),   // D_DEST}, // 252 SPELL_EFFECT_TELEPORT_UNITS
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 253 SPELL_EFFECT_GIVE_HONOR
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.DEST),                // 254 SPELL_EFFECT_JUMP_CHARGE
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),            // 255 SPELL_EFFECT_LEARN_TRANSMOG_SET
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.NONE, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.ITEM),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.NONE),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT),
            new StaticData(SpellEffectImplicitTargetTypes.EXPLICIT, SpellTargetObjectTypes.UNIT)
    };
    private final SpellInfo spellInfo;
    private final ImmunityInfo immunityInfo;
    public SpellEffIndex effectIndex;
    public SpellEffectName effect = SpellEffectName.NONE;
    public AuraType applyAuraName = AuraType.values()[0];

    public int applyAuraPeriod;
    public float basePoints;
    public float realPointsPerLevel;
    public float pointsPerResource;
    public float amplitude;
    public float chainAmplitude;
    public float bonusCoefficient;
    public int miscValue;
    public int miscValueB;
    public Mechanics mechanic;
    public float positionFacing;
    public SpellImplicitTargetInfo targetA = new SpellImplicitTargetInfo();
    public SpellImplicitTargetInfo targetB = new SpellImplicitTargetInfo();
    public SpellRadius radiusEntry;
    public SpellRadius maxRadiusEntry;
    public int chainTargets;
    public int dieSides;
    public int itemType;

    public int triggerSpell;
    public Flag128 spellClassMask;
    public float bonusCoefficientFromAp;
    public ArrayList<Condition> implicitTargetConditions;
    public SpellEffectAttributes effectAttributes = SpellEffectAttributes.None;
    public ScalingInfo scaling = new ScalingInfo();

    public SpellEffectInfo(SpellInfo spellInfo) {
        this(spellInfo, null);
    }

    public SpellEffectInfo(SpellInfo spellInfo, SpellEffect effect) {
        this.spellInfo = spellInfo;

        if (effect != null) {
            effectIndex = SpellEffIndex.values()[effect.getEffectIndex()];
            effect = SpellEffectName.forValue(effect.effect);
            applyAuraName = AuraType.forValue(effect.EffectAura);
            applyAuraPeriod = effect.getEffectAuraPeriod();
            basePoints = effect.getEffectBasePoints();
            realPointsPerLevel = effect.getEffectRealPointsPerLevel();
            pointsPerResource = effect.getEffectPointsPerResource();
            amplitude = effect.getEffectAmplitude();
            chainAmplitude = effect.getEffectChainAmplitude();
            bonusCoefficient = effect.getEffectBonusCoefficient();
            miscValue = effect.getEffectMiscValue1();
            miscValueB = effect.getEffectMiscValue2();
            mechanic = mechanics.forValue(effect.getEffectMechanic());
            positionFacing = effect.getEffectPosFacing();
            targetA = new SpellImplicitTargetInfo(targets.forValue(effect.ImplicitTarget[0]));
            targetB = new SpellImplicitTargetInfo(targets.forValue(effect.ImplicitTarget[1]));
            radiusEntry = CliDB.SpellRadiusStorage.get(effect.EffectRadiusIndex[0]);
            maxRadiusEntry = CliDB.SpellRadiusStorage.get(effect.EffectRadiusIndex[1]);
            chainTargets = effect.getEffectChainTargets();
            dieSides = effect.getEffectDieSides();
            itemType = effect.getEffectItemType();
            triggerSpell = effect.getEffectTriggerSpell();
            spellClassMask = effect.getEffectSpellClassMask();
            bonusCoefficientFromAp = effect.getEffectBonusCoefficientFromAP();
            scaling.scalingClass = effect.getScalingClass();
            scaling.coefficient = effect.getScalingCoefficient();
            scaling.variance = effect.getScalingVariance();
            scaling.resourceCoefficient = effect.getScalingResourceCoefficient();
            effectAttributes = effect.getEffectAttributes();
        }

        implicitTargetConditions = null;

        immunityInfo = new ImmunityInfo();
    }

    public final boolean isTargetingArea() {
        return targetA.isArea() || targetB.isArea();
    }

    public final boolean isAreaAuraEffect() {
        if (effect == SpellEffectName.ApplyAreaAuraParty || effect == SpellEffectName.ApplyAreaAuraRaid || effect == SpellEffectName.ApplyAreaAuraFriend || effect == SpellEffectName.ApplyAreaAuraEnemy || effect == SpellEffectName.ApplyAreaAuraPet || effect == SpellEffectName.ApplyAreaAuraOwner || effect == SpellEffectName.ApplyAreaAuraSummons || effect == SpellEffectName.ApplyAreaAuraPartyNonrandom) {
            return true;
        }

        return false;
    }

    public final boolean isUnitOwnedAuraEffect() {
        return isAreaAuraEffect() || effect == SpellEffectName.ApplyAura || effect == SpellEffectName.ApplyAuraOnPet;
    }

    public final boolean getHasRadius() {
        return radiusEntry != null && (radiusEntry.RadiusMin != 0 || radiusEntry.RadiusMax != 0);
    }

    public final boolean getHasMaxRadius() {
        return maxRadiusEntry != null && (maxRadiusEntry.RadiusMin != 0 || maxRadiusEntry.RadiusMax != 0);
    }

    public final SpellCastTargetFlags getProvidedTargetMask() {
        return spellInfo.getTargetFlagMask(targetA.getObjectType()) | spellInfo.getTargetFlagMask(targetB.getObjectType());
    }

    public final SpellEffectImplicitTargetTypes getImplicitTargetType() {
        return _data[Effect.getValue()].implicitTargetType;
    }

    public final SpellTargetObjectTypes getUsedTargetObjectType() {
        return _data[Effect.getValue()].usedTargetObjectType;
    }

    public final ImmunityInfo getImmunityInfo() {
        return immunityInfo;
    }

    public final boolean isEffect() {
        return effect != 0;
    }

    public final boolean isEffect(SpellEffectName effectName) {
        return effect == effectName;
    }

    public final boolean isAura() {
        return (isUnitOwnedAuraEffect() || effect == SpellEffectName.PersistentAreaAura) && applyAuraName != 0;
    }

    public final boolean isAura(AuraType aura) {
        return isAura() && applyAuraName == aura;
    }


    public final float calcValue(WorldObject caster, Double bp, Unit target, int castItemId) {
        return calcValue(caster, bp, target, castItemId, -1);
    }

    public final float calcValue(WorldObject CASTER, Double bp, Unit target) {
        return calcValue(CASTER, bp, target, 0, -1);
    }

    public final float calcValue(WorldObject CASTER, Double bp) {
        return calcValue(CASTER, bp, null, 0, -1);
    }

    public final float calcValue(WorldObject CASTER) {
        return calcValue(CASTER, null, null, 0, -1);
    }

    public final float calcValue() {
        return calcValue(null, null, null, 0, -1);
    }

    public final float calcValue(WorldObject CASTER, Double bp, Unit target, int castItemId, int itemLevel) {

        return calcValue(out _, CASTER, bp, target, castItemId, itemLevel);
    }


    public final float calcValue(tangible.OutObject<Double> variance, WorldObject CASTER, Double bp, Unit target, int castItemId) {
        return calcValue(variance, CASTER, bp, target, castItemId, -1);
    }

    public final float calcValue(tangible.OutObject<Double> variance, WorldObject CASTER, Double bp, Unit target) {
        return calcValue(variance, CASTER, bp, target, 0, -1);
    }

    public final float calcValue(tangible.OutObject<Double> variance, WorldObject CASTER, Double bp) {
        return calcValue(variance, CASTER, bp, null, 0, -1);
    }

    public final float calcValue(tangible.OutObject<Double> variance, WorldObject CASTER) {
        return calcValue(variance, CASTER, null, null, 0, -1);
    }

    public final float calcValue(tangible.OutObject<Double> variance) {
        return calcValue(variance, null, null, null, 0, -1);
    }

    public final float calcValue(WorldObject caster, YieldResult<Integer> bp, Unit target, YieldResult<Float> variance) {
        var basePointsPerLevel = realPointsPerLevel;
        var basePoints = calcBaseValue(caster, target);
        var value = bp != null && !bp.wasNull() ? bp.get() : basePoints;
        var comboDamage = pointsPerResource;

        Unit casterUnit = null;

        if (caster != null) {
            casterUnit = caster.toUnit();
        }

        if (dieSides != 0) {
            // roll in a range <1;EffectDieSides> as of patch 3.3.3
            if (dieSides == 1)
                value += dieSides;
            else
                value += (dieSides >= 1) ? RandomUtil.randomInt(1, dieSides + 1) : RandomUtil.randomInt(dieSides, 2);
        }

        if (scaling.variance != 0) {
            var delta = Math.abs(scaling.variance * 0.5f);
            var valueVariance = RandomUtil.randomFloat(-delta, delta);
            value += basePoints * valueVariance;
            if (variance != null) {
                variance.set(valueVariance);
            }
        }

        // base amount modification based on spell lvl vs caster lvl
        if (scaling.coefficient != 0.0f) {
            if (scaling.resourceCoefficient != 0) {
                comboDamage = scaling.resourceCoefficient * value;
            }
        } else {
            if (casterUnit != null && basePointsPerLevel != 0.0f) {
                var level = casterUnit.getLevel();

                if (level > spellInfo.getMaxLevel() && spellInfo.getMaxLevel() > 0) {
                    level = spellInfo.getMaxLevel();
                }

                // if base level is greater than spell level, reduce by base level (eg. pilgrims foods)
                level -= Math.max(spellInfo.getBaseLevel(), spellInfo.getSpellLevel());

                if (level < 0) {
                    level = 0;
                }

                value += level * basePointsPerLevel;
            }
        }

        // random damage
        if (casterUnit != null) {
            // bonus amount from combo points
            if (comboDamage != 0) {
                var comboPoints = casterUnit.getComboPoints();

                if (comboPoints != 0) {
                    value += comboDamage * comboPoints;
                }
            }
            value = caster.applyEffectModifiers(spellInfo, effectIndex, value);
        }

        if (caster != null && spellInfo.hasAttribute(SpellAttr8.MASTERY_AFFECTS_POINTS)) {
            Player playerCaster = caster.toPlayer();
            if (playerCaster != null) {
                value += playerCaster.getActivePlayerData().getMastery() * bonusCoefficient;
            }
        }

        if (caster != null)
            value = caster.applyEffectModifiers(spellInfo, effectIndex, value);

        return value;
    }


    public final float calcBaseValue(WorldObject caster, Unit target) {
        if (scaling.coefficient != 0.0f) {
            var level = spellInfo.getSpellLevel();

            if (target != null && spellInfo.isPositiveEffect(effectIndex) && (effect == SpellEffectName.APPLY_AURA)) {
                level = target.getLevel();
            } else if (caster != null && caster.isUnit()) {
                level = caster.toUnit().getLevel();
            }

            if (spellInfo.getBaseLevel() != 0 && !spellInfo.hasAttribute(SpellAttr11.SCALES_WITH_ITEM_LEVEL) && spellInfo.hasAttribute(SpellAttr10.USE_SPELL_BASE_LEVEL_FOR_SCALING)) {
                level = spellInfo.getBaseLevel();
            }

            if (spellInfo.scaling.minScalingLevel != 0 && spellInfo.scaling.minScalingLevel > level) {
                level = spellInfo.scaling.minScalingLevel;
            }

            if (spellInfo.scaling.maxScalingLevel != 0 && spellInfo.scaling.maxScalingLevel < level) {
                level = spellInfo.scaling.maxScalingLevel;
            }

            double tempValue = 0.0f;

            if (level > 0) {
                if (scaling.class == 0) {
                    return 0;
                }

                var effectiveItemLevel = itemLevel != -1 ? (int) itemLevel : 1;

                if (spellInfo.scaling.scalesFromItemLevel != 0 || spellInfo.hasAttribute(SpellAttr11.ScalesWithItemLevel)) {
                    if (spellInfo.scaling.scalesFromItemLevel != 0) {
                        effectiveItemLevel = spellInfo.scaling.scalesFromItemLevel;
                    }

                    if (scaling.class == -8 || scaling.class == -9) {
                        var randPropPoints = CliDB.RandPropPointsStorage.get(effectiveItemLevel);

                        if (randPropPoints == null) {
                            randPropPoints = CliDB.RandPropPointsStorage.get(CliDB.RandPropPointsStorage.GetNumRows() - 1);
                        }

                        tempValue = scaling.class == -8 ? randPropPoints.DamageReplaceStatF : randPropPoints.DamageSecondaryF;
                    } else {
                        tempValue = ItemEnchantmentManager.getRandomPropertyPoints(effectiveItemLevel, itemQuality.Rare, inventoryType.chest, 0);
                    }
                } else {
                    tempValue = CliDB.GetSpellScalingColumnForClass(CliDB.SpellScalingGameTable.GetRow(level), scaling.class);
                }

                if (scaling.class == -7) {
                    var ratingMult = CliDB.CombatRatingsMultByILvlGameTable.GetRow(effectiveItemLevel);

                    if (ratingMult != null) {
                        var itemSparse = CliDB.ItemSparseStorage.get(itemId);

                        if (itemSparse != null) {
                            tempValue *= CliDB.GetIlvlStatMultiplier(ratingMult, itemSparse.inventoryType);
                        }
                    }
                }

                if (scaling.class == -6) {
                    var staminaMult = CliDB.StaminaMultByILvlGameTable.GetRow(effectiveItemLevel);

                    if (staminaMult != null) {
                        var itemSparse = CliDB.ItemSparseStorage.get(itemId);

                        if (itemSparse != null) {
                            tempValue *= CliDB.GetIlvlStatMultiplier(staminaMult, itemSparse.inventoryType);
                        }
                    }
                }
            }

            tempValue *= scaling.coefficient;

            if (tempValue > 0.0f && tempValue < 1.0f) {
                tempValue = 1.0f;
            }

            return tempValue;
        } else {
            var tempValue = basePoints;
            var stat = getScalingExpectedStat();

            if (stat != ExpectedStatType.NONE) {
                if (spellInfo.hasAttribute(SpellAttr0.ScalesWithCreatureLevel)) {
                    stat = ExpectedStatType.CreatureAutoAttackDps;
                }

                // TODO - add expansion and content tuning id args?
                var contentTuningId = spellInfo.getContentTuningId(); // content tuning should be passed as arg, the one stored in SpellInfo is fallback
                var expansion = -2;
                var contentTuning = CliDB.ContentTuningStorage.get(contentTuningId);

                if (contentTuning != null) {
                    expansion = contentTuning.ExpansionID;
                }

                var level = caster != null && caster.isUnit() ? caster.toUnit().getLevel() : 1;
                tempValue = global.getDB2Mgr().EvaluateExpectedStat(stat, level, expansion, 0, UnitClass.NONE) * BasePoints / 100.0f;
            }

            return tempValue;
        }
    }


    public final float calcValueMultiplier(WorldObject CASTER) {
        return calcValueMultiplier(CASTER, null);
    }

    public final float calcValueMultiplier(WorldObject CASTER, Spell spell) {
        var multiplier = amplitude;
        var modOwner = (CASTER != null ? CASTER.getSpellModOwner() : null);

        if (modOwner != null) {
            tangible.RefObject<Double> tempRef_multiplier = new tangible.RefObject<Double>(multiplier);
            modOwner.applySpellMod(spellInfo, SpellModOp.amplitude, tempRef_multiplier, spell);
            multiplier = tempRef_multiplier.refArgValue;
        }

        return multiplier;
    }


    public final double calcDamageMultiplier(WorldObject CASTER) {
        return calcDamageMultiplier(CASTER, null);
    }

    public final float calcDamageMultiplier(WorldObject CASTER, Spell spell) {
        var multiplierPercent = ChainAmplitude * 100.0f;
        var modOwner = (CASTER != null ? CASTER.getSpellModOwner() : null);

        if (modOwner != null) {
            tangible.RefObject<Double> tempRef_multiplierPercent = new tangible.RefObject<Double>(multiplierPercent);
            modOwner.applySpellMod(spellInfo, SpellModOp.chainAmplitude, tempRef_multiplierPercent, spell);
            multiplierPercent = tempRef_multiplierPercent.refArgValue;
        }

        return multiplierPercent / 100.0f;
    }

    public final SpellRadiusRecord getLargestRange() {
        var max = getHasMaxRadius();
        var min = getHasRadius();

        if (!max && !min) {
            return null;
        } else if (max) {
            return maxRadiusEntry;
        } else if (min) {
            return radiusEntry;
        }

        return radiusEntry.RadiusMax > maxRadiusEntry.RadiusMax ? RadiusEntry : maxRadiusEntry;
    }


    public final float calcRadius(WorldObject CASTER) {
        return calcRadius(CASTER, null);
    }

    public final float calcRadius() {
        return calcRadius(null, null);
    }

    public final float calcRadius(WorldObject CASTER, Spell spell) {
        var entry = getLargestRange();

        if (entry == null) {
            return 0.0f;
        }

        var radius = entry.RadiusMin;

        // Client uses max if min is 0
        if (radius == 0.0f) {
            radius = entry.RadiusMax;
        }

        if (CASTER != null) {
            var CASTERUnit = CASTER.toUnit();

            if (CASTERUnit != null) {
                radius += entry.RadiusPerLevel * CASTERUnit.getLevel();
            }

            radius = Math.min(radius, entry.RadiusMax);
            var modOwner = CASTER.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Float> tempRef_radius = new tangible.RefObject<Float>(radius);
                modOwner.applySpellMod(spellInfo, SpellModOp.radius, tempRef_radius, spell);
                radius = tempRef_radius.refArgValue;
            }
        }

        return radius;
    }


    public final SpellCastTargetFlags getMissingTargetMask(boolean srcSet, boolean dstSet) {
        return getMissingTargetMask(srcSet, dstSet, 0);
    }

    public final SpellCastTargetFlags getMissingTargetMask(boolean srcSet) {
        return getMissingTargetMask(srcSet, false, 0);
    }

    public final SpellCastTargetFlags getMissingTargetMask() {
        return getMissingTargetMask(false, false, 0);
    }

    public final SpellCastTargetFlags getMissingTargetMask(boolean srcSet, boolean dstSet, SpellCastTargetFlags mask) {
        var effImplicitTargetMask = spellInfo.getTargetFlagMask(getUsedTargetObjectType());
        var providedTargetMask = SpellCastTargetFlags.forValue(getProvidedTargetMask().getValue() | mask.getValue());

        // remove all flags covered by effect target mask
        if ((boolean) (providedTargetMask.getValue() & SpellCastTargetFlags.UnitMask.getValue())) {
            effImplicitTargetMask = SpellCastTargetFlags.forValue(effImplicitTargetMask.getValue() & ~SpellCastTargetFlags.UnitMask.getValue());
        }

        if ((boolean) (providedTargetMask.getValue() & SpellCastTargetFlags.CorpseMask.getValue())) {
            effImplicitTargetMask = SpellCastTargetFlags.forValue(effImplicitTargetMask.getValue() & ~(SpellCastTargetFlags.UnitMask.getValue() | SpellCastTargetFlags.CorpseMask.getValue()).getValue());
        }

        if ((boolean) (providedTargetMask.getValue() & SpellCastTargetFlags.GameobjectItem.getValue())) {
            effImplicitTargetMask = SpellCastTargetFlags.forValue(effImplicitTargetMask.getValue() & ~(SpellCastTargetFlags.GameobjectItem.getValue() | SpellCastTargetFlags.GAMEOBJECT.getValue().getValue() | SpellCastTargetFlags.item.getValue().getValue()).getValue());
        }

        if ((boolean) (providedTargetMask.getValue() & SpellCastTargetFlags.GAMEOBJECT.getValue())) {
            effImplicitTargetMask = SpellCastTargetFlags.forValue(effImplicitTargetMask.getValue() & ~(SpellCastTargetFlags.GAMEOBJECT.getValue() | SpellCastTargetFlags.GameobjectItem.getValue()).getValue());
        }

        if ((boolean) (providedTargetMask.getValue() & SpellCastTargetFlags.item.getValue())) {
            effImplicitTargetMask = SpellCastTargetFlags.forValue(effImplicitTargetMask.getValue() & ~(SpellCastTargetFlags.item.getValue() | SpellCastTargetFlags.GameobjectItem.getValue()).getValue());
        }

        if (dstSet || (boolean) (providedTargetMask.getValue() & SpellCastTargetFlags.DestLocation.getValue())) {
            effImplicitTargetMask = SpellCastTargetFlags.forValue(effImplicitTargetMask.getValue() & ~SpellCastTargetFlags.DestLocation.getValue());
        }

        if (srcSet || (boolean) (providedTargetMask.getValue() & SpellCastTargetFlags.sourceLocation.getValue())) {
            effImplicitTargetMask = SpellCastTargetFlags.forValue(effImplicitTargetMask.getValue() & ~SpellCastTargetFlags.sourceLocation.getValue());
        }

        return effImplicitTargetMask;
    }

    private ExpectedStatType getScalingExpectedStat() {
        switch (effect) {
            case SchoolDamage:
            case EnvironmentalDamage:
            case HealthLeech:
            case WeaponDamageNoSchool:
            case WeaponDamage:
                return ExpectedStatType.CreatureSpellDamage;
            case Heal:
            case HealMechanical:
                return ExpectedStatType.PlayerHealth;
            case Energize:
            case PowerBurn:
                if (miscValue == powerType.mana.getValue()) {
                    return ExpectedStatType.PlayerMana;
                }

                return ExpectedStatType.NONE;
            case PowerDrain:
                return ExpectedStatType.PlayerMana;
            case ApplyAura:
            case PersistentAreaAura:
            case ApplyAreaAuraParty:
            case ApplyAreaAuraRaid:
            case ApplyAreaAuraPet:
            case ApplyAreaAuraFriend:
            case ApplyAreaAuraEnemy:
            case ApplyAreaAuraOwner:
            case ApplyAuraOnPet:
            case ApplyAreaAuraSummons:
            case ApplyAreaAuraPartyNonrandom:
                switch (applyAuraName) {
                    case PeriodicDamage:
                    case ModDamageDone:
                    case DamageShield:
                    case ProcTriggerDamage:
                    case PeriodicLeech:
                    case ModDamageDoneCreature:
                    case PeriodicHealthFunnel:
                    case ModMeleeAttackPowerVersus:
                    case ModRangedAttackPowerVersus:
                    case ModFlatSpellDamageVersus:
                        return ExpectedStatType.CreatureSpellDamage;
                    case PeriodicHeal:
                    case ModDamageTaken:
                    case ModIncreaseHealth:
                    case SchoolAbsorb:
                    case ModRegen:
                    case ManaShield:
                    case ModHealing:
                    case ModHealingDone:
                    case ModHealthRegenInCombat:
                    case ModMaxHealth:
                    case ModIncreaseHealth2:
                    case SchoolHealAbsorb:
                        return ExpectedStatType.PlayerHealth;
                    case PeriodicManaLeech:
                        return ExpectedStatType.PlayerMana;
                    case ModStat:
                    case ModAttackPower:
                    case ModRangedAttackPower:
                        return ExpectedStatType.PlayerPrimaryStat;
                    case ModRating:
                        return ExpectedStatType.PlayerSecondaryStat;
                    case ModResistance:
                    case ModBaseResistance:
                    case ModTargetResistance:
                    case ModBonusArmor:
                        return ExpectedStatType.ArmorConstant;
                    case PeriodicEnergize:
                    case ModIncreaseEnergy:
                    case ModPowerCostSchool:
                    case ModPowerRegen:
                    case PowerBurn:
                    case ModMaxPower:
                        if (miscValue == powerType.mana.getValue()) {
                            return ExpectedStatType.PlayerMana;
                        }

                        return ExpectedStatType.NONE;
                    default:
                        break;
                }

                break;
            default:
                break;
        }

        return ExpectedStatType.NONE;
    }

    public static class StaticData {
        public SpellEffectimplicitTargetTypes implicitTargetType = SpellEffectImplicitTargetTypes.values()[0]; // defines what target can be added to effect target list if there's no valid target type provided for effect
        public SpellTargetObjectTypes usedTargetObjectType = SpellTargetObjectTypes.values()[0]; // defines valid target object type for spell effect

        public StaticData(SpellEffectImplicitTargetTypes implicittarget, SpellTargetObjectTypes usedtarget) {
            implicitTargetType = implicittarget;
            usedTargetObjectType = usedtarget;
        }
    }


    public final static class ScalingInfo {
        public int scalingClass;
        public float coefficient;
        public float variance;
        public float resourceCoefficient;
    }
}
