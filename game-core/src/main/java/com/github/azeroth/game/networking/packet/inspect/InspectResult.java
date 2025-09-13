package com.github.azeroth.game.networking.packet.inspect;


import java.util.ArrayList;


public class InspectResult extends ServerPacket {
    public PlayerModeldisplayInfo displayInfo;
    public ArrayList<SHORT> glyphs = new ArrayList<>();
    public ArrayList<SHORT> talents = new ArrayList<>();
    public Array<SHORT> pvpTalents = new Array<SHORT>(PlayerConst.MaxPvpTalentSlots, 0);
    public InspectguildData guildData = null;
    public Array<PVPbracketData> bracket = new Array<PVPBracketData>(7,
    default);
    public Integer azeriteLevel = null;
    public int itemLevel;
    public int lifetimeHK;
    public int honorLevel;
    public short todayHK;
    public short yesterdayHK;
    public byte lifetimeMaxRank;
    public traitInspectInfo talentTraits = new traitInspectInfo();

    public InspectResult() {
        super(ServerOpcode.InspectResult);
        displayInfo = new PlayerModelDisplayInfo();
    }

    @Override
    public void write() {
        displayInfo.write(this);
        this.writeInt32(glyphs.size());
        this.writeInt32(talents.size());
        this.writeInt32(pvpTalents.size());
        this.writeInt32(itemLevel);
        this.writeInt8(lifetimeMaxRank);
        this.writeInt16(todayHK);
        this.writeInt16(yesterdayHK);
        this.writeInt32(lifetimeHK);
        this.writeInt32(honorLevel);

        for (var i = 0; i < glyphs.size(); ++i) {
            this.writeInt16(glyphs.get(i));
        }

        for (var i = 0; i < talents.size(); ++i) {
            this.writeInt16(talents.get(i));
        }

        for (var i = 0; i < pvpTalents.size(); ++i) {
            this.writeInt16(pvpTalents.get(i));
        }

        this.writeBit(guildData != null);
        this.writeBit(azeriteLevel != null);
        this.flushBits();

        for (var bracket : bracket) {
            bracket.write(this);
        }

        if (guildData != null) {
            guildData.getValue().write(this);
        }

        if (azeriteLevel != null) {
            this.writeInt32(azeriteLevel.intValue());
        }

        talentTraits.write(this);
    }
}
