package com.github.azeroth.game.networking.packet.scenario;

import com.github.azeroth.game.scenario.ScenarioPOI;

import java.util.ArrayList;


final class ScenarioPOIData {
    public int criteriaTreeID;
    public ArrayList<ScenarioPOI> scenarioPOIs;

    public ScenarioPOIData clone() {
        ScenarioPOIData varCopy = new ScenarioPOIData();

        varCopy.criteriaTreeID = this.criteriaTreeID;
        varCopy.scenarioPOIs = this.scenarioPOIs;

        return varCopy;
    }
}
