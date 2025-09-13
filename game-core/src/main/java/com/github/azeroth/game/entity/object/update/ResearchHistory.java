package com.github.azeroth.game.entity.object.update;

import java.util.List;

public final class ResearchHistory extends UpdateMaskObject{

    List<CompletedProject> completedProjects;

    public ResearchHistory() {
        super(2);
    }

    @Override
    public void clearChangesMask() {
    }
}
