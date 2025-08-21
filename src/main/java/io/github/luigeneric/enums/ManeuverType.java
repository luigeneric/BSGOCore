package io.github.luigeneric.enums;

public enum ManeuverType
{
    Pulse,
    Teleport,
    Rest,
    Warp,
    Directional,
    Launch,
    Rotation,
    Flip,
    Turn,
    Follow,
    DirectionalWithoutRoll,
    TurnQweasd,
    TurnToDirectionStrikes,
    TurnByPitchYawStrikes,
    TargetLaunch;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static ManeuverType forValue(byte value)
    {
        return values()[value];
    }
}
