package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.core.gameplayalgorithms.*;
import lombok.Getter;

import java.util.Objects;

@Getter
public final class SectorAlgorithms
{
    private final IHitchanceCalculator hitchanceCalculator;
    private final IArmorAlgorithm armorAlgorithm;
    private final ICritchanceAlgorithm critchanceAlgorithm;
    private final IFlareChanceAlgorithm flareChanceAlgorithm;
    private final IEWDurationAlgorithm ewDurationAlgorithm;

    public SectorAlgorithms(final IHitchanceCalculator hitchanceCalculator, final IArmorAlgorithm armorAlgorithm,
                            final ICritchanceAlgorithm critchanceAlgorithm, final IFlareChanceAlgorithm flareChanceAlgorithm,
                            final IEWDurationAlgorithm ewDurationAlgorithm)
    {
        this.hitchanceCalculator = Objects.requireNonNull(hitchanceCalculator);
        this.armorAlgorithm = Objects.requireNonNull(armorAlgorithm);
        this.critchanceAlgorithm = Objects.requireNonNull(critchanceAlgorithm);
        this.flareChanceAlgorithm = Objects.requireNonNull(flareChanceAlgorithm);
        this.ewDurationAlgorithm = Objects.requireNonNull(ewDurationAlgorithm);
    }

    public static SectorAlgorithms defaultAlgorithms()
    {
        return new SectorAlgorithms(
                new HitchanceBasedOnThrottle(),
                new ArmorAlgorithmV0(),
                new CritchanceAlgorithmV1(),
                new FlareChanceAlgorithmV1(),
                new HackDurationAlgorithmV1()
        );
    }
}
