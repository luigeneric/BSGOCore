package io.github.luigeneric.enums;

public enum ResourceType
{
    None(0),
    Cubits(264733124L),
    Titanium(207047790L),
    Tylium(215278030L),
    Water(130762195L),
    Token(130920111L),
    TuningKit(254909109L),
    TechnicalAnalysisKit(187088612L),
    Plutonium(63148366L),
    Uranium(172582782L),
    CommAccess(28157328L),
    FragmentedFTLCoordinates(130797813L),
    FtlOverride(166681557L),
    DivineInspiration(92666191L),

    YellowBox(13),
    GreenBox(11),
    RedBox(12),
    BlueBox(10),
    StrikerStandard_Rounds(197609684L),
    StrikerStandard_Missiles(17980086L),
    StrikerGreen_Missiles(101797958L),
    StrikerGreen_Rounds(101797958L),
    EscortStandard_Rounds(126173396L),
    EscortStandard_Missiles(221436534L),
    EscortGreen_Rounds(5L),
    EscortGreen_Missiles(228448854L),
    LinerStandard_Rounds(59143780L),
    LinerGreen_Rounds(9L),
    LinerStandard_Missiles(218608438L),
    LinerGreen_Missiles(228410854L),
    Liner_powerCell(113883533L),

    Strikerx20Nuke(98392991L),
    Escortx20Nuke(174428943L),
    Linerx20Nuke(190162639L),

    Strikex5Nuke(195427878L),
    Escortx5Nuke(57483190L),
    Linerx5Nuke(56189094L);

    public static final int SIZE = Integer.SIZE;

    public final long guid;

    private static final class MappingsHolder
    {
        private static final java.util.HashMap<Long, ResourceType> mappings = new java.util.HashMap<>();
    }

    private static java.util.HashMap<Long, ResourceType> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ResourceType(final long guid)
    {
        this.guid = guid;
        getMappings().put(guid, this);
    }

    public static ResourceType forValue(long value)
    {
        return getMappings().get(value);
    }

    public static ResourceType repairType(final boolean useCubits)
    {
        return useCubits ? Cubits : Titanium;
    }
}
