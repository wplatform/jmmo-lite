package com.github.azeroth.game.networking.packet.spell;


import java.util.ArrayList;
import java.util.HashMap;


public class SpellEmpowerStart extends ServerPacket {
    public int spellID;
    public ObjectGuid castID = ObjectGuid.EMPTY;
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public SpellCastvisual visual = new spellCastVisual();
    public int duration;
    public int firstStageDuration;
    public int finalStageDuration;
    public ArrayList<ObjectGuid> targets = new ArrayList<>();
    public HashMap<Byte, Integer> stageDurations = new HashMap<Byte, Integer>();
    public SpellChannelStartInterruptimmunities immunities = null;
    public SpellhealPrediction healPrediction = null;

    public SpellEmpowerStart() {
        super(ServerOpcode.SpellEmpowerStart);
    }

    @Override
    public void write() {
        this.writeGuid(castID);
        this.writeGuid(caster);
        this.writeInt32((int) targets.size());
        this.write(spellID);
        visual.write(this);
        this.write(duration);
        this.write(firstStageDuration);
        this.write(finalStageDuration);
        this.writeInt32((int) stageDurations.size());

        for (var target : targets) {
            this.write(target);
        }

        for (var val : stageDurations.values()) {
            this.write(val);
        }

        this.write(immunities != null);
        this.write(healPrediction != null);

        if (immunities != null) {
            immunities.getValue().write(this);
        }

        if (healPrediction != null) {
            healPrediction.getValue().write(this);
        }
    }
}
