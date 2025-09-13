package com.github.azeroth.game.group;


public class GroupRefManager extends RefManager<PlayerGroup, player> {

    public final GroupReference getFirst() {
        return (GroupReference) super.getFirst();
    }
}
