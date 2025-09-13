package com.github.azeroth.game.misc;

public class InteractionData {
    private ObjectGuid sourceGuid = ObjectGuid.EMPTY;

    private int trainerId;

    private int playerChoiceId;

    public final ObjectGuid getSourceGuid() {
        return sourceGuid;
    }

    public final void setSourceGuid(ObjectGuid value) {
        sourceGuid = value;
    }


    public final int getTrainerId() {
        return trainerId;
    }


    public final void setTrainerId(int value) {
        trainerId = value;
    }


    public final int getPlayerChoiceId() {
        return playerChoiceId;
    }


    public final void setPlayerChoiceId(int value) {
        playerChoiceId = value;
    }

    public final void reset() {
        getSourceGuid().clear();
        setTrainerId(0);
    }
}
