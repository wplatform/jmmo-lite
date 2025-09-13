package com.github.azeroth.game.spell;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.player.enums.TradeSlots;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.networking.packet.spell.SpellCastRequest;
import com.github.azeroth.game.networking.packet.spell.SpellTargetData;
import com.github.azeroth.game.networking.packet.spell.TargetLocation;
import com.github.azeroth.game.spell.enums.SpellCastTargetFlag;
import lombok.Getter;

import java.util.Objects;

public class SpellCastTargets {
    private final String strTarget;
    @Getter
    private final EnumFlag<SpellCastTargetFlag> targetMask = EnumFlag.of(SpellCastTargetFlag.NONE);
    // objects (can be used at spell creating and after Update at casting)
    private WorldObject objectTarget;
    private Item itemTarget;
    // object GUID/etc, can be used always
    private ObjectGuid objectTargetGuid = ObjectGuid.EMPTY;
    private ObjectGuid itemTargetGuid = ObjectGuid.EMPTY;
    private int itemTargetEntry;
    private SpellDestination src;
    private SpellDestination dst;
    private float pitch;
    private float speed;

    public SpellCastTargets() {
        strTarget = "";

        src = new SpellDestination();
        dst = new SpellDestination();
    }

    public SpellCastTargets(Unit caster, SpellCastRequest spellCastRequest) {
        targetMask.setFlags(spellCastRequest.target.flags);
        objectTargetGuid = spellCastRequest.target.unit;
        itemTargetGuid = spellCastRequest.target.item;
        strTarget = spellCastRequest.target.name;

        src = new SpellDestination();
        dst = new SpellDestination();

        if (spellCastRequest.target.srcLocation != null) {
            src.transportGuid = spellCastRequest.target.srcLocation.transport;
            Position pos;

            if (!src.transportGuid.isEmpty()) {
                pos = src.transportOffset;
            } else {
                pos = src.position;
            }

            pos.relocate(spellCastRequest.target.srcLocation.location);

            if (spellCastRequest.target.orientation != null) {
                pos.setO(spellCastRequest.target.orientation);
            }
        }

        if (spellCastRequest.target.dstLocation != null) {
            dst.transportGuid = spellCastRequest.target.dstLocation.transport;
            Position pos;

            if (!dst.transportGuid.isEmpty()) {
                pos = dst.transportOffset;
            } else {
                pos = dst.position;
            }

            pos.relocate(spellCastRequest.target.dstLocation.location);

            if (spellCastRequest.target.orientation != null) {
                pos.setO(spellCastRequest.target.orientation);
            }
        }

        setPitch(spellCastRequest.missileTrajectory.pitch);
        setSpeed(spellCastRequest.missileTrajectory.speed);

        update(caster);
    }

    public final ObjectGuid getItemTargetGuid() {
        return itemTargetGuid;
    }

    public final Item getItemTarget() {
        return itemTarget;
    }

    public final void setItemTarget(Item value) {
        if (value == null) {
            return;
        }

        itemTarget = value;
        itemTargetGuid = value.getGUID();
        itemTargetEntry = value.getEntry();
        targetMask.addFlag(SpellCastTargetFlag.ITEM);
    }

    public final int getItemTargetEntry() {
        return itemTargetEntry;
    }

    public final boolean getHasSrc() {
        return targetMask.hasFlag(SpellCastTargetFlag.SOURCE_LOCATION);
    }

    public final boolean getHasDst() {
        return targetMask.hasFlag(SpellCastTargetFlag.DEST_LOCATION);
    }

    public final boolean getHasTraj() {
        return getSpeed() != 0;
    }

    public final float getPitch() {
        return pitch;
    }

    public final void setPitch(float value) {
        pitch = value;
    }

    public final float getSpeed() {
        return speed;
    }

    public final void setSpeed(float value) {
        speed = value;
    }

    public final float getDist2d() {
        return src.position.getExactDist2D(dst.position);
    }

    public final float getSpeedXY() {
        return (float) (getSpeed() * Math.cos(getPitch()));
    }

    public final float getSpeedZ() {
        return (float) (getSpeed() * Math.sin(getPitch()));
    }

    public final String getTargetString() {
        return strTarget;
    }

    public final ObjectGuid getUnitTargetGUID() {
        if (objectTargetGuid.isUnit()) {
            return objectTargetGuid;
        }

        return ObjectGuid.EMPTY;
    }

    public final Unit getUnitTarget() {
        if (objectTarget instanceof Unit) {
            return objectTarget.toUnit();
        }

        return null;
    }

    public final void setUnitTarget(Unit value) {
        if (value == null) {
            return;
        }

        objectTarget = value;
        objectTargetGuid = value.getGUID();
        targetMask.addFlag(SpellCastTargetFlag.UNIT);
    }

    public final GameObject getGOTarget() {
        if (objectTarget != null) {
            return objectTarget.toGameObject();
        }

        return null;
    }

    public final void setGOTarget(GameObject value) {
        if (value == null) {
            return;
        }

        objectTarget = value;
        objectTargetGuid = value.getGUID();
        targetMask.addFlag(SpellCastTargetFlag.GAME_OBJECT);
    }

    public final ObjectGuid getCorpseTargetGUID() {
        if (objectTargetGuid.isCorpse()) {
            return objectTargetGuid;
        }

        return ObjectGuid.EMPTY;
    }

    public final Corpse getCorpseTarget() {
        if (objectTarget != null) {
            return objectTarget.toCorpse();
        }

        return null;
    }

    public final WorldObject getObjectTarget() {
        return objectTarget;
    }

    public final ObjectGuid getObjectTargetGUID() {
        return objectTargetGuid;
    }

    public final SpellDestination getSrc() {
        return src;
    }

    public final void setSrc(WorldObject wObj) {
        src = new SpellDestination(wObj);
        targetMask.addFlag(SpellCastTargetFlag.SOURCE_LOCATION);
    }

    private void setSrc(Position pos) {
        src = new SpellDestination(pos);
        targetMask.addFlag(SpellCastTargetFlag.SOURCE_LOCATION);
    }

    public final Position getSrcPos() {
        return src.position;
    }

    public final SpellDestination getDst() {
        return dst;
    }

    public final void setDst(SpellDestination value) {
        dst = value;
        targetMask.addFlag(SpellCastTargetFlag.DEST_LOCATION);
    }

    public final void setDst(Position pos) {
        dst = new SpellDestination(pos);
        targetMask.addFlag(SpellCastTargetFlag.DEST_LOCATION);
    }

    public final void setDst(WorldObject wObj) {
        dst = new SpellDestination(wObj);
        targetMask.addFlag(SpellCastTargetFlag.DEST_LOCATION);
    }

    public final void setDst(SpellCastTargets spellTargets) {
        dst = spellTargets.dst;
        targetMask.addFlag(SpellCastTargetFlag.DEST_LOCATION);
    }

    public final WorldLocation getDstPos() {
        return dst.position;
    }

    private ObjectGuid getGOTargetGUID() {
        if (objectTargetGuid.isAnyTypeGameObject()) {
            return objectTargetGuid;
        }

        return ObjectGuid.EMPTY;
    }

    public final void write(SpellTargetData data) {
        data.flags = targetMask.getFlag();

        if (targetMask.hasAnyFlag(SpellCastTargetFlag.UNIT, SpellCastTargetFlag.CORPSE_ALLY, SpellCastTargetFlag.GAME_OBJECT, SpellCastTargetFlag.CORPSE_ENEMY, SpellCastTargetFlag.UNIT_MINIPET)) {
            data.unit = objectTargetGuid;
        }

        if (targetMask.hasAnyFlag(SpellCastTargetFlag.ITEM, SpellCastTargetFlag.TRADE_ITEM) && itemTarget != null) {
            data.item = itemTarget.getGUID();
        }

        if (targetMask.hasFlag(SpellCastTargetFlag.SOURCE_LOCATION)) {
            TargetLocation target = new TargetLocation();
            target.transport = src.transportGuid; // relative position guid here - transport for example

            if (!src.transportGuid.isEmpty()) {
                target.location = src.transportOffset;
            } else {
                target.location = src.position;
            }

            data.srcLocation = target;
        }

        if (targetMask.hasFlag(SpellCastTargetFlag.DEST_LOCATION)) {
            TargetLocation target = new TargetLocation();
            target.transport = dst.transportGuid; // relative position guid here - transport for example

            if (!dst.transportGuid.isEmpty()) {
                target.location = dst.transportOffset;
            } else {
                target.location = dst.position;
            }

            data.dstLocation = target;
        }

        if (targetMask.hasFlag(SpellCastTargetFlag.STRING)) {
            data.name = strTarget;
        }
    }

    public final void removeObjectTarget() {
        objectTarget = null;
        objectTargetGuid.clear();
        targetMask.removeFlag(SpellCastTargetFlag.UNIT_MASK, SpellCastTargetFlag.CORPSE_MASK, SpellCastTargetFlag.GAMEOBJECT_MASK);
    }

    public final void setTradeItemTarget(Player caster) {
        itemTargetGuid = ObjectGuid.TRADE_ITEM;
        itemTargetEntry = 0;
        targetMask.addFlag(SpellCastTargetFlag.TRADE_ITEM);

        update(caster);
    }

    public final void updateTradeSlotItem() {
        if (itemTarget != null && targetMask.hasFlag(SpellCastTargetFlag.TRADE_ITEM)) {
            itemTargetGuid = itemTarget.getGUID();
            itemTargetEntry = itemTarget.getEntry();
        }
    }

    public final void modSrc(Position pos) {
        src.relocate(pos);
    }

    public final void removeSrc() {
        targetMask.removeFlag(SpellCastTargetFlag.SOURCE_LOCATION);
    }

    public final void setDst(float x, float y, float z, float orientation) {
        setDst(x, y, z, orientation, 0xFFFFFFFF);
    }

    public final void setDst(float x, float y, float z, float orientation, int mapId) {
        dst = new SpellDestination(x, y, z, orientation, mapId);
        targetMask.addFlag(SpellCastTargetFlag.DEST_LOCATION);
    }

    public final void modDst(Position pos) {
        dst.relocate(pos);
    }

    public final void modDst(SpellDestination spellDest) {
        dst = spellDest;
    }

    public final void removeDst() {
        targetMask.removeFlag(SpellCastTargetFlag.DEST_LOCATION);
    }

    public final void update(WorldObject caster) {

        objectTarget = (Objects.equals(objectTargetGuid, caster.getGUID())) ? caster : caster.getWorldContext().getWorldObject(caster,objectTargetGuid);

        itemTarget = null;

        if (caster instanceof Player player) {

            if (targetMask.hasFlag(SpellCastTargetFlag.ITEM)) {
                itemTarget = player.getItemByGuid(itemTargetGuid);
            } else if (targetMask.hasFlag(SpellCastTargetFlag.TRADE_ITEM)) {
                if (Objects.equals(itemTargetGuid, ObjectGuid.TRADE_ITEM)) // here it is not guid but slot. Also prevents hacking slots
                {
                    var pTrade = player.getTradeData();

                    if (pTrade != null) {
                        itemTarget = pTrade.getTraderData().getItem(TradeSlots.NON_TRADED);
                    }
                }
            }

            if (itemTarget != null) {
                itemTargetEntry = itemTarget.getEntry();
            }
        }

        // update positions by transport move
        if (getHasSrc() && !src.transportGuid.isEmpty()) {
            var transport = caster.getWorldContext().getWorldObject(caster,src.transportGuid);

            if (transport != null) {
                src.position.relocate(transport.getLocation());
                src.position.relocateOffset(src.transportOffset);
            }
        }

        if (getHasDst() && !dst.transportGuid.isEmpty()) {
            var transport = caster.getWorldContext().getWorldObject(caster,dst.transportGuid);

            if (transport != null) {
                dst.position.relocate(transport.getLocation());
                dst.position.relocateOffset(dst.transportOffset);
            }
        }
    }

    public final void setTargetFlag(SpellCastTargetFlag flag) {
        targetMask.addFlag(flag);
    }

    private void setSrc(float x, float y, float z) {
        src = new SpellDestination(x, y, z);
        targetMask.addFlag(SpellCastTargetFlag.SOURCE_LOCATION);
    }
}
