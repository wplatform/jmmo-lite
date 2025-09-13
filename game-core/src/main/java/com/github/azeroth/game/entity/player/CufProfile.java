package com.github.azeroth.game.entity.player;


public class CufProfile {
    private String profileName;
    private short frameHeight;
    private short frameWidth;
    private byte sortBy;
    private byte healthText;
    // LeftAlign, TopAlight, BottomAlign
    private byte topPoint;
    private byte bottomPoint;
    private byte leftPoint;
    // leftOffset, TopOffset and BottomOffset
    private short topOffset;
    private short bottomOffset;
    private short leftOffset;
    private BitSet boolOptions;

    public CufProfile() {
        setBoolOptions(new bitSet(CUFBoolOptions.BoolOptionsCount.getValue()));
    }

    public CufProfile(String name, short frameHeight, short frameWidth, byte sortBy, byte healthText, int boolOptions, byte topPoint, byte bottomPoint, byte leftPoint, short topOffset, short bottomOffset, short leftOffset) {
        setProfileName(name);

        setBoolOptions(new bitSet(new int[]{boolOptions}));

        setFrameHeight(frameHeight);
        setFrameWidth(frameWidth);
        setSortBy(sortBy);
        setHealthText(healthText);
        setTopPoint(topPoint);
        setBottomPoint(bottomPoint);
        setLeftPoint(leftPoint);
        setTopOffset(topOffset);
        setBottomOffset(bottomOffset);
        setLeftOffset(leftOffset);
    }

    public final String getProfileName() {
        return profileName;
    }

    public final void setProfileName(String value) {
        profileName = value;
    }

    public final short getFrameHeight() {
        return frameHeight;
    }

    public final void setFrameHeight(short value) {
        frameHeight = value;
    }

    public final short getFrameWidth() {
        return frameWidth;
    }

    public final void setFrameWidth(short value) {
        frameWidth = value;
    }

    public final byte getSortBy() {
        return sortBy;
    }

    public final void setSortBy(byte value) {
        sortBy = value;
    }

    public final byte getHealthText() {
        return healthText;
    }

    public final void setHealthText(byte value) {
        healthText = value;
    }

    public final byte getTopPoint() {
        return topPoint;
    }

    public final void setTopPoint(byte value) {
        topPoint = value;
    }

    public final byte getBottomPoint() {
        return bottomPoint;
    }

    public final void setBottomPoint(byte value) {
        bottomPoint = value;
    }

    public final byte getLeftPoint() {
        return leftPoint;
    }

    public final void setLeftPoint(byte value) {
        leftPoint = value;
    }

    public final short getTopOffset() {
        return topOffset;
    }

    public final void setTopOffset(short value) {
        topOffset = value;
    }

    public final short getBottomOffset() {
        return bottomOffset;
    }

    public final void setBottomOffset(short value) {
        bottomOffset = value;
    }

    public final short getLeftOffset() {
        return leftOffset;
    }

    public final void setLeftOffset(short value) {
        leftOffset = value;
    }

    public final BitSet getBoolOptions() {
        return boolOptions;
    }

    public final void setBoolOptions(BitSet value) {
        boolOptions = value;
    }

    public final void setOption(CUFBoolOptions opt, byte arg) {
        getBoolOptions().set(opt.getValue(), arg != 0);
    }

    public final boolean getOption(CUFBoolOptions opt) {
        return getBoolOptions().Get(opt.getValue());
    }

    public final long getUlongOptionValue() {
        var array = new int[1];
        getBoolOptions().CopyTo(array, 0);

        return (long) array[0];
    }

    // More fields can be added to BoolOptions without changing DB schema (up to 32, currently 27)
}
