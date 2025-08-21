package io.github.luigeneric.templates.augments;


import io.github.luigeneric.enums.FactorSource;
import io.github.luigeneric.enums.FactorType;
import io.github.luigeneric.templates.utils.AugmentActionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AugmentFactorTemplate extends AugmentTemplate
{
    private final FactorSource factorSource;
    private final List<FactorTypeRecord> factorTypeRecords;
    private final int activeTimeInHours;

    public AugmentFactorTemplate(final long associatedItemGUID,
                                 FactorSource factorSource,
                                 List<FactorTypeRecord> factorTypeRecords,
                                 int activeTimeInHours)
    {
        super(AugmentActionType.None, associatedItemGUID);
        this.factorSource = factorSource;
        this.factorTypeRecords = factorTypeRecords;
        this.activeTimeInHours = activeTimeInHours;
    }

    public FactorSource getFactorSource()
    {
        return factorSource;
    }

    public List<FactorTypeRecord> getFactorTypeRecords()
    {
        return new ArrayList<>(getAccumulatedIntoOneTypeRecord());
    }
    private Collection<FactorTypeRecord> getAccumulatedIntoOneTypeRecord()
    {
        Map<FactorType, FactorTypeRecord> rv = factorTypeRecords.stream()
                .collect(Collectors.groupingBy(FactorTypeRecord::type))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> new FactorTypeRecord(
                                entry.getKey(),
                                (float) entry.getValue().stream()
                                        .mapToDouble(FactorTypeRecord::value)
                                        .sum()
                        )
                ));
        return rv.values();
    }

    public int getActiveTimeInHours()
    {
        return activeTimeInHours;
    }
}
