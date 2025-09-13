package com.github.azeroth.game.chat;

public class VariantArgAttribute extends Attribute {
    private Class[] types;

    public VariantArgAttribute(class...types) {
        setTypes(types);
    }

    public final Class[] getTypes() {
        return types;
    }

    public final void setTypes(Class[] value) {
        types = value;
    }
}
