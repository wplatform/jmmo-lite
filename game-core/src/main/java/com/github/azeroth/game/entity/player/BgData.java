package com.github.azeroth.game.entity.player;


import java.util.ArrayList;


public class BgData {
    private int bgInstanceId;
    //  when player is teleported to BG - (it is Battleground's GUID)
    private BattlegroundTypeId bgTypeId = BattlegroundTypeId.values()[0];
    private ArrayList<ObjectGuid> bgAfkReporter = new ArrayList<>();
    private byte bgAfkReportedCount;
    private long bgAfkReportedTimer;
    private int bgTeam;
    private int mountSpell;
    private int[] taxiPath = new int[2];
    private WorldLocation joinPos;

    public BgData() {
        setBgTypeId(BattlegroundTypeId.NONE);
        clearTaxiPath();
        setJoinPos(new worldLocation());
    }

    public final int getBgInstanceId() {
        return bgInstanceId;
    }

    public final void setBgInstanceId(int value) {
        bgInstanceId = value;
    }

    public final BattlegroundTypeId getBgTypeId() {
        return bgTypeId;
    }

    public final void setBgTypeId(BattlegroundTypeId value) {
        bgTypeId = value;
    }

    public final ArrayList<ObjectGuid> getBgAfkReporter() {
        return bgAfkReporter;
    }

    public final void setBgAfkReporter(ArrayList<ObjectGuid> value) {
        bgAfkReporter = value;
    }

    public final byte getBgAfkReportedCount() {
        return bgAfkReportedCount;
    }

    public final void setBgAfkReportedCount(byte value) {
        bgAfkReportedCount = value;
    }

    public final long getBgAfkReportedTimer() {
        return bgAfkReportedTimer;
    }

    public final void setBgAfkReportedTimer(long value) {
        bgAfkReportedTimer = value;
    }

    public final int getBgTeam() {
        return bgTeam;
    }

    public final void setBgTeam(int value) {
        bgTeam = value;
    }

    public final int getMountSpell() {
        return mountSpell;
    }

    public final void setMountSpell(int value) {
        mountSpell = value;
    }

    public final int[] getTaxiPath() {
        return taxiPath;
    }

    public final void setTaxiPath(int[] value) {
        taxiPath = value;
    }

    public final WorldLocation getJoinPos() {
        return joinPos;
    }

    public final void setJoinPos(WorldLocation value) {
        joinPos = value;
    }

    public final void clearTaxiPath() {
        getTaxiPath()[1] = 0;
        getTaxiPath()[0] = getTaxiPath()[1];
    }

    public final boolean hasTaxiPath() {
        return getTaxiPath()[0] != 0 && getTaxiPath()[1] != 0;
    }
}
