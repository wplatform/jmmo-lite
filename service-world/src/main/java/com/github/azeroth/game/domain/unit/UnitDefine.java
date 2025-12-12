package com.github.azeroth.game.domain.unit;


public interface UnitDefine {

    int WORLD_TRIGGER = 12999;

    int ARTIFACTS_ALL_WEAPONS_GENERAL_WEAPON_EQUIPPED_PASSIVE = 197886;
    int SPELL_DH_DOUBLE_JUMP = 196055;

    int DISPLAYID_HIDDEN_MOUNT = 73200;

    int WARMODE_ENLISTED_SPELL_OUTSIDE = 269083;

    int MAX_AGGRO_RESET_TIME = 10; // in seconds
    float MAX_AGGRO_RADIUS = 45.0f;  // yards


    // synonyms
    int UNIT_MOD_STAT_START = UnitMod.STAT_STRENGTH.ordinal();
    int UNIT_MOD_STAT_END = UnitMod.STAT_INTELLECT.ordinal() + 1;
    int UNIT_MOD_RESISTANCE_START = UnitMod.ARMOR.ordinal();
    int UNIT_MOD_RESISTANCE_END = UnitMod.RESISTANCE_ARCANE.ordinal() + 1;
    int UNIT_MOD_POWER_START = UnitMod.MANA.ordinal();
    int UNIT_MOD_POWER_END = UnitMod.ESSENCE.ordinal() + 1;


    int MAX_COMBAT_RATING = 32;


    int MAX_MOVE_TYPE = 9;


    int CURRENT_FIRST_NON_MELEE_SPELL = 1;
    int CURRENT_MAX_SPELL = 4;


    // for clearing special attacks
    int REACTIVE_TIMER_START = 4000;

    ;;

    // delay time next attack to prevent client attack animation problems
    int ATTACK_DISPLAY_DELAY = 200;
    // max distance for detection targets by player
    float MAX_PLAYER_STEALTH_DETECT_RANGE = 30.0f;


    float MIN_MELEE_REACH = 2.0f;
    float NOMINAL_MELEE_RANGE = 5.0f;
    float MELEE_RANGE = (NOMINAL_MELEE_RANGE - MIN_MELEE_REACH * 2); //center to center for players
    float BASE_MIN_DAMAGE = 1.0f;
    float BASE_MAX_DAMAGE = 2.0f;
    int BASE_ATTACK_TIME = 2000;

    int MAX_EQUIPMENT_ITEMS = 3;

    int MAX_DECLINED_NAME_CASES = 5;
    float[] BASE_MOVE_SPEED = {
            2.5f,                  // MOVE_WALK
            7.0f,                  // MOVE_RUN
            4.5f,                  // MOVE_RUN_BACK
            4.722222f,             // MOVE_SWIM
            2.5f,                  // MOVE_SWIM_BACK
            3.141594f,             // MOVE_TURN_RATE
            7.0f,                  // MOVE_FLIGHT
            4.5f,                  // MOVE_FLIGHT_BACK
            3.14f                  // MOVE_PITCH_RATE
    };
    float[] PLAYER_BASE_MOVE_SPEED = {
            2.5f,                  // MOVE_WALK
            7.0f,                  // MOVE_RUN
            4.5f,                  // MOVE_RUN_BACK
            4.722222f,             // MOVE_SWIM
            2.5f,                  // MOVE_SWIM_BACK
            3.141594f,             // MOVE_TURN_RATE
            7.0f,                  // MOVE_FLIGHT
            4.5f,                  // MOVE_FLIGHT_BACK
            3.14f                  // MOVE_PITCH_RATE
    };


}
