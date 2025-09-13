package com.github.azeroth.game.entity.player;

import com.github.azeroth.game.entity.item.Bag;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.player.enums.PlayerFieldBytes3Offset;
import lombok.Data;

import java.time.Duration;

public interface PlayerDefine {

    int PLAYER_MAX_SKILLS = 128;
    int PLAYER_EXPLORED_ZONES_SIZE = 320;
    int PLAYER_EXPLORED_ZONES_BITS = 32;
    int MAX_ACTION_BUTTONS = 132;
    int MAX_RUNES = 7;
    int MAX_RECHARGING_RUNES = 3;
    int MAX_DRUNKEN = 4;

    ;
    int PLAYER_BYTES_3_OVERRIDE_SPELLS_UINT16_OFFSET = (PlayerFieldBytes3Offset.OVERRIDE_SPELLS_ID.ordinal() / 2);
    int KNOWN_TITLES_SIZE = 6;
    int MAX_TITLE_INDEX = (KNOWN_TITLES_SIZE * 64);        // 4 uint64 fields
    int MAX_TIMERS = 3;
    int DISABLED_MIRROR_TIMER = -1;
    // Size of client completed quests bit map
    int QUESTS_COMPLETED_BITS_SIZE = 1750;
    int MAX_QUEST_COUNTS = 24;
    int MAX_QUEST_OFFSET = 16;
    int INVENTORY_SLOT_BAG_0 = 255;
    int INVENTORY_DEFAULT_SIZE = 20;
    int VISIBLE_ITEM_ENTRY_OFFSET = 0;
    int VISIBLE_ITEM_ENCHANTMENT_OFFSET = 1;
    int MAX_PLAYED_TIME_INDEX = 2;
    // Player summoning auto-decline time (in secs)
    Duration MAX_PLAYER_SUMMON_DELAY = Duration.ofMinutes(2);
    // Maximum money amount : 2^31 - 1
    long MAX_MONEY_AMOUNT = 99999999999L;
    int[] DEFAULT_TALENT_ROW_LEVELS = {15, 30, 45, 60, 75, 90, 100};
    int[] DK_TALENT_ROW_LEVELS = {57, 58, 59, 60, 75, 90, 100};
    int[] DH_TALENT_ROW_LEVELS = {99, 100, 102, 104, 106, 108, 110};
    byte PLAYER_MAX_HONOR_LEVEL = 50;
    byte PLAYER_LEVEL_MIN_HONOR = 110;
    int SPELL_PVP_RULES_ENABLED = 134735;
    float MAX_AREA_SPIRIT_HEALER_RANGE = 20.0f;
    byte PLAYER_BYTES_OFFSET_SKIN_ID = 0;
    byte PLAYER_BYTES_OFFSET_FACE_ID = 1;
    byte PLAYER_BYTES_OFFSET_HAIR_STYLE_ID = 2;
    byte PLAYER_BYTES_OFFSET_HAIR_COLOR_ID = 3;
    byte PLAYER_BYTES_2_OFFSET_CUSTOM_DISPLAY_OPTION = 0; // 3 bytes
    byte PLAYER_BYTES_2_OFFSET_FACIAL_STYLE = 3;
    byte PLAYER_BYTES_3_OFFSET_PARTY_TYPE = 0;
    byte PLAYER_BYTES_3_OFFSET_BANK_BAG_SLOTS = 1;
    byte PLAYER_BYTES_3_OFFSET_GENDER = 2;
    byte PLAYER_BYTES_3_OFFSET_INEBRIATION = 3;
    byte PLAYER_BYTES_4_OFFSET_PVP_TITLE = 0;
    byte PLAYER_BYTES_4_OFFSET_ARENA_FACTION = 1;
    byte PLAYER_FIELD_BYTES_OFFSET_RAF_GRANTABLE_LEVEL = 0;
    byte PLAYER_FIELD_BYTES_OFFSET_ACTION_BAR_TOGGLES = 1;
    byte PLAYER_FIELD_BYTES_OFFSET_LIFETIME_MAX_PVP_RANK = 2;
    byte PLAYER_FIELD_BYTES_OFFSET_NUM_RESPECS = 3;
    byte PLAYER_FIELD_BYTES_2_OFFSET_IGNORE_POWER_REGEN_PREDICTION_MASK = 0;
    byte PLAYER_FIELD_BYTES_2_OFFSET_AURA_VISION = 1;
    byte PLAYER_FIELD_BYTES_2_OFFSET_NUM_BACKPACK_SLOTS = 2;

    int GetBagSize(Bag bag);

    Item GetItemInBag(Bag bag, byte slot);

    @Data
    class Areas {
        int areaID;
        int areaFlag;
        float x1;
        float x2;
        float y1;
        float y2;
    }


}