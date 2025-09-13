package com.github.azeroth.game.entity.gobject;


class GameObjectTypeBase {
    protected final GameObject owner;

    public GameObjectTypeBase(GameObject owner) {
        owner = owner;
    }

    public void update(int diff) {
    }

    public void onStateChanged(GOState oldState, GOState newState) {
    }

    public void onRelocated() {
    }

    public static class CustomCommand {
        public void execute(GameObjectTypeBase type) {
        }
    }
}
