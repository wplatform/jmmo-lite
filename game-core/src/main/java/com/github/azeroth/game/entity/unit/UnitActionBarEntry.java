package com.github.azeroth.game.entity.unit;

public class UnitActionBarEntry {
    public int packedData;

    public UnitActionBarEntry() {
        packedData = (int) ActiveStates.disabled.getValue() << 24;
    }

    public static int MAKE_UNIT_ACTION_BUTTON(int action, int type) {
        return (action | (type << 24));
    }

    public static int UNIT_ACTION_BUTTON_ACTION(int packedData) {
        return (packedData & 0x00FFFFFF);
    }

    public static int UNIT_ACTION_BUTTON_TYPE(int packedData) {
        return ((packedData & 0xFF000000) >>> 24);
    }

    public final ActiveStates getActiveState() {
        return ActiveStates.forValue(UNIT_ACTION_BUTTON_TYPE(packedData));
    }

    public final int getAction() {
        return UNIT_ACTION_BUTTON_ACTION(packedData);
    }

    public final void setAction(int action) {
        packedData = (packedData & 0xFF000000) | UNIT_ACTION_BUTTON_ACTION(action);
    }

    public final boolean isActionBarForSpell() {
        var type = getActiveState();

        return type == ActiveStates.disabled || type == ActiveStates.enabled || type == ActiveStates.Passive;
    }

    public final void setActionAndType(int action, ActiveStates type) {
        packedData = MAKE_UNIT_ACTION_BUTTON(action, (int) type.getValue());
    }

    public final void setType(ActiveStates type) {
        packedData = MAKE_UNIT_ACTION_BUTTON(UNIT_ACTION_BUTTON_ACTION(packedData), (int) type.getValue());
    }
}
