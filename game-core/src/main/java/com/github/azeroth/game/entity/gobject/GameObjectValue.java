package com.github.azeroth.game.entity.gobject;


import com.github.azeroth.game.domain.transport.TransportAnimation;

import java.util.ArrayList;


public final class GameObjectValue {
    public transport transport = new transport();

    public fishinghole fishingHole = new fishinghole();

    public building building = new building();

    public capturePoint capturePoint = new capturePoint();

    public GameObjectValue clone() {
        GameObjectValue varCopy = new gameObjectValue();

        varCopy.transport = this.transport;
        varCopy.fishingHole = this.fishingHole;
        varCopy.building = this.building;
        varCopy.capturePoint = this.capturePoint;

        return varCopy;
    }

    //11 GAMEOBJECT_TYPE_TRANSPORT
    public final static class transport {
        public int pathProgress;
        public TransportAnimation animationInfo;
        public int currentSeg;
        public ArrayList<Integer> stopFrames;
        public int stateUpdateTimer;

        public transport clone() {
            transport varCopy = new transport();

            varCopy.pathProgress = this.pathProgress;
            varCopy.animationInfo = this.animationInfo;
            varCopy.currentSeg = this.currentSeg;
            varCopy.stopFrames = this.stopFrames;
            varCopy.stateUpdateTimer = this.stateUpdateTimer;

            return varCopy;
        }
    }

    //25 GAMEOBJECT_TYPE_FISHINGHOLE
    public final static class fishinghole {
        public int maxOpens;

        public fishinghole clone() {
            fishinghole varCopy = new fishinghole();

            varCopy.maxOpens = this.maxOpens;

            return varCopy;
        }
    }

    //33 GAMEOBJECT_TYPE_DESTRUCTIBLE_BUILDING
    public final static class building {
        public int health;
        public int maxHealth;

        public building clone() {
            building varCopy = new building();

            varCopy.health = this.health;
            varCopy.maxHealth = this.maxHealth;

            return varCopy;
        }
    }

    //42 GAMEOBJECT_TYPE_CAPTURE_POINT
    public final static class capturePoint {
        public int lastTeamCapture;
        public BattlegroundCapturePointstate state = BattlegroundCapturePointState.values()[0];
        public int assaultTimer;

        public capturePoint clone() {
            capturePoint varCopy = new capturePoint();

            varCopy.lastTeamCapture = this.lastTeamCapture;
            varCopy.state = this.state;
            varCopy.assaultTimer = this.assaultTimer;

            return varCopy;
        }
    }
}
