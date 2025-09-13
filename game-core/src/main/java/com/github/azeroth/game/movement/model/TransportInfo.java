package com.github.azeroth.game.movement.model;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import lombok.Data;

@Data
public class TransportInfo {

    public ObjectGuid guid;
    public Position pos;
    public byte seat;
    public int time;
    public int prevTime;
    public int vehicleId;

    void reset() {
        guid.clear();
        pos.relocate(0.0f, 0.0f, 0.0f, 0.0f);
        seat = -1;
        time = 0;
        prevTime = 0;
        vehicleId = 0;
    }
}
