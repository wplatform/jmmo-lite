package com.github.azeroth.game.networking.packet.talent;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class ActiveGlyphs extends ServerPacket {
    public ArrayList<GlyphBinding> glyphs = new ArrayList<>();
    public boolean isFullUpdate;

    public ActiveGlyphs() {
        super(ServerOpcode.ActiveGlyphs);
    }

    @Override
    public void write() {
        this.writeInt32(glyphs.size());

        for (var glyph : glyphs) {
            glyph.write(this);
        }

        this.writeBit(isFullUpdate);
        this.flushBits();
    }
}
