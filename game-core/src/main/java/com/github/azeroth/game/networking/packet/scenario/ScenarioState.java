package com.github.azeroth.game.networking.packet.scenario;


import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

class ScenarioState extends ServerPacket {
    public int scenarioID;
    public int currentStep = -1;

    public int difficultyID;

    public int waveCurrent;

    public int waveMax;

    public int timerDuration;
    public ArrayList<criteriaProgressPkt> criteriaProgress = new ArrayList<>();
    public ArrayList<BonusObjectiveData> bonusObjectives = new ArrayList<>();

    public ArrayList<Integer> pickedSteps = new ArrayList<>();
    public ArrayList<ScenarioSpellUpdate> spells = new ArrayList<>();
    public ObjectGuid playerGUID = ObjectGuid.EMPTY;
    public boolean scenarioComplete = false;

    public ScenarioState() {
        super(ServerOpcode.ScenarioState);
    }

    @Override
    public void write() {
        this.writeInt32(scenarioID);
        this.writeInt32(currentStep);
        this.writeInt32(difficultyID);
        this.writeInt32(waveCurrent);
        this.writeInt32(waveMax);
        this.writeInt32(timerDuration);
        this.writeInt32(criteriaProgress.size());
        this.writeInt32(bonusObjectives.size());
        this.writeInt32(pickedSteps.size());
        this.writeInt32(spells.size());
        this.writeGuid(playerGUID);

        for (var i = 0; i < pickedSteps.size(); ++i) {
            this.writeInt32(pickedSteps.get(i));
        }

        this.writeBit(scenarioComplete);
        this.flushBits();

        for (var progress : criteriaProgress) {
            progress.write(this);
        }

        for (var bonusObjective : bonusObjectives) {
            bonusObjective.write(this);
        }

        for (var spell : spells) {
            spell.write(this);
        }
    }
}
