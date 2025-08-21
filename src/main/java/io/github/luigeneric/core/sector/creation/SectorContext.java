package io.github.luigeneric.core.sector.creation;

import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.*;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.utils.BgoRandom;


public record SectorContext(Tick tick,
                            SectorUsers users,
                            SectorSpaceObjects spaceObjects,
                            BgoRandom bgoRandom,
                            SectorSender sender,
                            SectorBlueprint blueprint,
                            ObjectIDRegistry idRegistry,
                            SpaceObjectFactory spaceObjectFactory,
                            OutPostStates outPostStates,
                            LootAssociations lootAssociations
)
{
}
