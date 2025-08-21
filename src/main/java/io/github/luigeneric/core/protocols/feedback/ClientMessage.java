package io.github.luigeneric.core.protocols.feedback;

enum ClientMessage
{
    UiElementShown,
    UiElementHidden;

    public static final int SIZE = Integer.SIZE;

    public int getValue()
    {
        return this.ordinal();
    }

    public static ClientMessage forValue(int value)
    {
        return values()[value];
    }
}
