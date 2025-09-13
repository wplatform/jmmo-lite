package com.github.azeroth.game.networking.packet.pet;


import java.util.ArrayList;


public class PetSpells extends ServerPacket {
    public ObjectGuid petGUID = ObjectGuid.EMPTY;
    public short creatureFamily;
    public short specialization;
    public int timeLimit;
    public reactStates reactState = ReactStates.values()[0];
    public commandStates commandState = CommandStates.values()[0];
    public byte flag;

    public int[] actionButtons = new int[10];

    public ArrayList<Integer> actions = new ArrayList<>();
    public ArrayList<PetSpellCooldown> cooldowns = new ArrayList<>();
    public ArrayList<PetspellHistory> spellHistory = new ArrayList<>();

    public PetSpells() {
        super(ServerOpcode.PetSpellsMessage);
    }

    @Override
    public void write() {
        this.writeGuid(petGUID);
        this.writeInt16(creatureFamily);
        this.writeInt16(specialization);
        this.writeInt32(timeLimit);
        this.writeInt16((short) ((byte) commandState.getValue() | (flag << 16)));
        this.writeInt8((byte) reactState.getValue());

        for (var actionButton : actionButtons) {
            this.writeInt32(actionButton);
        }

        this.writeInt32(actions.size());
        this.writeInt32(cooldowns.size());
        this.writeInt32(spellHistory.size());

        for (var action : actions) {
            this.writeInt32(action);
        }

        for (var cooldown : cooldowns) {
            this.writeInt32(cooldown.spellID);
            this.writeInt32(cooldown.duration);
            this.writeInt32(cooldown.categoryDuration);
            this.writeFloat(cooldown.modRate);
            this.writeInt16(cooldown.category);
        }

        for (var history : spellHistory) {
            this.writeInt32(history.categoryID);
            this.writeInt32(history.recoveryTime);
            this.writeFloat(history.chargeModRate);
            this.writeInt8(history.consumedCharges);
        }
    }
}
