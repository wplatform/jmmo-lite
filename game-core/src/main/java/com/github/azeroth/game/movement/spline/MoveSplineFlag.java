package com.github.azeroth.game.movement.spline;


import com.github.azeroth.common.EnumFlag;

public class MoveSplineFlag {
    private final EnumFlag<SplineFlag> flags = EnumFlag.of(SplineFlag.None);
    public byte animTier;

    public final boolean isSmooth() {
        return flags.hasFlag(SplineFlag.Catmullrom);
    }

    public final boolean isLinear() {
        return !isSmooth();
    }

    public final boolean hasAllFlags(SplineFlag f) {
        return flags.hasAllFlags(f);
    }

    public final boolean hasFlag(SplineFlag f) {
        return flags.hasFlag(f);
    }

    public final void setFlag(SplineFlag f, boolean set) {
        if (set) {
            setFlag(f);
        } else {
            removeFlag(f);
        }
    }

    public final void setFlag(SplineFlag f) {
        flags.removeFlag(getDisallowedFlagsFor(f)).addFlag(f);
    }

    public final void removeFlag(SplineFlag f) {
        flags.removeFlag(f);
    }

    public final void enableAnimation() {
        setFlag(SplineFlag.Animation);
    }

    public final void enableParabolic() {
        setFlag(SplineFlag.Parabolic);
    }

    public final void enableFlying() {
        setFlag(SplineFlag.Flying);
    }

    public final void enableFalling() {
        setFlag(SplineFlag.Falling);
    }

    public final void enableCatmullRom() {
        setFlag(SplineFlag.Catmullrom);
    }

    public final void enableTransportEnter() {
        setFlag(SplineFlag.TransportEnter);
    }

    public final void enableTransportExit() {
        setFlag(SplineFlag.TransportExit);
    }

    public EnumFlag<SplineFlag> getDisallowedFlagsFor(SplineFlag flag) {
        return switch (flag) {
            case SplineFlag.JumpOrientationFixed -> EnumFlag.of(SplineFlag.OrientationFixed);
            case SplineFlag.Falling -> EnumFlag.of(SplineFlag.Parabolic, SplineFlag.Animation, SplineFlag.Flying);
            case SplineFlag.Flying -> EnumFlag.of(SplineFlag.FallingSlow, SplineFlag.Falling);
            case SplineFlag.OrientationFixed -> EnumFlag.of(SplineFlag.JumpOrientationFixed);
            case SplineFlag.Catmullrom -> EnumFlag.of(SplineFlag.SmoothGroundPath);
            case SplineFlag.TransportEnter -> EnumFlag.of(SplineFlag.TransportExit);
            case SplineFlag.TransportExit -> EnumFlag.of(SplineFlag.TransportEnter);
            case SplineFlag.SmoothGroundPath -> EnumFlag.of(SplineFlag.Steering);
            case SplineFlag.Animation -> EnumFlag.of(SplineFlag.Falling, SplineFlag.Parabolic, SplineFlag.FallingSlow, SplineFlag.FadeObject);
            case SplineFlag.Parabolic -> EnumFlag.of(SplineFlag.Falling, SplineFlag.Animation, SplineFlag.FallingSlow, SplineFlag.FadeObject);
            case SplineFlag.FadeObject -> EnumFlag.of(SplineFlag.Falling, SplineFlag.Parabolic, SplineFlag.FallingSlow, SplineFlag.Animation);
            case SplineFlag.Steering -> EnumFlag.of(SplineFlag.SmoothGroundPath);
            default -> EnumFlag.of(SplineFlag.None);
        };
    }

    public int getFlag() {
        return flags.getFlag();
    }
}
