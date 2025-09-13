package com.github.azeroth.game.entity.transport;

class SetTransportAutoCycleBetweenStopFrames extends GameObjectTypeBase.CustomCommand {
    private final boolean on;

    public SetTransportAutoCycleBetweenStopFrames(boolean on) {
        on = on;
    }

    @Override
    public void execute(GameObjectTypeBase type) {
        var transport = (transport) type;

        if (transport != null) {
            transport.setAutoCycleBetweenStopFrames(on);
        }
    }
}
