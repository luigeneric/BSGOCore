package io.github.luigeneric.core.sector.management;



import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.damage.AccumulatedDamage;
import io.github.luigeneric.core.sector.management.damage.ObjectDamageHistory;
import io.github.luigeneric.core.sector.management.damage.SectorDamageHistory;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.sector.objleft.ObjectLeftDeath;
import io.github.luigeneric.core.sector.objleft.ObjectLeftDescription;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.templates.sectortemplates.OutpostProgressTemplate;

import java.util.Objects;
import java.util.Optional;

public class SectorOutpostProgress implements ObjectLeftSubscriber
{
    private final Tick tick;
    private final OutPostStates outPostStates;
    private final SectorDamageHistory sectorDamageHistory;
    private final LootAssociations lootAssociations;

    public SectorOutpostProgress(final Tick tick, final OutPostStates outPostStates,
                                 final SectorDamageHistory sectorDamageHistory,
                                 final LootAssociations lootAssociations)
    {
        this.tick = tick;
        this.outPostStates = outPostStates;
        this.sectorDamageHistory = sectorDamageHistory;
        this.lootAssociations = lootAssociations;
    }

    public void updateDeath(final ObjectLeftDescription objectLeftDescription)
    {
        final SpaceObject spaceObjectToRemove = objectLeftDescription.getRemovedSpaceObject();
        switch (spaceObjectToRemove.getSpaceEntityType())
        {
            case Player ->
            {
                if (objectLeftDescription instanceof ObjectLeftDeath objectLeftDeath)
                {
                    final Optional<SpaceObject> optKillerObj = objectLeftDeath.getKillerObject();
                    if (optKillerObj.isPresent() && optKillerObj.get().isPlayer())
                    {
                        final OutpostProgressTemplate ptsTemplate =
                                outPostStates.getInvertedTemplateForFaction(spaceObjectToRemove.getFaction());

                        final OutpostState state = this.outPostStates.getStateForFaction(optKillerObj.get().getFaction());
                        state.increasePoints(ptsTemplate.ptsPlayerKilled());
                    }
                }
            }
            case Outpost ->
            {
                final OutpostState currentOpState =
                        spaceObjectToRemove.getFaction() == Faction.Colonial ?
                                outPostStates.colonialOutpostState() : outPostStates.cylonOutpostState();
                currentOpState.opDied(this.tick);
            }
            case BotFighter, WeaponPlatform ->
            {
                final ObjectDamageHistory dmgHistory = sectorDamageHistory.getDamageHistory(spaceObjectToRemove);
                if (dmgHistory == null)
                {
                    return;
                }
                final Optional<AccumulatedDamage> optKillShotDealer = dmgHistory.getKillShotDealer();
                if (optKillShotDealer.isEmpty())
                    return;

                final AccumulatedDamage killShotDealer = optKillShotDealer.get();
                if (!killShotDealer.getDealer().isPlayer())
                    return;

                try
                {
                    final OutpostProgressTemplate ptsTemplate =
                            outPostStates.getInvertedTemplateForFaction(killShotDealer.getDealer().getFaction());
                    final OutpostState opState = outPostStates.getStateForFaction(killShotDealer.getDealer().getFaction());
                    opState.increasePoints(ptsTemplate.ptsNpcKilled());
                }catch (IllegalArgumentException ignored)
                {}
            }
            case Asteroid ->
            {
                final boolean hasLoot = lootAssociations.hasLoot(spaceObjectToRemove);
                if (!hasLoot)
                    return;

                final Optional<AccumulatedDamage> optKillShotDamage =
                        sectorDamageHistory.getKillShotDealerOfObject(spaceObjectToRemove);
                if (optKillShotDamage.isPresent())
                {
                    final SpaceObject dealer = optKillShotDamage.get().getDealer();
                    if (!dealer.isPlayer())
                        return;

                    final OutpostProgressTemplate ptsTemplate =
                            outPostStates.getTemplateFromFaction(dealer.getFaction());
                    final OutpostState opState = outPostStates.getStateForFaction(dealer.getFaction());
                    //Log.info("Asteroid killed, add pts: " + ptsTemplate.ptsAsteroidKilledWithRessources());
                    opState.increasePoints(ptsTemplate.ptsAsteroidKilledWithRessources());
                }
            }
        }
    }

    @Override
    public void onUpdate(final ObjectLeftDescription arg)
    {
        if (Objects.requireNonNull(arg.getRemovingCause()) == RemovingCause.Death)
        {
            updateDeath(arg);
        }
    }
}
