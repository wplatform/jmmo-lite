package com.github.azeroth.game.entity.player.model;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.player.enums.DuelState;
import lombok.Data;

@Data
public class DuelInfo {
    private Player opponent;
    private Player initiator;
    private boolean mounted;
    private DuelState state;
    private long startTime;
    private long outOfBoundsTime;

    public DuelInfo(Player opponent, Player initiator, boolean isMounted) {
        setOpponent(opponent);
        setInitiator(initiator);
        setMounted(isMounted);
    }
}
