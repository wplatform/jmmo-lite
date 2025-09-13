package com.github.azeroth.game.networking.packet.spell;


import java.util.ArrayList;


public class SpellEmpowerStageUpdate extends ServerPacket {
    public ObjectGuid castID = ObjectGuid.EMPTY;
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public int timeRemaining;
    public boolean unk;
    public ArrayList<Integer> remainingStageDurations = new ArrayList<>();

    public SpellEmpowerStageUpdate() {
        super(ServerOpcode.SpellEmpowerUpdate);
    }

    @Override
    public void write() {
        this.writeGuid(castID);
        this.writeGuid(caster);
        this.write(timeRemaining);
        this.write((int) remainingStageDurations.size());
        this.write(unk);

        for (var stageDuration : remainingStageDurations) {
            this.write(stageDuration);
        }
    }
}
