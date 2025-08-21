package io.github.luigeneric.core.sector.management.abilities.actions;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.gameplayalgorithms.IFlareChanceAlgorithm;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.maneuver.DirectionalManeuver;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.SectorAlgorithms;
import io.github.luigeneric.core.sector.management.SectorSender;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.Missile;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.utils.ObjectStat;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class DropFlareAction extends AbilityAction
{
    private final SectorSpaceObjects spaceObjects;
    private final SectorSender sectorSender;
    private final IFlareChanceAlgorithm flareChanceAlgorithm;
    public DropFlareAction(Ship castingShip, ShipSlot castingSlot, List<SpaceObject> targetSpaceObjects,
                           boolean isAutoCastAbility, final SectorContext ctx,
                           final SectorAlgorithms sectorAlgorithms)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx);
        this.spaceObjects = ctx.spaceObjects();
        this.sectorSender = ctx.sender();
        this.flareChanceAlgorithm = sectorAlgorithms.getFlareChanceAlgorithm();
    }

    @Override
    protected boolean internalProcess()
    {
        try
        {
            final float flareRange = ability.getItemBuffAdd().getStatOrDefault(ObjectStat.FlareRange);
            final float flareRangeSq = flareRange * flareRange;
            //the flare id-info should be an empty list
            if (!targetSpaceObjects.isEmpty())
                return false;

            final List<Missile> missiles = this.spaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.Missile);

            if (ctx.users().isEmpty())
                return false;

            final GameProtocolWriteOnly gameProtocolWriteOnly = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Game);
            final BgoProtocolWriter flareReleased = gameProtocolWriteOnly.writeFlareReleased(castingShip.getObjectID());
            this.sectorSender.sendToAllClients(flareReleased);

            for (final Missile missile : missiles)
            {
                if (missile.isRemoved())
                    continue;

                if (missile.getFaction() == castingShip.getFaction())
                    continue;


                final float distanceSq = Vector3
                        .distanceSquared(castingShip.getMovementController().getPosition(), missile.getMovementController().getPosition());
                //not even in range
                if (distanceSq > flareRangeSq)
                    continue;

                //prevent error
                if (flareRange == 0)
                    continue;

                try
                {
                    final float chanceForFlareSuccess = this.flareChanceAlgorithm
                            .getChanceForFlareSuccess(Mathf.sqrt(distanceSq), flareRange);

                    final boolean dropMissile = ctx.bgoRandom().rollChance(chanceForFlareSuccess);
                    if (!dropMissile)
                    {
                        continue;
                    }
                    //now drop missile
                    final Maneuver oldManeuver = missile.getMovementController().getCurrentManeuver();
                    if (oldManeuver.getManeuverType() != ManeuverType.TargetLaunch)
                        continue;

                    final DirectionalManeuver directionalManeuver =
                            new DirectionalManeuver(missile.getMovementController().getFrame().getEuler3());
                    missile.getMovementController().setNextManeuver(directionalManeuver);

                    final SpaceObject missileTarget = missile.getMissileLaunchedOnObject();
                    if (missileTarget == null)
                    {
                        log.error("Missile target was null");
                        continue;
                    }

                    missile.invalidateLaunchOnObject();

                    if (missileTarget.isPlayer())
                    {
                        final Optional<User> optUser = this.ctx.users().getUser(missileTarget.getPlayerId());
                        if (optUser.isEmpty())
                            continue;
                        final User user = optUser.get();
                        final BgoProtocolWriter missileDecoyed = gameProtocolWriteOnly.writeMissileDecoyed(missile.getObjectID());
                        ctx.sender().sendToClient(missileDecoyed, user);
                    }
                } catch (IllegalArgumentException illegalArgumentException)
                {
                    illegalArgumentException.printStackTrace();
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return true;
    }
}
