package io.github.luigeneric.enums;

public enum CreatingCause
{
    AlreadyExists,
    JumpIn;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }
}
