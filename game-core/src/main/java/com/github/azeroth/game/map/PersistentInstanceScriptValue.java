package com.github.azeroth.game.map;

class PersistentInstanceScriptValue<T> extends PersistentInstanceScriptValueBase {
    public PersistentInstanceScriptValue(InstanceScript instance, String name, T value) {
        super(instance, name, value);
    }

    public final PersistentInstanceScriptValue<T> setValue(T value) {
        value = value;
        notifyValueChanged();

        return this;
    }

    private void notifyValueChanged() {
        instance.getInstance().updateInstanceLock(createEvent());
    }

    private void loadValue(T value) {
        value = value;
    }
}
