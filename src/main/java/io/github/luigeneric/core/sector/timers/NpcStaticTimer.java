package io.github.luigeneric.core.sector.timers;


import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.ShipSlots;
import io.github.luigeneric.core.sector.SectorCards;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.abilities.AbilityCastRequest;
import io.github.luigeneric.core.sector.management.abilities.AbilityCastRequestQueue;
import io.github.luigeneric.core.sector.management.damage.SectorDamageHistory;
import io.github.luigeneric.core.spaceentities.NpcShip;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.utils.ShipAbilityAffect;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NpcStaticTimer extends NpcTimer
{
    private final Map<Long, SpaceObject> lastTargets;
    public NpcStaticTimer(final Tick tick, final SectorSpaceObjects sectorSpaceObjects, final long delay,
                          final AbilityCastRequestQueue abilityCastRequestQueue,
                          final SectorDamageHistory sectorDamageHistory,
                          final SectorCards sectorCards
    )
    {
        super(tick, sectorSpaceObjects, delay, abilityCastRequestQueue, sectorDamageHistory, sectorCards);
        this.lastTargets = new HashMap<>();
    }

    @Override
    protected void delayedUpdate()
    {
        final List<NpcShip> npcShips = sectorSpaceObjects.getSpaceObjectsOfEntityTypes(
                SpaceEntityType.WeaponPlatform,
                SpaceEntityType.Outpost
        );
        for (final NpcShip npcShip : npcShips)
        {
            //update target
            final SpaceObject closest = getNextTarget(npcShip);
            //if no change in target, dont update all weapons all over again!
            final SpaceObject lastTarget = lastTargets.get(npcShip.getObjectID());
            //lastTarget may be null, closest aswell!
            if ((lastTarget == null && closest == null) || closest != null && closest.equals(lastTarget))
            {
                continue;
            }
            lastTargets.put(npcShip.getObjectID(), closest);
            //update weapons, does nothing if closest is null
            updateWeapons(npcShip, closest);
        }
    }


    @Override
    protected void updateWeapons(final Ship ship, final SpaceObject closest)
    {
        final Optional<ShipSlots> optSlots = ship.getSpaceSubscribeInfo().getShipSlots();
        if (optSlots.isEmpty())
            return;
        final ShipSlots slots = optSlots.get();
        for (final ShipSlot slot : slots.values())
        {
            if (closest == null)
            {
                this.abilityCastRequestQueue.removeAutoCastAbility(slot.getShipSystem().getServerID(), ship.getObjectID());
            }
            else
            {
                if (slot.getShipSystem().getCardGuid() != 0 &&
                        slot.getShipAbility().getShipAbilityCard().getShipAbilityAffect() == ShipAbilityAffect.Area)
                {
                    final Set<Long> allObjectIDs = getAllEnemyObjectIds(ship, spaceObject -> true);

                    final AbilityCastRequest abilityCastRequest = new AbilityCastRequest(
                            ship,
                            slot.getShipSystem().getServerID(),
                            true,
                            allObjectIDs);
                    this.abilityCastRequestQueue.addAutoCastAbility(abilityCastRequest);
                }
                else
                {
                    final AbilityCastRequest abilityCastRequest = new AbilityCastRequest(
                            ship,
                            slot.getShipSystem().getServerID(),
                            true,
                            closest.getObjectID());
                    this.abilityCastRequestQueue.addAutoCastAbility(abilityCastRequest);
                }
            }
        }
    }

    private Set<Long> getAllEnemyObjectIds(final Ship me, final Predicate<SpaceObject> predicate)
    {
        return this.sectorSpaceObjects.values().stream()
                .filter(obj -> obj.getFaction() != me.getFaction())
                .filter(predicate)
                .map(SpaceObject::getObjectID)
                .collect(Collectors.toSet());
    }
}
