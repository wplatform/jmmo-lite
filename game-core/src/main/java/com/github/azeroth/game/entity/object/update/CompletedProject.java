package com.github.azeroth.game.entity.object.update;

public final class CompletedProject extends UpdateMaskObject{

    int projectID;
    long firstCompleted;
    int completionCount;

    public CompletedProject() {
        super(4);
    }

    @Override
    public void clearChangesMask() {

    }
}
