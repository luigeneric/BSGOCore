package io.github.luigeneric.enums;

public enum SpaceEntityType
{
    Player                  (0x01000000L),
    Missile                 (0x02000000L),
    WeaponPlatform          (0x03000000L),
    Cruiser                 (0x04000000L),
    BotFighter              (0x05000000L),
    Debris                  (0x06000000L),
    Asteroid                (0x07000000L),
    CargoObject             (0x08000000L),
    MiningShip              (0x09000000L),
    Outpost                 (0x0a000000L),
    AsteroidBot             (0x0b000000L),
    Trigger                 (0x0c000000L),
    Planet                  (0x0d000000L),
    Planetoid               (0x0e000000L),
    Mine                    (0x0f000000L),
    Volume                  (0x10000000L),
    JumpBeacon              (0x11000000L),
    SectorEvent             (0x12000000L),
    MineField               (0x13000000L),
    JumpTargetTransponder   (0x14000000L),
    Comet                   (0x15000000L),
    @Deprecated
    SmartMine               (0x16000000L),
    CaptureTrigger          (0x17000000L),
    TypeMask                (0x1f000000L),
    SpaceLocationMarker     (0xf0000000L),
    AsteroidGroup           (0xf1000000L),
    SectorMap3DFocusPoint   (0xf2000000L);

    public final long value;

    public static SpaceEntityType fromValue(long value)
    {
        for (SpaceEntityType entry : SpaceEntityType.values())
        {
            if (value == entry.value)
            {
                return entry;
            }
        }
        return null;
    }

    SpaceEntityType(long value)
    {
        this.value = value;
    }

    private final static SpaceEntityType[] shipTypes = new SpaceEntityType[]{
        Player,
        MiningShip,
        Outpost,
        WeaponPlatform,
        BotFighter,
        Cruiser,
        JumpBeacon, //yes, no joke
        AsteroidBot, //dafuq
    };
    public static SpaceEntityType[] getShipTypes()
    {
        return shipTypes;
    }

    public boolean isOfType(final SpaceEntityType... types)
    {
        for (SpaceEntityType type : types)
        {
            if (this == type)
            {
                return true;
            }
        }
        return false;
    }
}
