package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.ISpaceObjectRemover;
import io.github.luigeneric.core.sector.management.OutPostStates;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.Outpost;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;

import java.util.List;

public class OutpostSpawnTimer extends DelayedTimer
{
    private final OutPostStates outPostStates;
    private final ISpaceObjectRemover remover;
    private final SpaceObjectFactory factory;
    private final SectorJoinQueue joinQueue;
    private final SectorDesc sectorDesc;

    public OutpostSpawnTimer(final Tick tick, SectorSpaceObjects sectorSpaceObjects, long delayedTicks,
                             final OutPostStates outPostStates, final ISpaceObjectRemover remover, final SpaceObjectFactory factory,
                             final SectorJoinQueue joinQueue, final SectorDesc sectorDesc)
    {
        super(tick, sectorSpaceObjects, delayedTicks);
        this.outPostStates = outPostStates;
        this.remover = remover;
        this.factory = factory;
        this.joinQueue = joinQueue;
        this.sectorDesc = sectorDesc;
    }

    @Override
    protected void delayedUpdate()
    {
        final List<SpaceObject> currentOutposts = sectorSpaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.Outpost);
        if (GalaxyMapCard.isBaseSector(Faction.Colonial, sectorDesc.getSectorID()))
        {
            if (currentOutposts.isEmpty())
            {
                spawnOp(Faction.Colonial);
            }
        }
        else if (GalaxyMapCard.isBaseSector(Faction.Cylon, sectorDesc.getSectorID()))
        {
            if (currentOutposts.isEmpty())
            {
                spawnOp(Faction.Cylon);
            }
        }

        if (this.outPostStates.colonialOutpostState().isOutPost())
        {
            if (currentOutposts.stream().noneMatch(obj -> obj.getFaction() == Faction.Colonial))
            {
                spawnOp(Faction.Colonial);
            }
        }
        else
        {
            despawnOp(Faction.Colonial);
        }
        if (this.outPostStates.cylonOutpostState().isOutPost())
        {
            if (currentOutposts.stream().noneMatch(obj -> obj.getFaction() == Faction.Cylon))
            {
                spawnOp(Faction.Cylon);
            }
        }
        else
        {
            despawnOp(Faction.Cylon);
        }
    }

    public void despawnOp(final Faction faction)
    {
        if (GalaxyMapCard.isBaseSector(Faction.Colonial, this.sectorDesc.getSectorID()) ||
                GalaxyMapCard.isBaseSector(Faction.Cylon, this.sectorDesc.getSectorID()))
        {
            return;
        }
        final List<Outpost> outposts = this.sectorSpaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.Outpost);
        for (final Outpost outpost : outposts)
        {
            if (outpost.getFaction() == faction)
            {
                remover.notifyRemovingCauseAdded(outpost, RemovingCause.JumpOut);
            }
        }
    }
    public void spawnOp(final Faction faction)
    {
        try
        {
            final SpaceObject op = factory.createOutpost(faction);
            joinQueue.addSpaceObject(op);
        }
        catch (IllegalStateException illegalStateException)
        {
            //illegalStateException.printStackTrace();
        }
    }
}
