package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.scene.SceneProtocol;
import io.github.luigeneric.core.sector.management.JumpRegistry;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.SectorUsers;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.utils.ObjectStat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PowerPointsTimer extends UpdateTimer
{
    private final JumpRegistry jumpRegistry;
    private final SectorUsers sectorUsers;
    public PowerPointsTimer(final SectorSpaceObjects sectorSpaceObjects, final JumpRegistry jumpRegistry,
                            final SectorUsers sectorUsers)
    {
        super(sectorSpaceObjects);
        this.jumpRegistry = jumpRegistry;
        this.sectorUsers = sectorUsers;
    }

    @Override
    public void update(final float dt)
    {
        for (final SpaceObject spaceObject : this.sectorSpaceObjects.getSpaceObjectsNotOfEntityType(
                SpaceEntityType.Asteroid,
                SpaceEntityType.Planetoid,
                SpaceEntityType.Missile,
                SpaceEntityType.Planet,
                SpaceEntityType.MiningShip,
                SpaceEntityType.Comet))
        {
            if (spaceObject.isRemoved())
                continue;

            final SpaceSubscribeInfo stats = spaceObject.getSpaceSubscribeInfo();

            final Float recoveryPP = stats.getStat(ObjectStat.PowerRecovery);
            final Float maxPP = stats.getStat(ObjectStat.MaxPowerPoints);
            if (recoveryPP == null || maxPP == null)
                continue;

            final float currentPP = stats.getPp();

            //is jumping -> no need to recover
            if (spaceObject.isPlayer())
            {
                final SceneProtocol sceneProtocol = sectorUsers.getUserUnsafe(spaceObject.getPlayerId()).getProtocol(ProtocolID.Scene);
                final boolean registryContainsJump = jumpRegistry.containsSpaceObject(spaceObject);
                if (registryContainsJump)
                {
                    if (currentPP != 0)
                    {
                        stats.setPp(0);
                    }
                }
                else if (sceneProtocol.isLoggingOut())
                {
                    if (currentPP != 0)
                    {
                        stats.setPp(0);
                    }
                }
                else if (spaceObject.getSpaceObjectState().getIsDocking())
                {
                    if (currentPP != 0)
                    {
                        stats.setPp(0);
                    }
                }
                else
                {
                    setNewPp(maxPP, currentPP, recoveryPP, dt, stats);
                }
            }
            else
            {
                //no recovery, no change,
                //current equals max is not necessary and maxPP equals 0 is as well not necessary
                if (recoveryPP == 0 || currentPP == maxPP || maxPP == 0)
                    continue;

                setNewPp(maxPP, currentPP, recoveryPP, dt, stats);
            }
        }
    }
    private void setNewPp(final float maxPP, final float currentPP, final float recoveryPP, final float dt,
                          final SpaceSubscribeInfo stats)
    {
        final float newPP = Mathf.clampSafe(currentPP + recoveryPP * dt, 0, maxPP);

        //don't set if current equals new -> no update resent
        if (newPP == currentPP)
            return;

        stats.setPp(newPP);
    }
}