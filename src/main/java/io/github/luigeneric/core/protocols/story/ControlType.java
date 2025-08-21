package io.github.luigeneric.core.protocols.story;

enum ControlType
{
    TargetEnemy((byte) 19),
    TargetAlly(((byte) 19) + 1),
    ThrottleBar(((byte) 19) + 2),
    Boosters(((byte) 19) + 3),
    Camera(((byte) 19) + 4),
    SystemMap(((byte) 19) + 5),
    MatchSpeed(((byte) 19) + 6),
    Follow(((byte) 19) + 7),
    Turn(((byte) 19) + 8),
    Strafe(((byte) 19) + 9),
    Roll(((byte) 19) + 10),
    Tournament((byte) 51),
    Weapon1((byte) 101),
    Weapon2(((byte) 101) + 1),
    Weapon3(((byte) 101) + 2),
    Weapon4(((byte) 101) + 3),
    Weapon5(((byte) 101) + 4),
    Weapon6(((byte) 101) + 5),
    Weapon7(((byte) 101) + 6),
    Weapon8(((byte) 101) + 7),
    Weapon9(((byte) 101) + 8),
    Weapon10(((byte) 101) + 9),
    Weapon11(((byte) 101) + 10),
    Weapon12(((byte) 101) + 11),
    WEAPON_MAX(((byte) 101) + 12),
    Ability1(((byte) 101) + 13),
    Ability2(((byte) 101) + 14),
    Ability3(((byte) 101) + 15),
    Ability4(((byte) 101) + 16),
    Ability5(((byte) 101) + 17),
    Ability6(((byte) 101) + 18),
    Ability7(((byte) 101) + 19),
    Ability8(((byte) 101) + 20),
    Ability9(((byte) 101) + 21),
    Ability10(((byte) 101) + 22),
    ABILITY_MAX((byte) 126),
    HpGuiSlot((byte) 130);

    public static final int SIZE = Byte.SIZE;

    private final byte byteValue;
    private static java.util.HashMap<Byte, ControlType> mappings;

    ControlType(final int i)
    {
        this((byte) i);
    }

    private static java.util.HashMap<Byte, ControlType> getMappings()
    {
        if (mappings == null)
        {
            synchronized (ControlType.class)
            {
                if (mappings == null)
                {
                    mappings = new java.util.HashMap<>();
                }
            }
        }
        return mappings;
    }

    ControlType(final byte value)
    {
        byteValue = value;
        getMappings().put(value, this);
    }

    public byte getValue()
    {
        return byteValue;
    }

    public static ControlType forValue(byte value)
    {
        return getMappings().get(value);
    }
}
