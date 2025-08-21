package io.github.luigeneric.enums;

public enum AvatarItem
{
    Race,
    Sex,
    HumanHead,
    HumanFace,
    HumanHands,
    HumanGlasses,
    HumanHelmet,
    HumanHair,
    HumanHairColor,
    HumanSuit,
    HumanBeard,
    HumanBeardColor,
    CylonHead,
    CylonHeadSkin,
    CylonArms,
    CylonArmsSkin,
    CylonBody,
    CylonBodySkin,
    CylonLegs,
    CylonLegsSkin;

    public static final int SIZE = Byte.SIZE;

    public byte getValue()
    {
        return (byte) this.ordinal();
    }

    public static AvatarItem forValue(final int value)
    {
        return forValue((byte) value);
    }

    public static AvatarItem forValue(final byte value)
    {
        return values()[value];
    }
}

