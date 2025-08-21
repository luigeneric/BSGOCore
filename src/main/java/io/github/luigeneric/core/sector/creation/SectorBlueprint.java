package io.github.luigeneric.core.sector.creation;

import io.github.luigeneric.core.sector.SectorCards;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;
import io.github.luigeneric.templates.utils.MapStarDesc;

public record SectorBlueprint(
        SectorDesc sectorDesc,
        SectorCards sectorCards,
        MapStarDesc starDesc
) {}

