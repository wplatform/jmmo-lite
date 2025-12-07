package com.github.azeroth.game.domain.creature;


import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.unit.AnimTier;
import com.github.azeroth.game.domain.unit.SheathState;
import com.github.azeroth.game.domain.unit.UnitStandStateType;
import com.github.azeroth.game.domain.unit.VisibilityDistanceType;
import com.github.azeroth.utils.StringUtil;

public class CreatureAddon {

    public int entry;
    public int pathId;

    public int mount;

    public UnitStandStateType standState;

    public AnimTier animTier;

    public SheathState sheathState;

    public byte pvpFlags;

    public byte visFlags;

    public int emote;

    public short aiAnimKit;

    public short movementAnimKit;

    public short meleeAnimKit;

    public int[] auras;
    public VisibilityDistanceType visibilityDistanceType;

    public CreatureAddon(int entry, int pathId, int mount, byte standState, byte animTier,
                         byte sheathState, byte pvpFlags, byte visFlags, int emote, short aiAnimKit,
                         short movementAnimKit, short meleeAnimKit, String auras, byte visibilityDistanceType) {
        this.entry = entry;
        this.pathId = pathId;
        this.mount = mount;

        if (standState >= UnitStandStateType.values().length) {
            Logs.SQL.error("Creature (Entry: {}) has invalid unit stand state ({}) defined in `creature_template_addon`. Truncated to 0.", entry, standState);
            this.standState = UnitStandStateType.STATE_STAND;
        } else  {
            this.standState = UnitStandStateType.values()[sheathState];
        }

        if (animTier >= AnimTier.values().length) {
            Logs.SQL.error("Creature (Entry: {}) has invalid animation tier ({}) defined in `creature_template_addon`. Truncated to 0.", entry, animTier);
            this.animTier = AnimTier.GROUND;
        } else {
            this.animTier = AnimTier.values()[animTier];
        }

        if (sheathState >= SheathState.values().length) {
            Logs.SQL.error("Creature (Entry: {}) has invalid sheath state ({}) defined in `creature_template_addon`. Truncated to 0.", entry, sheathState);
            this.sheathState = SheathState.UNARMED;
        } else {
            this.sheathState = SheathState.values()[sheathState];
        }
        this.pvpFlags = pvpFlags;
        this.visFlags = visFlags;
        this.emote = emote;
        this.aiAnimKit = aiAnimKit;
        this.movementAnimKit = movementAnimKit;
        this.meleeAnimKit = meleeAnimKit;
        this.auras = StringUtil.distinctTokenizeInts(auras, " ");

        if (visibilityDistanceType >= VisibilityDistanceType.values().length) {
            Logs.SQL.error("Creature (Entry: {}) has invalid visibilityDistanceType ({}) defined in `creature_template_addon`.",
                    entry, visibilityDistanceType);
            this.visibilityDistanceType = VisibilityDistanceType.NORMAL;
        } else {
            this.visibilityDistanceType = VisibilityDistanceType.values()[visibilityDistanceType];
        }
    }
}
