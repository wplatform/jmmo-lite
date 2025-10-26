package com.github.azeroth.defines;

public enum LootRollIneligibilityReason {
    None                    ,
    UnusableByClass         , // Your class may not roll need on this item.
    MaxUniqueItemCount      , // You already have the maximum amount of this item.
    CannotBeDisenchanted    , // This item may not be disenchanted.
    EnchantingSkillTooLow   , // You do not have an Enchanter of skill %d in your group.
    NeedDisabled            , // Need rolls are disabled for this item.
    OwnBetterItem             // You already have a powerful version of this item.
}
