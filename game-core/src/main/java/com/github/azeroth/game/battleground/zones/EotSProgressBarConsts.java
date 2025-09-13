package com.github.azeroth.game.battleground.zones;


import java.util.HashMap;

enum EotSProgressBarConsts {
    PointMaxCapturersCount(5),
    PointRadius(70),
    ProgressBarDontShow(0),
    progressBarShow(1),
    progressBarPercentGrey(40),
    ProgressBarStateMiddle(50),
    ProgressBarHordeControlled(0),
    ProgressBarNeutralLow(30),
    ProgressBarNeutralHigh(70),
    ProgressBarAliControlled(100);

    public static final int SIZE = Integer.SIZE;
    private static HashMap<Integer, EotSProgressBarConsts> mappings;
    private int intValue;

    private EotSProgressBarConsts(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static HashMap<Integer, EotSProgressBarConsts> getMappings() {
        if (mappings == null) {
            synchronized (EotSProgressBarConsts.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, EotSProgressBarConsts>();
                }
            }
        }
        return mappings;
    }

    public static EotSProgressBarConsts forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
