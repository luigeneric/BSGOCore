package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.templates.sectortemplates.OutpostProgressTemplate;

public record OutPostStates(OutpostState colonialOutpostState, OutpostState cylonOutpostState,
                            OutpostProgressTemplate colonialProgressTemplate, OutpostProgressTemplate cylonProgressTemplate)
{
    public OutpostProgressTemplate getTemplateFromFaction(final Faction faction) throws IllegalArgumentException
    {
        switch (faction)
        {
            case Colonial -> {
                return colonialProgressTemplate;
            }
            case Cylon ->
            {
                return cylonProgressTemplate;
            }
            default -> throw new IllegalArgumentException("No OPProgressTemplate for faction " + faction);
        }
    }
    public OutpostProgressTemplate getInvertedTemplateForFaction(final Faction faction)
    {
        switch (faction)
        {
            case Colonial -> {
                return cylonProgressTemplate;
            }
            case Cylon ->
            {
                return colonialProgressTemplate;
            }
            default -> throw new IllegalArgumentException("No OPProgressTemplate for faction " + faction);
        }
    }

    public OutpostState getStateForFaction(final Faction faction)
    {
        switch (faction)
        {
            case Colonial ->
            {
                return colonialOutpostState;
            }
            case Cylon ->
            {
                return cylonOutpostState;
            }
            default -> throw new IllegalArgumentException("OPState for faction not implemented");
        }
    }
}
