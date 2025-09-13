package com.github.azeroth.game.map;

public class PersistentInstanceScriptValueBase {
    protected InstanceScript instance;
    protected String name;
    protected Object value;

    protected PersistentInstanceScriptValueBase(InstanceScript instance, String name, Object value) {
        instance = instance;
        name = name;
        value = value;

        instance.registerPersistentScriptValue(this);
    }

    public final String getName() {
        return name;
    }

    public final UpdateAdditionalSaveDataEvent createEvent() {
        return new UpdateAdditionalSaveDataEvent(name, value);
    }

    public final void loadValue(long value) {
        value = value;
    }

    public final void loadValue(double value) {
        value = value;
    }
}
