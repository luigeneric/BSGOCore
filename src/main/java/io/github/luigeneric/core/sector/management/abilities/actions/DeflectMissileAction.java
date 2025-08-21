package io.github.luigeneric.core.sector.management.abilities.actions;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.movement.maneuver.DirectionalManeuver;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.game.GameProtocolWriteOnly;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.spaceentities.Missile;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.utils.ObjectStat;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class DeflectMissileAction extends AbilityAction
{
    private final GameProtocolWriteOnly gameWriter;
    public DeflectMissileAction(final Ship castingShip,
                                final ShipSlot castingSlot,
                                final List<SpaceObject> targetSpaceObjects,
                                final boolean isAutoCastAbility,
                                final SectorContext ctx)
    {
        super(castingShip, castingSlot, targetSpaceObjects, isAutoCastAbility, ctx);
        this.gameWriter = ProtocolRegistryWriteOnly.game();
    }

    @Override
    protected boolean internalProcess()
    {
        final float range = ability.getItemBuffAdd().getStatOrDefault(ObjectStat.MaxRange);
        final float rangeSq = range * range;
        final Vector3 casterPos = castingShip.getMovementController().getPosition();

        for (final SpaceObject targetSpaceObject : targetSpaceObjects)
        {
            if (targetSpaceObject.getSpaceEntityType() != SpaceEntityType.Missile)
            {
                log.error(targetSpaceObject.getPlayerId() + "Cast missile jammer on something else than missile!");
                continue;
            }
            final Missile missile = (Missile) targetSpaceObject;
            if (missile.isRemoved())
                continue;

            final Vector3 missilePos = missile.getMovementController().getPosition();
            final float distanceSq = casterPos.distanceSq(missilePos);
            if (distanceSq > rangeSq)
                continue;

            //the missile is ready to be jammed!
            missile.getMovementController()
                    .setNextManeuver(new DirectionalManeuver(missile.getMovementController().getFrame().getEuler3()));

            final SpaceObject missileTarget = missile.getMissileLaunchedOnObject();
            if (missileTarget == null)
                continue;

            if (missileTarget.isPlayer())
            {
                final Optional<User> optUser = this.ctx.users().getUser(missileTarget.getPlayerId());
                if (optUser.isEmpty())
                    continue;
                final User user = optUser.get();
                final BgoProtocolWriter missileDecoyed = gameWriter.writeMissileDecoyed(missile.getObjectID());
                ctx.sender().sendToClient(missileDecoyed, user);
            }
            missile.invalidateLaunchOnObject();
        }
        return true;
    }
}
