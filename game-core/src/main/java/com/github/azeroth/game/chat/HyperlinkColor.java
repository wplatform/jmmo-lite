package com.github.azeroth.game.chat;

final class HyperlinkColor {
    public byte R;
    public byte G;
    public byte B;
    public byte A;

    public hyperlinkColor() {
    }

    public hyperlinkColor(int c) {
        R = (byte) (c >>> 16);
        G = (byte) (c >>> 8);
        B = (byte) c;
        A = (byte) (c >>> 24);
    }

    public HyperlinkColor clone() {
        HyperlinkColor varCopy = new hyperlinkColor();

        varCopy.R = this.R;
        varCopy.G = this.G;
        varCopy.B = this.B;
        varCopy.A = this.A;

        return varCopy;
    }
}
