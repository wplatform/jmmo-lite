package com.github.azeroth.game.movement.spline;


import com.github.azeroth.common.EnumFlag;

public class MoveSplineFlag {
    public EnumFlag<SplineFlag> flags = EnumFlag.of(SplineFlag.None);
    public byte animTier;

    public MoveSplineFlag() {
    }

    public MoveSplineFlag(SplineFlag f) {
        flags = EnumFlag.of(f);
    }

    public MoveSplineFlag(MoveSplineFlag f) {
        flags = f.flags;
        animTier = f.animTier;
    }

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


    public final void setUnsetFlag(SplineFlag f) {
        setUnsetFlag(f, true);
    }


    public final void setUnsetFlag(SplineFlag f, boolean set) {
        if (set) {
            flags.addFlag(f);
        } else {
            flags.removeFlag(f);
        }
    }

    public final void enableAnimation() {
        flags.removeFlag(SplineFlag.Falling, SplineFlag.Parabolic, SplineFlag.FallingSlow, SplineFlag.FadeObject).addFlag(SplineFlag.Animation);
    }

    public final void enableParabolic() {
        flags.removeFlag(SplineFlag.Falling, SplineFlag.Animation, SplineFlag.FallingSlow, SplineFlag.FadeObject).addFlag(SplineFlag.Parabolic);
    }

    public final void enableFlying() {
        flags.removeFlag(SplineFlag.Falling).addFlag(SplineFlag.Flying);
    }

    public final void enableFalling() {
        flags.removeFlag(SplineFlag.Parabolic, SplineFlag.Animation).addFlag(SplineFlag.Flying);
    }

    public final void enableCatmullRom() {
        flags.removeFlag(SplineFlag.SmoothGroundPath).addFlag(SplineFlag.Catmullrom);
    }

    public final void enableTransportEnter() {
        flags.removeFlag(SplineFlag.TransportExit).addFlag(SplineFlag.TransportEnter);
    }

    public final void enableTransportExit() {
        flags.removeFlag(SplineFlag.TransportEnter).addFlag(SplineFlag.TransportExit);
    }

}
