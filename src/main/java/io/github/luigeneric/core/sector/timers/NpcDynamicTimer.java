package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementOptions;
import io.github.luigeneric.core.movement.maneuver.DirectionalManeuver;
import io.github.luigeneric.core.movement.maneuver.DirectionalWithoutRollManeuver;
import io.github.luigeneric.core.sector.SectorCards;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.ISpaceObjectRemover;
import io.github.luigeneric.core.sector.management.SectorSpaceObjects;
import io.github.luigeneric.core.sector.management.abilities.AbilityCastRequestQueue;
import io.github.luigeneric.core.sector.management.damage.SectorDamageHistory;
import io.github.luigeneric.core.sector.npcbehaviour.KillObjective;
import io.github.luigeneric.core.sector.npcbehaviour.PatrolObjective;
import io.github.luigeneric.core.spaceentities.NpcShip;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.utils.BgoRandom;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
@Slf4j
public class NpcDynamicTimer extends NpcTimer
{
    private final ISpaceObjectRemover remover;
    private final BgoRandom bgoRandom;
    public NpcDynamicTimer(final Tick tick, final SectorSpaceObjects sectorSpaceObjects, final long delayedTicks,
                           final AbilityCastRequestQueue abilityCastRequestQueue,
                           final SectorDamageHistory sectorDamageHistory,
                           final ISpaceObjectRemover remover,
                           final SectorCards sectorCards,
                           final BgoRandom bgoRandom

    )
    {
        super(tick, sectorSpaceObjects, delayedTicks, abilityCastRequestQueue, sectorDamageHistory, sectorCards);
        this.remover = remover;
        this.bgoRandom = bgoRandom;
    }

    @Override
    protected void delayedUpdate()
    {
        final Collection<NpcShip> movingBotFighterMovings = this.sectorSpaceObjects.getSpaceObjectsCollectionOfEntityType(SpaceEntityType.BotFighter);
        for (final NpcShip botFighterMoving : movingBotFighterMovings)
        {
            final boolean jumpedOut = jumpOutNpcIfNoMoreTargets(botFighterMoving);
            if (jumpedOut)
                continue;

            //update target
            final SpaceObject closest = getNextTarget(botFighterMoving);
            if (closest == null && botFighterMoving.getPatrolObjectives().isEmpty())
                continue;

            //update weapons, does nothing if closest is null
            updateWeapons(botFighterMoving, closest);

            //update current maneuver based on closest possible target or environment checkup(is inside box)
            if (closest == null)
            {
                updateBotManeuver(botFighterMoving);
            } else
            {
                updateBotManeuver(botFighterMoving, closest);
            }
        }
    }

    /**
     * Updates the movement based on the patrol information if one is present
     * If one patrol-objective is present, takes the first and checks if the fighter is inside the given environment.
     * If so, do nothing, if the bot left the box, it should direct back to the center!
     * @param npcShip the BotFighter object to move
     */
    public void updateBotManeuver(final NpcShip npcShip)
    {
        final List<PatrolObjective> patrolObjectives = npcShip.getPatrolObjectives();
        if (patrolObjectives.isEmpty())
            return;
        final PatrolObjective patrolObjective = patrolObjectives.get(0);
        if (patrolObjective == null)
        {
            log.error("inside NpcDynamicTimer: PatrolObjective was null, therefore returned out of update movement");
            return;
        }

        //check if rest maneuver(if so there would be no movement)
        if (npcShip.getMovementController().getCurrentManeuver() == null ||
                npcShip.getMovementController().getCurrentManeuver().getManeuverType() == ManeuverType.Rest)
        {
            npcShip.getMovementController().setNextManeuver(
                    new DirectionalManeuver(Euler3.fromQuaternion(npcShip.getMovementController().getRotation())));
        }

        final MovementOptions movementOptions = npcShip.getMovementController().getMovementOptions();
        if (movementOptions.getSpeed() == 0)
        {
            final float speed = npcShip.getSpaceSubscribeInfo().getStatOrDefault(ObjectStat.Speed);
            movementOptions.setSpeed(speed);
            movementOptions.setThrottleSpeed(speed);
        }

        final boolean isInsideBox = patrolObjective.isInsideBox(npcShip.getMovementController().getPosition());
        if (isInsideBox)
            return;
        //final Euler3 directionToCenter = patrolObjective.getDirectionToCenter(botFighter.getMovementController().getPosition());
        //botFighter.getMovementController().setNextManeuver(new DirectionalManeuver(directionToCenter));
        //different approach -> random position inside the box!
        final Vector3 targetVector3 =
                new Vector3(
                        bgoRandom.getInsideVectors(patrolObjective.getBoxToPatrolIn().min().toArray(),
                                patrolObjective.getBoxToPatrolIn().max().toArray()
                        )
                );
        final Euler3 directionToRandomPosition = Euler3.direction(targetVector3.sub(npcShip.getMovementController().getPosition()));
        npcShip.getMovementController().setNextManeuver(new DirectionalManeuver(directionToRandomPosition));
    }
    public void updateBotManeuver(final NpcShip botFighterMoving, final SpaceObject closest)
    {
        final Vector3 botPosition = botFighterMoving.getMovementController().getPosition();

        final float closestDistance = closest.getMovementController().getPosition().distance(botPosition);
        final Euler3 direction = Euler3.direction(Vector3.sub(closest.getMovementController().getPosition(), botPosition));
        final boolean isDirectionWithRoll = tick.getValue() % 5 == 0;
        final Maneuver newManeuver = isDirectionWithRoll ? new DirectionalManeuver(direction) : new DirectionalWithoutRollManeuver(direction);
        botFighterMoving.getMovementController().setNextManeuver(newManeuver);

        //speed to 0, if distance to target is close enough and the target-speed is less than 5(almost standing)
        //else max flank-speed
        final float speed = closestDistance < botFighterMoving.getNpcBehaviourTemplate().speedZeroDistance()
                && closest.getMovementController().getFrame().getLinearSpeed().magnitude() < 5f ?
                0 :
                botFighterMoving.getSpaceSubscribeInfo().getStatOrDefault(ObjectStat.Speed);

        botFighterMoving.getMovementController().getMovementOptions().setSpeed(speed);
        botFighterMoving.getMovementController().getMovementOptions().setThrottleSpeed(speed);
    }

    private boolean jumpOutNpcIfNoMoreTargets(final NpcShip botFighterMoving)
    {
        //if the kill objective is gone, jump this npc out
        final List<KillObjective> killObjectives = botFighterMoving.getKillObjectives();
        final boolean allKillObjectivesGone = killObjectives
                .stream()
                .allMatch(
                        obj -> obj.getObjectivesToKill()
                                .stream()
                                .allMatch(spaceObject -> spaceObject.getRemovingCause().isPresent()));


        final long lifeTimeEndTimeStamp =
                (long) (botFighterMoving.getNpcBehaviourTemplate().lifeTimeSeconds() * 1000L + botFighterMoving.getCreatingTimeStamp());
        final boolean lifeTimeIsOver = (lifeTimeEndTimeStamp - tick.getTimeStamp()) < 0;
        final boolean botFighterIsInCombat = botFighterMoving.getSpaceSubscribeInfo().isInCombat();

        //all targets have to be gone AND
        //  lifetime over AND
        //      not in combat OR
        //      bot is allowed to jump out
        if (allKillObjectivesGone &&
                lifeTimeIsOver && (!botFighterIsInCombat || botFighterMoving.getNpcBehaviourTemplate().jumpOutIfInCombat()))
        {
            remover.notifyRemovingCauseAdded(botFighterMoving, RemovingCause.JumpOut);
            return true;
        }

        return false;
    }
}
