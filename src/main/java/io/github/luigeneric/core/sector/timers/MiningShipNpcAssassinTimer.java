package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.creation.SectorContext;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.spaceentities.MiningShip;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.StaticVectors;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.npcbehaviour.NpcBehaviourTemplate;
import io.github.luigeneric.templates.sectortemplates.MiningShipConfig;
import io.github.luigeneric.templates.sectortemplates.NpcGuidLootId;
import io.github.luigeneric.utils.BgoRandom;

import java.util.Arrays;
import java.util.List;

public class MiningShipNpcAssassinTimer extends DelayedTimer
{
    private final MiningShipConfig miningShipConfig;
    private final SpaceObjectFactory spaceObjectFactory;
    private final SectorJoinQueue joinQueue;
    private final BgoRandom bgoRandom;
    public MiningShipNpcAssassinTimer(final SectorContext ctx, final long delayedTicks,
                                      final MiningShipConfig miningShipConfig, final SpaceObjectFactory spaceObjectFactory,
                                      final SectorJoinQueue joinQueue)
    {
        super(ctx.tick(), ctx.spaceObjects(), delayedTicks);
        this.miningShipConfig = miningShipConfig;
        this.spaceObjectFactory = spaceObjectFactory;
        this.joinQueue = joinQueue;
        this.bgoRandom = new BgoRandom();
    }

    @Override
    protected void delayedUpdate()
    {
        final List<MiningShip> miningShips = this.sectorSpaceObjects.getSpaceObjectsOfEntityType(SpaceEntityType.MiningShip);
        final long tickTimeStamp = tick.getTimeStamp();
        final long npcSpawnDelay = miningShipConfig.secondsUntilNpcSpawns() * 1000L;
        final long initialDelay = miningShipConfig.npcInitialSpawnDelaySeconds() * 1000L;
        for (final MiningShip miningShip : miningShips)
        {
            final long lastTimeAssassin = miningShip.getLastTimeAssassin();
            //there was no npc spawn ever
            if (lastTimeAssassin == 0)
            {
                miningShip.setLastTimeAssassin(tickTimeStamp + initialDelay - npcSpawnDelay);
                continue;
            }

            final long timeStampWhenNpcShouldSpawn = npcSpawnDelay + lastTimeAssassin;
            final long diff = tickTimeStamp - timeStampWhenNpcShouldSpawn;
            if (diff < 0)
                continue;

            //get mining ship position
            final Transform miningTransform = miningShip.getMovementController().getTransform();

            //final SpaceObject newObj = this.spaceObjectFactory.createComet(23);
            //newObj.getMovementController().setNextManeuver(new RestManeuver(assassinTransform));
            final List<NpcGuidLootId> res =
                    Arrays.stream(miningShipConfig.npcGuidLootIds())
                            .filter(npcGuidLootId -> npcGuidLootId.faction() != miningShip.getFaction())
                            .toList();
            final NpcGuidLootId npcGuidLootId = this.bgoRandom.getRandomListMember(res);
            for (int i = 0; i < npcGuidLootId.count(); i++)
            {
                final Transform assassinTransform = miningTransform.copy();

                final Vector3 assassinPos = assassinTransform.getPosition();
                final Vector3 upAxis = assassinTransform.getRotation().mult(StaticVectors.UP);
                final Vector3 rightAxis = assassinTransform.getRotation().mult(StaticVectors.RIGHT);
                final Vector3 forwardAxis = assassinTransform.getRotation().mult(StaticVectors.FORWARD);
                assassinPos.add(assassinTransform.getRotation().direction().add(upAxis.mult(bgoRandom.getRndBetween(200, 1000))));
                assassinPos.add(assassinTransform.getRotation().direction().add(rightAxis.mult(bgoRandom.getRndBetween(-600, 600))));
                assassinPos.add(assassinTransform.getRotation().direction().add(forwardAxis.mult(bgoRandom.getRndBetween(450, 700))));

                final SpaceObject newAssassin = this.spaceObjectFactory.createBotFighter(npcGuidLootId.npcGUID(),
                        List.of(miningShip), List.of(), List.of(),
                        assassinTransform,
                        new NpcBehaviourTemplate(
                                1, 400, 2500, 15, false, 400),
                        npcGuidLootId.lootID());
                this.joinQueue.addSpaceObject(newAssassin);
            }


            miningShip.setLastTimeAssassin(tickTimeStamp);
        }
    }
}
