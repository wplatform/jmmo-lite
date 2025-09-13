package com.github.azeroth.game.group;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.reference.Reference;

public class GroupReference extends Reference<PlayerGroup, Player, GroupReference> {
    private byte iSubGroup;

    public GroupReference() {
        iSubGroup = 0;
    }

    public final byte getSubGroup() {
        return iSubGroup;
    }

    public final void setSubGroup(byte value) {
        iSubGroup = value;
    }

    @Override
    protected void insert(GroupReference thisNode) {
        refManager().linkMember(thisNode);
    }

    @Override
    protected GroupReference self() {
        return this;
    }
}
