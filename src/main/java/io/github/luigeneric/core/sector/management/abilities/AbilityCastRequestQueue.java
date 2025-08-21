package io.github.luigeneric.core.sector.management.abilities;


import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.core.sector.SectorJob;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.SectorAlgorithms;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.sector.management.abilities.actions.AbilityAction;
import io.github.luigeneric.core.sector.management.damage.DamageMediator;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AbilityCastRequestQueue implements SectorJob
{
    private final Deque<AbilityCastRequest> abilityCastRequests;
    private final Map<Long, AbilityCastRequest> autoCastAbilities;
    private final AbilityActionFactory abilityActionFactory;
    private final SectorContext ctx;

    public AbilityCastRequestQueue(final Deque<AbilityCastRequest> abilityCastRequests,
                                   final Map<Long, AbilityCastRequest> autoCastAbilities,
                                   final SectorContext ctx, final SectorAlgorithms sectorAlgorithms,
                                   final DamageMediator damageMediator, final SectorJoinQueue sectorJoinQueue,
                                   final LootAssociations lootAssociations)
    {
        this.abilityCastRequests = abilityCastRequests;
        this.autoCastAbilities = autoCastAbilities;
        this.ctx = ctx;
        this.abilityActionFactory = new AbilityActionFactory(ctx, sectorAlgorithms,
                damageMediator, sectorJoinQueue, lootAssociations);
    }
    public AbilityCastRequestQueue(final SectorContext ctx,
                                   final SectorAlgorithms sectorAlgorithms,
                                   final DamageMediator damageMediator, final SectorJoinQueue sectorJoinQueue,
                                   final LootAssociations lootAssociations)
    {
        this(new ConcurrentLinkedDeque<>(), new ConcurrentHashMap<>(), ctx, sectorAlgorithms,
                damageMediator, sectorJoinQueue, lootAssociations);
    }

    public void addCastRequest(final AbilityCastRequest abilityCastRequest)
    {
        this.abilityCastRequests.offer(abilityCastRequest);
    }
    public void addAutoCastAbility(final AbilityCastRequest abilityCastRequest)
    {
        Objects.requireNonNull(abilityCastRequest, "AbilityCastRequest was null!");
        this.autoCastAbilities.put(abilityCastRequest.generateKey(), abilityCastRequest);
    }

    public void removeAutoCastAbility(final int abilityID, final long objectID)
    {
        this.autoCastAbilities.remove(AbilityCastRequest.generateKey(objectID, abilityID));
    }
    public void removeAutoCastAbilityFromObject(final long objectID)
    {
        for (final AbilityCastRequest value : this.autoCastAbilities.values())
        {
            if (value.getCastingShip().getObjectID() == objectID)
            {
                this.autoCastAbilities.remove(value.generateKey());
            }
        }
    }

    private void updateAutoCastAbilities()
    {
        this.abilityCastRequests.addAll(this.autoCastAbilities.values());
    }

    @Override
    public void run()
    {
        this.updateAutoCastAbilities();

        while (!this.abilityCastRequests.isEmpty())
        {
            final AbilityCastRequest abilityCastRequest = abilityCastRequests.poll();

            final Optional<Ship> optCastingShip = ctx.spaceObjects().getShipByObjectID(abilityCastRequest.getCastingShip().getObjectID());
            if (optCastingShip.isEmpty())
            {
                this.removeAutoCastAbility(abilityCastRequest.getAbilityID(), abilityCastRequest.getCastingShip().getObjectID());
                continue;
            }
            final Ship castingShip = optCastingShip.get();
            final Optional<ShipSlots> optSlots = castingShip.getSpaceSubscribeInfo().getShipSlots();
            if (optSlots.isEmpty()) continue;
            final ShipSlot castingSlot = optSlots.get().getSlot(abilityCastRequest.getAbilityID());
            final List<SpaceObject> toCallOn = new ArrayList<>(abilityCastRequest.getTargetIDs().length);
            for (final long objID : abilityCastRequest.getTargetIDs())
            {
                final Optional<SpaceObject> optTmpObj = ctx.spaceObjects().get(objID);
                optTmpObj.ifPresent(e ->
                {
                    if (e.isVisible())
                        toCallOn.add(e);

                });
            }

            //this.abilityMediator.process(castingShip, castingSlot, toCallOn, abilityCastRequest.isAutoCastAbility());
            final AbilityAction abilityAction = this.abilityActionFactory
                    .create(castingShip, castingSlot, toCallOn, abilityCastRequest.isAutoCastAbility());
            abilityAction.process();
        }
    }
}
