package com.github.azeroth.game.networking.packet.worldstate;


import java.util.ArrayList;


public class InitWorldStates extends ServerPacket {
    private final ArrayList<WorldStateInfo> worldstates = new ArrayList<>();
    public int areaID;
    public int subareaID;
    public int mapID;

    public InitWorldStates() {
        super(ServerOpcode.InitWorldStates);
    }

    @Override
    public void write() {
        this.writeInt32(mapID);
        this.writeInt32(areaID);
        this.writeInt32(subareaID);

        this.writeInt32(worldstates.size());

        for (var wsi : worldstates) {
            this.writeInt32(wsi.variableID);
            this.writeInt32(wsi.value);
        }
    }

    public final void addState(WorldStates variableID, int value) {
        addState((int) variableID.getValue(), value);
    }

    public final void addState(int variableID, int value) {
        worldstates.add(new WorldStateInfo(variableID, (int) value));
    }

    public final void addState(int variableID, int value) {
        worldstates.add(new WorldStateInfo((int) variableID, value));
    }

    public final void addState(WorldStates variableID, boolean value) {
        addState((int) variableID.getValue(), value);
    }

    public final void addState(int variableID, boolean value) {
        worldstates.add(new WorldStateInfo(variableID, value ? 1 : 0));
    }

    private final static class WorldStateInfo {
        public final int variableID;
        public final int value;

        public WorldStateInfo() {
        }
        public WorldStateInfo(int variableID, int value) {
            variableID = variableID;
            value = value;
        }

        public WorldStateInfo clone() {
            WorldStateInfo varCopy = new WorldStateInfo();

            varCopy.variableID = this.variableID;
            varCopy.value = this.value;

            return varCopy;
        }
    }
}
