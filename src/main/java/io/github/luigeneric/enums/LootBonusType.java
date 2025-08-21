package io.github.luigeneric.enums;

public enum LootBonusType
{
    None,
    Squad,
    Booster,
    Faction;

    public static final int SIZE = Short.SIZE;

    public short getValue()
    {
        return (short) this.ordinal();
    }

    public static LootBonusType forValue(short value)
    {
        return values()[value];
    }

    public static LootBonusType fromFactorSource(final FactorSource factorSource)
    {
        switch (factorSource)
        {
            case Augment, Marketing, Holiday ->
            {
                return Booster;
            }
            case Faction ->
            {
                return Faction;
            }
        }
        return None;
    }
}
