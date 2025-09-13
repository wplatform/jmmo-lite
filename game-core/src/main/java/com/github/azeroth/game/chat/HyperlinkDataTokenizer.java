package com.github.azeroth.game.chat;


class HyperlinkDataTokenizer {
    private final StringArguments arg;
    private final boolean allowEmptyTokens;

    public HyperlinkDataTokenizer(String arg) {
        this(arg, false);
    }


    public HyperlinkDataTokenizer(String arg, boolean allowEmptyTokens) {
        arg = new StringArguments(arg);
        allowEmptyTokens = allowEmptyTokens;
    }

    public final boolean isEmpty() {
        return arg.isEmpty();
    }

    public final boolean tryConsumeTo(tangible.OutObject<Byte> val) {
        if (isEmpty()) {
            val.outArgValue = 0;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextByte(":");

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<SHORT> val) {
        if (isEmpty()) {
            val.outArgValue = 0;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextUInt16(":");

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<Integer> val) {
        if (isEmpty()) {
            val.outArgValue = 0;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextUInt32(":");

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<Long> val) {
        if (isEmpty()) {
            val.outArgValue = 0;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextUInt64(":");

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<Byte> val) {
        if (isEmpty()) {
            val.outArgValue = 0;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextSByte(":");

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<SHORT> val) {
        if (isEmpty()) {
            val.outArgValue = 0;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextInt16(":");

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<Integer> val) {
        if (isEmpty()) {
            val.outArgValue = 0;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextInt32(":");

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<Long> val) {
        if (isEmpty()) {
            val.outArgValue = 0;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextInt64(":");

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<Float> val) {
        if (isEmpty()) {
            val.outArgValue = 0F;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextSingle(":");

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<ObjectGuid> val) {
        if (isEmpty()) {
            val.outArgValue = null;

            return allowEmptyTokens;
        }

        val.outArgValue = ObjectGuid.FromString(arg.NextString(":"));

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<String> val) {
        if (isEmpty()) {
            val.outArgValue = null;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextString(":");

        return true;
    }

    public final boolean tryConsumeTo(tangible.OutObject<Boolean> val) {
        if (isEmpty()) {
            val.outArgValue = false;

            return allowEmptyTokens;
        }

        val.outArgValue = arg.NextBoolean(":");

        return true;
    }
}
