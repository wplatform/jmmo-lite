package com.github.azeroth.game.entity.item.enums;

// EnumUtils: DESCRIBE THIS
public enum InventoryResult {
    OK,
    CANT_EQUIP_LEVEL_I,  // You must reach level %d to use that item.
    CANT_EQUIP_SKILL,  // You aren't skilled enough to use that item.
    WRONG_SLOT,  // That item does not go in that slot.
    BAG_FULL,  // That bag is full.
    BAG_IN_BAG,  // Can't put non-empty bags in other bags.
    TRADE_EQUIPPED_BAG,  // You can't trade equipped bags.
    AMMO_ONLY,  // Only ammo can go there.
    PROFICIENCY_NEEDED,  // You do not have the required proficiency for that item.
    NO_SLOT_AVAILABLE,  // No equipment slot is available for that item.
    CANT_EQUIP_EVER, // You can never use that item.
    CANT_EQUIP_EVER_2, // You can never use that item.
    NO_SLOT_AVAILABLE_2, // No equipment slot is available for that item.
    TWO_HANDED_EQUIPPED, // Cannot equip that with a two-handed weapon.
    TWO_H_SKILL_NOT_FOUND, // You cannot dual-wield
    WRONG_BAG_TYPE, // That item doesn't go in that container.
    WRONG_BAG_TYPE_2, // That item doesn't go in that container.
    ITEM_MAX_COUNT, // You can't carry any more of those items.
    NO_SLOT_AVAILABLE_3, // No equipment slot is available for that item.
    CANT_STACK, // This item cannot stack.
    NOT_EQUITABLE, // This item cannot be equipped.
    CANT_SWAP, // These items can't be swapped.
    SLOT_EMPTY, // That slot is empty.
    ITEM_NOT_FOUND, // The item was not found.
    DROP_BOUND_ITEM, // You can't drop a soulbound item.
    OUT_OF_RANGE, // Out of range.
    TOO_FEW_TO_SPLIT, // Tried to split more than number in stack.
    SPLIT_FAILED, // Couldn't split those items.
    SPELL_FAILED_REAGENTS_GENERIC, // Missing reagent
    CANT_TRADE_GOLD, // Gold may only be offered by one trader.
    NOT_ENOUGH_MONEY, // You don't have enough money.
    NOT_A_BAG, // Not a bag.
    DESTROY_NONEMPTY_BAG, // You can only do that with empty bags.
    NOT_OWNER, // You don't own that item.
    ONLY_ONE_QUIVER, // You can only equip one quiver.
    NO_BANK_SLOT, // You must purchase that bag slot first
    NO_BANK_HERE, // You are too far away from a bank.
    ITEM_LOCKED, // Item is locked.
    GENERIC_STUNNED, // You are stunned
    PLAYER_DEAD, // You can't do that when you're dead.
    CLIENT_LOCKED_OUT, // You can't do that right now.
    INTERNAL_BAG_ERROR, // Internal Bag Error
    ONLY_ONE_BOLT, // You can only equip one quiver.
    ONLY_ONE_AMMO, // You can only equip one ammo pouch.
    CANT_WRAP_STACKABLE, // Stackable items can't be wrapped.
    CANT_WRAP_EQUIPPED, // Equipped items can't be wrapped.
    CANT_WRAP_WRAPPED, // Wrapped items can't be wrapped.
    CANT_WRAP_BOUND, // Bound items can't be wrapped.
    CANT_WRAP_UNIQUE, // Unique items can't be wrapped.
    CANT_WRAP_BAGS, // Bags can't be wrapped.
    LOOT_GONE, // Already looted
    INV_FULL, // Inventory is full.
    BANK_FULL, // Your bank is full
    VENDOR_SOLD_OUT, // That item is currently sold out.
    BAG_FULL_2, // That bag is full.
    ITEM_NOT_FOUND_2, // The item was not found.
    CANT_STACK_2, // This item cannot stack.
    BAG_FULL_3, // That bag is full.
    VENDOR_SOLD_OUT_2, // That item is currently sold out.
    OBJECT_IS_BUSY, // That object is busy.
    CANT_BE_DISENCHANTED, // Item cannot be disenchanted
    NOT_IN_COMBAT, // You can't do that while in combat
    NOT_WHILE_DISARMED, // You can't do that while disarmed
    BAG_FULL_4, // That bag is full.
    CANT_EQUIP_RANK, // You don't have the required rank for that item
    CANT_EQUIP_REPUTATION, // You don't have the required reputation for that item
    TOO_MANY_SPECIAL_BAGS, // You cannot equip another bag of that type
    LOOT_CANT_LOOT_THAT_NOW, // You can't loot that item now.
    ITEM_UNIQUE_EQUIPPABLE, // You cannot equip more than one of those.
    VENDOR_MISSING_TURNINS, // You do not have the required items for that purchase
    NOT_ENOUGH_HONOR_POINTS, // You don't have enough honor points
    NOT_ENOUGH_ARENA_POINTS, // You don't have enough arena points
    ITEM_MAX_COUNT_SOCKETED, // You have the maximum number of those gems in your inventory or socketed into items.
    MAIL_BOUND_ITEM, // You can't mail soulbound items.
    INTERNAL_BAG_ERROR_2, // Internal Bag Error
    BAG_FULL_5, // That bag is full.
    ITEM_MAX_COUNT_EQUIPPED_SOCKETED, // You have the maximum number of those gems socketed into equipped items.
    ITEM_UNIQUE_EQUIPPABLE_SOCKETED, // You cannot socket more than one of those gems into a single item.
    TOO_MUCH_GOLD, // At gold limit
    NOT_DURING_ARENA_MATCH, // You can't do that while in an arena match
    TRADE_BOUND_ITEM, // You can't trade a soulbound item.
    CANT_EQUIP_RATING, // You don't have the personal, team, or battleground rating required to buy that item
    EVENT_AUTO_EQUIP_BIND_CONFIRM,
    NOT_SAME_ACCOUNT, // Account-bound items can only be given to your own character.
    EQUIP_NONE_3,
    ITEM_MAX_LIMIT_CATEGORY_COUNT_EXCEEDED_IS, // You can only carry %d %s
    ITEM_MAX_LIMIT_CATEGORY_SOCKETED_EXCEEDED_IS, // You can only equip %d |4item:items in the %s category
    SCALING_STAT_ITEM_LEVEL_EXCEEDED, // Your level is too high to use that item
    PURCHASE_LEVEL_TOO_LOW, // You must reach level %d to purchase that item.
    CANT_EQUIP_NEED_TALENT, // You do not have the required talent to equip that.
    ITEM_MAX_LIMIT_CATEGORY_EQUIPPED_EXCEEDED_IS, // You can only equip %d |4item:items in the %s category
    SHAPE_SHIFT_FORM_CANNOT_EQUIP, // Cannot equip item in this form
    ITEM_INVENTORY_FULL_SATCHEL, // Your inventory is full. Your satchel has been delivered to your mailbox.
    SCALING_STAT_ITEM_LEVEL_TOO_LOW, // Your level is too low to use that item
    CANT_BUY_QUANTITY, // You can't buy the specified quantity of that item.
    ITEM_IS_BATTLE_PAY_LOCKED, // Your purchased item is still waiting to be unlocked
    REAGENT_BANK_FULL, // Your reagent bank is full
    REAGENT_BANK_LOCKED,
    WRONG_BAG_TYPE_3, // That item doesn't go in that container.
    CANT_USE_ITEM, // You can't use that item.
    CANT_BE_OBLITERATED,// You can't obliterate that item
    GUILD_BANK_CONJURED_ITEM,// You cannot store conjured items in the guild bank
    BAG_FULL_6,// That bag is full.
    BAG_FULL_7,// That bag is full.
    CANT_BE_SCRAPPED,// You can't scrap that item
    BAG_FULL_8,// That bag is full.
    NOT_IN_PET_BATTLE,// You cannot do that while in a pet battle
    BAG_FULL_9,// That bag is full.
    CANT_DO_THAT_RIGHT_NOW,// You can't do that right now.
    CANT_DO_THAT_RIGHT_NOW_2,// You can't do that right now.
    NOT_IN_NPE,// Not available during the tutorial
    ITEM_COOLDOWN,// Item is not ready yet.
    NOT_IN_RATED_BATTLEGROUND,// You can't do that in a rated battleground.
    EQUIPABLESPELLS_SLOTS_FULL,
    CANT_BE_RECRAFTED,// You can't recraft that itemv
    REAGENTBAG_WRONG_SLOT,// Reagent Bags can only be placed in the reagent bag slot.
    SLOT_ONLY_REAGENTBAG,// Only Reagent Bags can be placed in the reagent bag slot.
    REAGENTBAG_ITEM_TYPE                          // Only Reagents can be placed in Reagent Bags.
}
