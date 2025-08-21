package io.github.luigeneric.core.protocols.login;

public enum LoginError
{
    Unknown,
    AlreadyConnected,
    WrongProtocol,
    WrongSession,
    WrongUserId,
    WrongPlayerId,
    WrongPlayerName;

    public static final int SIZE = Integer.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static LoginError forValue(byte value)
    {
        return values()[value];
    }
}
