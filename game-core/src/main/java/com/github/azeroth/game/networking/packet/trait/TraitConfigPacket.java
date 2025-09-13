package com.github.azeroth.game.networking.packet.trait;


import com.github.azeroth.game.entity.TraitConfig;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.HashMap;


public class TraitConfigPacket {
    public int ID;
    public TraitConfigtype type = TraitConfigType.values()[0];
    public int chrSpecializationID = 0;
    public TraitcombatConfigFlags combatConfigFlags = TraitCombatConfigFlags.values()[0];
    public int localIdentifier; // Local to specialization
    public int skillLineID;
    public int traitSystemID;
    public HashMap<Integer, HashMap<Integer, TraitEntryPacket>> entries = new HashMap<Integer, HashMap<Integer, TraitEntryPacket>>();
    public String name = "";

    public traitConfigPacket() {
    }

    public traitConfigPacket(TraitConfig ufConfig) {
        ID = ufConfig.ID;
        type = TraitConfigType.forValue((int) ufConfig.type);
        chrSpecializationID = ufConfig.chrSpecializationID;
        combatConfigFlags = TraitCombatConfigFlags.forValue((int) ufConfig.combatConfigFlags);
        localIdentifier = ufConfig.localIdentifier;
        skillLineID = (int) (int) ufConfig.skillLineID;
        traitSystemID = ufConfig.traitSystemID;

        for (var ufEntry : ufConfig.entries) {
            addEntry(new TraitEntryPacket(ufEntry));
        }

        name = ufConfig.name;
    }

    public final void addEntry(TraitEntryPacket packet) {
        TValue innerDict;
        if (!(entries.containsKey(packet.traitNodeID) && (innerDict = entries.get(packet.traitNodeID)) == innerDict)) {
            innerDict = new HashMap<Integer, TraitEntryPacket>();
            entries.put(packet.traitNodeID, innerDict);
        }

        innerDict[packet.TraitNodeEntryID] = packet;
    }

    public final void read(WorldPacket data) {
        ID = data.readInt32();
        type = TraitConfigType.forValue(data.readInt32());
        var entriesCount = data.readUInt32();

        switch (type) {
            case Combat:
                chrSpecializationID = data.readInt32();
                combatConfigFlags = TraitCombatConfigFlags.forValue(data.readInt32());
                localIdentifier = data.readInt32();

                break;
            case Profession:
                skillLineID = data.readUInt32();

                break;
            case Generic:
                traitSystemID = data.readInt32();

                break;
            default:
                break;
        }

        for (var i = 0; i < entriesCount; ++i) {
            TraitEntryPacket traitEntry = new TraitEntryPacket();
            traitEntry.read(data);
            addEntry(traitEntry);
        }

        var nameLength = data.readBit(9);
        name = data.readString(nameLength);
    }

    public final void write(WorldPacket data) {
        data.writeInt32(ID);
        data.writeInt32(type.getValue());
        data.writeInt32(entries.size());

        switch (type) {
            case Combat:
                data.writeInt32(chrSpecializationID);
                data.writeInt32(combatConfigFlags.getValue());
                data.writeInt32(localIdentifier);

                break;
            case Profession:
                data.writeInt32(skillLineID);

                break;
            case Generic:
                data.writeInt32(traitSystemID);

                break;
            default:
                break;
        }

        for (var tkvp : entries.entrySet()) {
            for (var traitEntry : tkvp.getValue().values()) {
                traitEntry.write(data);
            }
        }

        data.writeBits(name.getBytes().length, 9);
        data.flushBits();

        data.writeString(name);
    }
}
