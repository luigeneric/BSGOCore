package io.github.luigeneric.core.sector.management.abilities.actions;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.game.GameProtocol;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.SectorSender;
import io.github.luigeneric.core.sector.management.lootsystem.loot.Loot;
import io.github.luigeneric.core.sector.management.lootsystem.loot.LootAssociations;
import io.github.luigeneric.core.spaceentities.Asteroid;
import io.github.luigeneric.core.spaceentities.Planetoid;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.cards.CounterCardType;
import io.github.luigeneric.templates.cards.SectorCard;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.Price;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ResourceScanAction extends AbilityAction
{
    private final LootAssociations lootAssociations;
    public ResourceScanAction(Ship castingShip, ShipSlot castingSlot, List<SpaceObject> targetSpaceObjects,
                              boolean isAutoCastAbility, SectorContext ctx,
                              final LootAssociations lootAssociations)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx);
        this.lootAssociations = lootAssociations;
    }

    @Override
    protected boolean internalProcess()
    {
        //scan is only for players for now
        if (!castingShip.isPlayer())
            return false;

        final Optional<User> optUser = this.ctx.users().getUser(castingShip.getPlayerId());
        if (optUser.isEmpty())
            return false;
        final User client = optUser.get();

        final GameProtocol gameProtocol = client.getProtocol(ProtocolID.Game);
        final Vector3 casterPosition = castingShip.getMovementController().getPosition();
        final float maxRange = ability.getItemBuffAdd().getStatOrDefault(ObjectStat.MaxRange);
        final float maxRangeSq = maxRange * maxRange;

        for (final SpaceObject targetSpaceObject : targetSpaceObjects)
        {
            final float distSq = Vector3.distanceSquared(casterPosition, targetSpaceObject.getMovementController().getPosition());
            if (distSq > maxRangeSq)
                continue;

            final boolean scanResultOkay =
                    scanProcess(
                            client,
                            gameProtocol,
                            targetSpaceObject,
                            lootAssociations,
                            ctx.blueprint().sectorDesc().getMiningShipConfig().priceInCubits(),
                            ctx.sender(),
                            ctx.blueprint().sectorCards().sectorCard(),
                            ctx.blueprint().sectorDesc().getSectorID()
                    );
            if (!scanResultOkay)
                return false;
        }

        return true;
    }

    public static boolean scanProcess(final User user, final GameProtocol gameProtocol, final SpaceObject target,
                                      final LootAssociations lootAssociations, final int priceInCubits,
                                      final SectorSender sender,
                                      final SectorCard sectorCard,
                                      final long sectorId
                                      )
    {
        if (!target.getSpaceEntityType().isOfType(SpaceEntityType.Asteroid, SpaceEntityType.Planetoid))
        {
            log.warn("Scan call on non-Asteroid type!");
            return false;
        }
        final Asteroid asteroid = (Asteroid) target;
        final Optional<Loot> loot = lootAssociations.get(asteroid);
        final Loot optLoot = loot.orElse(null);
        final ItemCountable resource = optLoot == null ? null : (ItemCountable) optLoot.getLootItems().getFirst().shipItem();

        final Price price = new Price();
        boolean isMinable = false;
        if (asteroid instanceof Planetoid planetoid)
        {
            price.addItem(ResourceType.Cubits.guid, priceInCubits);
            isMinable = !planetoid.hasMiningShip();
        }
        else
        {
            user.getPlayer().getCounterFacade().incrementCounter(
                    CounterCardType.asteroids_scanned,
                    sectorCard.getCardGuid()
            );
        }
        final BgoProtocolWriter bw = gameProtocol.writeScan(asteroid.getObjectID(), resource, isMinable, price,
                LocalDateTime.ofEpochSecond(0,0, ZoneOffset.UTC), sectorId);
        sender.sendToClient(bw, user);

        return true;
    }
}
