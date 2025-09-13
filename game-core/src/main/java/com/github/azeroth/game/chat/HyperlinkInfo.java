package com.github.azeroth.game.chat;

class HyperlinkInfo {
    public String tail;
    public Hyperlinkcolor color = new hyperlinkColor();
    public String tag;
    public String data;
    public String text;


    public HyperlinkInfo(String t, int c, String tag, String data) {
        this(t, c, tag, data, null);
    }

    public HyperlinkInfo(String t, int c, String tag) {
        this(t, c, tag, null, null);
    }

    public HyperlinkInfo(String t, int c) {
        this(t, c, null, null, null);
    }

    public HyperlinkInfo(String t) {
        this(t, 0, null, null, null);
    }

    public HyperlinkInfo() {
        this(null, 0, null, null, null);
    }

    public HyperlinkInfo(String t, int c, String tag, String data, String text) {
        tail = t;
        color = new hyperlinkColor(c);
        tag = tag;
        data = data;
        text = text;
    }
}
