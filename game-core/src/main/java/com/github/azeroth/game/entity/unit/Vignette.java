package com.github.azeroth.game.entity.unit;

import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.map.WmoLocation;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.vignette.VignetteData;
import com.github.azeroth.game.networking.packet.vignette.VignetteDataSet;
import lombok.Data;

@Data
public class Vignette {
    private ObjectGuid guid;
    private ObjectGuid objectGuid;
    private Position position;
    private VignetteEntry data;
    private int zoneID;
    private int wmoGroupID;
    private int wmoDoodadPlacementID;
    private float healthPercent;
    private boolean needUpdate;

    public Vignette(VignetteEntry entry, WorldObject owner) {
        this.data = entry;
        this.objectGuid = owner.getGUID();
        this.position = owner.getLocation();
        this.zoneID = owner.getZoneId();
        this.healthPercent = 1.0f;
        updatePosition(owner);

        if (owner instanceof Unit) {
            updateHealth((Unit) owner);
        }
    }

    public void updatePosition(WorldObject owner) {
        this.position = owner.getLocation();
        WmoLocation wmoLocation = owner.getCurrentWmo();
        if (wmoLocation != null) {
            this.wmoGroupID = wmoLocation.getGroupId();
            this.wmoDoodadPlacementID = wmoLocation.getUniqueId();
        }
    }

    public void updateHealth(Unit unit) {
        this.healthPercent = (float) unit.getHealth() / unit.getMaxHealth();
    }

    public void fillPacket(VignetteDataSet dataSet) {
        dataSet.getIDs().add(guid);
        VignetteData data = new VignetteData();
        data.setObjGUID(objectGuid);
        data.setPosition(new Vector3(position.getX(),position.getY(),position.getZ()));
        data.setVignetteID(this.data.ID);
        data.setZoneID(zoneID);
        data.setWMOGroupID(wmoGroupID);
        data.setWMODoodadPlacementID(wmoDoodadPlacementID);
        data.setHealthPercent(healthPercent);
        dataSet.getData().add(data);
    }

    public static Vignette create(VignetteEntry entry, WorldObject owner) {
        Vignette vignette = new Vignette(entry, owner);
        // ... existing creation logic ...
        return vignette;
    }

    public void update(WorldObject owner) {
        updatePosition(owner);
        if (owner instanceof Unit) {
            updateHealth((Unit) owner);
        }
        // ... existing update logic ...
    }

    public void remove(WorldObject owner) {
        // ... existing removal logic ...
    }

    public static boolean canSee(Player player, Vignette vignette) {
        // ... existing visibility check logic ...
        return true;
    }

}
