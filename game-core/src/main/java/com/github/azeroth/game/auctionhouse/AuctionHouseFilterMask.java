package com.github.azeroth.game.auctionhouse;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuctionHouseFilterMask implements EnumFlag.FlagValue{
    None                        (0x0000),
    UncollectedOnly             (0x0002),
    UsableOnly                  (0x0004),
    CurrentExpansionOnly        (0x0008),
    UpgradesOnly                (0x0010),
    ExactMatch                  (0x0020),
    PoorQuality                 (0x0040),
    CommonQuality               (0x0080),
    UncommonQuality             (0x0100),
    RareQuality                 (0x0200),
    EpicQuality                 (0x0400),
    LegendaryQuality            (0x0800),
    ArtifactQuality             (0x1000),
    LegendaryCraftedItemOnly    (0x2000);

    private final int value;


}
