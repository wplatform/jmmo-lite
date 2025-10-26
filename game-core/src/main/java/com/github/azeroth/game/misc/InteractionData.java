package com.github.azeroth.game.misc;

import com.github.azeroth.game.domain.object.ObjectGuid;
import lombok.Data;

@Data
public class InteractionData {
    private ObjectGuid sourceGuid;

    private int trainerId;

    private int playerChoiceId;

    public final void reset() {
        sourceGuid.clear();
        trainerId = 0;
        playerChoiceId = 0;
    }
}
