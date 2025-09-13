package com.github.azeroth.game.domain.gossip;


import com.github.azeroth.game.domain.condition.ConditionsReference;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GossipMenus {
    public int menuId;

    public int textId;

    public ConditionsReference conditions;
}
