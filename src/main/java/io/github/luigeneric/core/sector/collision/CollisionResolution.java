package io.github.luigeneric.core.sector.collision;


import io.github.luigeneric.core.movement.MovementController;
import io.github.luigeneric.core.movement.maneuver.PulseManeuver;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.ISpaceObjectRemover;
import io.github.luigeneric.core.sector.management.damage.DamageMediator;
import io.github.luigeneric.core.spaceentities.Asteroid;
import io.github.luigeneric.core.spaceentities.Missile;
import io.github.luigeneric.core.spaceentities.Ship;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.collidershapes.CollisionRecord;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.cards.ShipCard;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.utils.Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CollisionResolution
{
    private final DamageMediator damageMediator;
    private final ISpaceObjectRemover remover;
    private Tick currentTick;
    private List<CollisionPair> primitivePair;
    private List<CollisionInfo> contactPair;
    public CollisionResolution(final DamageMediator damageMediator,
                               final ISpaceObjectRemover remover)
    {
        this.damageMediator = damageMediator;
        this.remover = remover;
    }

    public void setCurrentTick(final Tick currentTick)
    {
        this.currentTick = currentTick;
    }

    public void injectPrimitiveAndContactInfos(final List<CollisionPair> primitivePair, final List<CollisionInfo> contactPair)
    {
        this.primitivePair = primitivePair;
        this.contactPair = contactPair;
    }


    private static byte getTier(final SpaceObject spaceObject)
    {
        if (spaceObject instanceof Ship ship)
        {
            return ship.getShipCard().getTier();
        }
        return 0;
    }
    private static ShipCard getSpaceObjectShipCard(final SpaceObject spaceObject)
    {
        if (!(spaceObject instanceof Ship ship))
        {
            throw new IllegalArgumentException("SpaceObject is not first ship");
        }

        return ship.getShipCard();
    }

    public void resolveAll()
    {
        this.resolvePrimitive();
        this.resolveNonPrimitives();
    }

    private void resolvePrimitive()
    {
        if (this.primitivePair.isEmpty())
            return;

        for (final CollisionPair collisionPair : this.primitivePair)
        {
            //asteroids x ships
            //missiles x ships x planetoids
            final SpaceObject a = collisionPair.first();
            final SpaceObject b = collisionPair.second();

            // ok why do I check this here?
            // --> some primitives might be already removed because
            // a missile already hit planetoid but it was aswell inside the radius of an other ship
            if (a.isRemoved() || b.isRemoved())
            {
                continue;
            }

            final SpaceEntityType aType = a.getSpaceEntityType();
            final SpaceEntityType bType = b.getSpaceEntityType();

            if (aType == SpaceEntityType.Asteroid || bType == SpaceEntityType.Asteroid)
            {
                final Asteroid asteroid = (Asteroid) (aType == SpaceEntityType.Asteroid ? a : b);
                final SpaceObject other = aType == SpaceEntityType.Asteroid ? b : a;
                if (other.getSpaceEntityType() == SpaceEntityType.Asteroid)
                {
                    throw new IllegalStateException("Asteroid x Asteroid in primitive check should never happen!");
                }
                this.resolveAsteroidXOther(asteroid, other);
            }
            //this should never happen!!! (because its sorted out before but lets check anyway
            else if (aType == SpaceEntityType.Missile && bType == SpaceEntityType.Missile)
            {
                throw new IllegalStateException("Missile x Missile resolution!! This should never happen");
            }
            else if (aType == SpaceEntityType.Missile || bType == SpaceEntityType.Missile)
            {
                final Missile missile = (Missile) (aType == SpaceEntityType.Missile ? a : b);
                final SpaceObject other = (aType == SpaceEntityType.Missile ? b : a);
                this.resolveMissileOther(missile, other);
            } else if (aType == SpaceEntityType.Comet || bType == SpaceEntityType.Comet)
            {
                final SpaceObject comet100Percent = aType == SpaceEntityType.Comet ? a : b;
                final SpaceObject cometNotSure = aType == SpaceEntityType.Comet ? b : a;
                if (cometNotSure.getSpaceEntityType() == SpaceEntityType.Comet)
                {
                    this.resolveCometXComet(comet100Percent, cometNotSure);
                }
                else
                {
                    this.resolveCometXOther(comet100Percent, cometNotSure);
                }
            } else
            {
                throw new IllegalStateException("State not handled for collision-pair: " + a + " "  + b);
            }
        }
    }

    private void resolveCometXOther(final SpaceObject comet, final SpaceObject other)
    {
        if (other.getSpaceEntityType().isOfType(SpaceEntityType.Planetoid, SpaceEntityType.Planet, SpaceEntityType.Outpost))
        {
            remover.notifyRemovingCauseAdded(comet, RemovingCause.Death);
        } else if (other.getSpaceEntityType().isOfType(SpaceEntityType.getShipTypes()))
        {
            remover.notifyRemovingCauseAdded(other, RemovingCause.Death);
        }
    }

    private void resolveCometXComet(final SpaceObject comet100Percent, final SpaceObject otherComet)
    {
        remover.notifyRemovingCauseAdded(comet100Percent, RemovingCause.Death);
        remover.notifyRemovingCauseAdded(otherComet, RemovingCause.Death);
    }

    private void resolveAsteroidXOther(final Asteroid asteroid, final SpaceObject other)
    {
        if (other.isRemoved())
        {
            return;
        }
        this.damageMediator.dealDamageFromAsteroidCollision(asteroid, other);
        remover.notifyRemovingCauseAdded(asteroid, RemovingCause.Death);
    }

    private void resolveMissileOther(final Missile missile, final SpaceObject other)
    {
        //other is planetoid -> missile will die from planetoid and only removed
        if (missile.isRemoved())
        {
            try
            {
                throw new IllegalStateException("Should not happen");
            }
            catch (Exception ex)
            {
                log.info("missile[{}] already removed! Cause={} skip collision resolve process={}",
                        missile.getObjectID(),
                        missile.getRemovingCause(),
                        Utils.getExceptionStackTrace(ex)
                );
            }
            return;
        }
        if (other.getSpaceEntityType() == SpaceEntityType.Planetoid)
        {
            remover.notifyRemovingCauseAdded(missile, RemovingCause.Hit, other);
        }
        else if (other.getSpaceEntityType().isOfType(SpaceEntityType.Outpost, SpaceEntityType.WeaponPlatform,
                SpaceEntityType.Player, SpaceEntityType.BotFighter, SpaceEntityType.MiningShip, SpaceEntityType.Comet))
        {
            if (other.isRemoved())
                return;
            this.damageMediator.dealDamageFromMissile(missile, other);
            remover.notifyRemovingCauseAdded(missile, RemovingCause.Hit, other);
        }
    }

    private void resolveNonPrimitives()
    {
        for (final CollisionInfo collisionInfo : this.contactPair)
        {
            resolveCollisionInfo(collisionInfo);
        }
    }


    public void resolveCollisionInfo(final CollisionInfo collision)
    {
        final SpaceObject currentObj = collision.object1();
        final SpaceObject againstObj = collision.object2();

        //both moving
        if (currentObj.getMovementController().isMovingObject() &&
                againstObj.getMovementController().isMovingObject())
        {
            resolveMovingXMovingObject(currentObj, againstObj, collision.collisionRecord());
        }
        //one is first moving and the other is static
        else if (currentObj.getMovementController().isMovingObject() || againstObj.getMovementController().isMovingObject())
        {
            final SpaceObject movingObject = currentObj.getMovementController().isMovingObject() ? currentObj : againstObj;
            final SpaceObject staticObject = currentObj.getMovementController().isMovingObject() ? againstObj : currentObj;
            boolean inverseNormal = !currentObj.getMovementController().isMovingObject();
            resolveMovingXStaticObject(movingObject, staticObject, collision.collisionRecord(), inverseNormal);
        }
        else
        {
            throw new IllegalStateException("This should not happen!");
        }
    }

    private void resolveMovingXMovingObject(final SpaceObject currentObj, final SpaceObject againstObj,
                                            final CollisionRecord collisionRecord)
    {
        //limit collision for 1 second

        final Tick lastCollisionTick = currentObj.getMovementController().getLastCollisionTick();

        if (lastCollisionTick != null &&
                lastCollisionTick.getValue() + 10 > currentTick.getValue())
        {
            return;
        }

        // calc base information
        final Vector3 currentLinearSpeed = currentObj.getMovementController().getFrame().getLinearSpeed();
        final float currentSpeed = currentLinearSpeed.magnitude();

        final Vector3 againstLinearSpeed = againstObj.getMovementController().getFrame().getLinearSpeed();
        final float againstSpeed = againstLinearSpeed.magnitude();

        final Vector3 v = collisionRecord.normal();
        final float depth = collisionRecord.penetrationDepth();

        final byte currentTier = getTier(currentObj);
        final byte againstTier = getTier(againstObj);

        final float avgSpeed = Mathf.avg(currentSpeed, againstSpeed);
        if (currentTier <= againstTier)
        {
            //idea: limit speed to max speed
            final float depthMultiplier = 1f;
            final float boostSpeed = currentObj.getSpaceSubscribeInfo().getStatOrDefault(ObjectStat.BoostSpeed);
            final float sumForce = getResolutionForce(depth, depthMultiplier, currentTier, againstTier, avgSpeed, boostSpeed);
            final Vector3 direction = Vector3.mult(v, sumForce);
            setCollisionManeuver(currentObj, direction);
        }

        if (againstTier <= currentTier)
        {
            final float depthMultiplier = 1f;
            final float boostSpeed = againstObj.getSpaceSubscribeInfo().getStatOrDefault(ObjectStat.BoostSpeed);
            final float finalForce = getResolutionForce(depth, depthMultiplier, currentTier, againstTier, avgSpeed, boostSpeed);
            final float sumForce = -finalForce;
            final Vector3 direction = Vector3.mult(v, sumForce);
            setCollisionManeuver(againstObj, direction);
        }
    }

    private void resolveMovingXStaticObject(final SpaceObject movingObject, final SpaceObject staticObject,
                                            final CollisionRecord collisionRecord, boolean inverseNormal)
    {
        final MovementController movingMovementController = movingObject.getMovementController();

        //limit collision for 1 second
        final Tick lastCollisionTick = movingMovementController.getLastCollisionTick();
        if (lastCollisionTick != null &&
                lastCollisionTick.getValue() + 10 > currentTick.getValue()
        )
        {
            return;
        }

        //System.out.println("current: " + movingObject.getObjectID());

        //baseinfos
        final Vector3 currentLinearSpeed = movingMovementController.getFrame().getLinearSpeed();
        final float currentSpeed = currentLinearSpeed.magnitude();

        //calculate the force between two objects
        //finde Verbindungslinie zwischen den Objekten
        //final Vector3 v = Vector3.subtract(currentPos, againstPos);
        //v.normalize();
        final Vector3 v = collisionRecord.normal();
        if (inverseNormal)
            v.invert();
        final float depth = collisionRecord.penetrationDepth();

        //System.out.println("v normal: " + v + " DEPTH: " + depth);
        //idea: limit speed to max speed
        final float depthMultiplier = 1.5f;

        final byte movingTier = getTier(movingObject);
        final byte staticTier = getTier(staticObject);

        //final float sumForce = Mathf.min(
        //        depth*depthMultiplier + avgSpeed,
        //        currentShipCard.getStats().getStatOrDefault(ObjectStat.BoostSpeed));
        final float boostSpeed = movingObject.getSpaceSubscribeInfo().getStatOrDefault(ObjectStat.BoostSpeed, 20);
        final float sumForce = getResolutionForce(
                depth, depthMultiplier,
                movingTier, staticTier,
                currentSpeed, boostSpeed
        );
        final Vector3 direction = Vector3.mult(v, sumForce);
        setCollisionManeuver(movingObject, direction);
    }

    private static float getResolutionForce(final float depth, final float depthMult,
                                            final float currentTier, final float againstTier,
                                            final float speed,
                                            final float boostSpeedLimiter
    )
    {
        final float depthSum = depth * depthMult;
        final float baseForce = (currentTier == 0 || againstTier == 0) ? 0 : Math.abs(againstTier - currentTier) * 8f;
        return Mathf.min(depthSum + baseForce + Mathf.clampSafe(speed, 0, boostSpeedLimiter), boostSpeedLimiter * 4);
    }

    private void setCollisionManeuver(final SpaceObject spaceObject, final Vector3 direction)
    {
        spaceObject.getMovementController().setNextManeuver(new PulseManeuver(direction));
        spaceObject.getMovementController().setLastCollisionTick(currentTick);
    }
}
