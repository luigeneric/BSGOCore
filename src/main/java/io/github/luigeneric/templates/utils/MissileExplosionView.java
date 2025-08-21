package io.github.luigeneric.templates.utils;

public enum MissileExplosionView
{
    Standard(0),
    Nuclear(1),
    NuclearMini(2),
    Torpedo(4);

    public final byte value;
    MissileExplosionView(int value)
    {
        this.value = (byte) value;
    }
}
