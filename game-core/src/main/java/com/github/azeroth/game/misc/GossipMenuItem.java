package com.github.azeroth.game.misc;


import com.github.azeroth.game.domain.gossip.GossipOptionFlag;
import com.github.azeroth.game.domain.gossip.GossipOptionNpc;
import lombok.Data;

@Data
public class GossipMenuItem {
    private int gossipOptionId;
    private int orderIndex;
    private GossipOptionNpc optionNpc;
    private String optionText;
    private int language;
    private GossipOptionFlag flags;
    private Integer gossipNpcOptionId = null;
    private boolean boxCoded;
    private int boxMoney;
    private String boxText;
    private Integer spellId = null;
    private Integer overrideIconId = null;
    // action data
    private int actionMenuId;
    private int actionPoiId;
    // additional scripting identifiers
    private int sender;
    private int action;
}
