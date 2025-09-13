package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.WorldPacket;

public class MonsterSplineUnknown901 {
    public Array<Inner> data = new Array<Inner>(16);

    public final void write(WorldPacket data) {
        for (var unkInner : data) {
            data.writeInt32(unkInner.unknown_1);
            unkInner.visual.write(data);
            data.writeInt32(unkInner.unknown_4);
        }
    }

    public final static class Inner {
        public int unknown_1;
        public SpellCastvisual visual = new spellCastVisual();
        public int unknown_4;

        public Inner clone() {
            Inner varCopy = new Inner();

            varCopy.unknown_1 = this.unknown_1;
            varCopy.visual = this.visual;
            varCopy.unknown_4 = this.unknown_4;

            return varCopy;
        }
    }
}
