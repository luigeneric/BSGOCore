package io.github.luigeneric.enums;

public enum CutsceneId
{
    Docking,
    Tut1IntroColonial,
    Tut1IntroCylon,
    Tut1DroneActivationColonial,
    Tut1DroneActivationCylon,
    Tut1ExtroPart1Colonial,
    Tut1ExtroPart1Cylon,
    Tut1ExtroPart2AllFactions,
    Tut1CylonsFindGalactica;

    public static final int SIZE = Integer.SIZE;

    public int getValue()
    {
        return this.ordinal();
    }

    public static CutsceneId forValue(final int value)
    {
        return values()[value];
    }
}
