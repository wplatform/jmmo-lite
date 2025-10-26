package com.github.azeroth.game.entity.player;

import com.github.azeroth.defines.BattlegroundQueueTypeId;
import lombok.Data;

@Data
public class BgBattlegroundQueueIdRec {
    public BattlegroundQueueTypeId bgQueueTypeId;
    public int invitedToInstance;
    public int joinTime;
    public boolean mercenary;

}
