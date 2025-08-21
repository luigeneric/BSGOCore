package io.github.luigeneric.core.movement;


import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Gear;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.utils.ObjectStat;
import io.github.luigeneric.templates.utils.ObjectStats;

import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class MovementController implements IMovementUpdateSubscriber
{
    protected final Transform transform;
    protected MovementFrame frame;
    protected Maneuver maneuver;
    protected Maneuver nextManeuver;
    protected Maneuver nextPulseManeuver;
    protected final Queue<Maneuver> nextManeuverQueue;

    protected Tick frameTick;
    protected boolean isNewManeuver;

    protected final MovementOptions movementOptions;
    protected boolean movementOptionsNeedsUpdate;
    protected Tick lastCollisionTick;
    protected Tick lastMovementUpdateTick;

    public MovementController(final Transform transform) throws NullPointerException
    {
        this.transform = transform;

        this.frame = new MovementFrame(this.getPosition(), Euler3.fromQuaternion(this.getRotation()));

        this.nextManeuverQueue = new ConcurrentLinkedQueue<>();
        this.isNewManeuver = false;
        this.movementOptions = new MovementOptions();
        this.movementOptions.setUpdateSubscriber(this);
        this.movementOptionsNeedsUpdate = false;

        nextManeuver = null;
        nextPulseManeuver = null;
    }

    @Override
    public void movementUpdate()
    {
        this.setMovementOptionsNeedUpdate();
    }

    public void movementUpdateInProgress()
    {
    }
    public void movementUpdateFinished()
    {
    }

    public void setNextManeuver(final Maneuver nextManeuver) throws NullPointerException
    {
        Objects.requireNonNull(nextManeuver, "New Maneuver cannot be null");
        this.movementUpdateInProgress();
        try
        {
            if (nextManeuver.getManeuverType() == ManeuverType.Pulse)
            {
                this.nextPulseManeuver = nextManeuver;
                this.nextManeuverQueue.offer(nextManeuver);
            }
            else if (nextManeuver.getManeuverType() == ManeuverType.Teleport)
            {
                this.nextManeuverQueue.offer(nextManeuver);
            }
            else
            {
                this.nextManeuver = nextManeuver;
            }
            this.movementOptionsNeedsUpdate = true;
        }
        finally
        {
            this.movementUpdateFinished();
        }
    }
    public void setMovementOptionsNeedUpdate()
    {
        this.movementUpdateInProgress();
        this.movementOptionsNeedsUpdate = true;
        this.movementUpdateFinished();
    }
    public abstract void move(final Tick tick, final float dt);

    public Vector3 getPosition()
    {
        return this.transform.getPosition();
    }
    public Quaternion getRotation()
    {
        return this.transform.getRotation();
    }
    public MovementFrame getFrame()
    {
        return this.frame;
    }

    public Tick getFrameTick()
    {
        return this.frameTick;
    }

    public boolean isNewManeuver()
    {
        return this.isNewManeuver;
    }
    public void setIsNewManeuver(final boolean value)
    {
        this.movementUpdateInProgress();
        this.isNewManeuver = value;
        this.movementUpdateFinished();
    }
    public MovementOptions getMovementOptions()
    {
        return movementOptions;
    }

    public abstract MovementFrame getLastFrame();

    @Override
    public void setMovementOptionsStats(final ObjectStats stats)
    {
        Objects.requireNonNull(movementOptions, "movementOptions cannot be null");
        Objects.requireNonNull(stats, "ObjectStats info cannot be null");

        switch (movementOptions.getGear())
        {
            case Regular ->
            {
                movementOptions.setSpeed(movementOptions.getThrottleSpeed());
            }
            case Boost ->
            {
                movementOptions.setSpeed(stats.getStat(ObjectStat.BoostSpeed));
            }
            case None ->
            {
                movementOptions.setSpeed(stats.getStat(ObjectStat.Speed));
            }
        }

        float acceleration = stats.getStatOrDefault(ObjectStat.Acceleration);
        if (movementOptions.getGear() == Gear.Boost && stats.containsStat(ObjectStat.AccelerationMultiplierOnBoost))
        {
            acceleration *= stats.getStat(ObjectStat.AccelerationMultiplierOnBoost);
        }
        movementOptions.setAcceleration(acceleration);
        movementOptions.setInertiaCompensation(stats.getStatOrDefault(ObjectStat.InertiaCompensation));
        movementOptions.setPitchAcceleration(stats.getStatOrDefault(ObjectStat.PitchAcceleration));
        movementOptions.setPitchMaxSpeed(stats.getStatOrDefault(ObjectStat.PitchMaxSpeed));
        movementOptions.setYawAcceleration(stats.getStatOrDefault(ObjectStat.YawAcceleration));
        movementOptions.setYawMaxSpeed(stats.getStatOrDefault(ObjectStat.YawMaxSpeed));
        movementOptions.setRollAcceleration(stats.getStatOrDefault(ObjectStat.RollAcceleration));
        movementOptions.setRollMaxSpeed(stats.getStatOrDefault(ObjectStat.RollMaxSpeed));
        movementOptions.setStrafeAcceleration(stats.getStatOrDefault(ObjectStat.StrafeAcceleration));
        movementOptions.setStrafeMaxSpeed(stats.getStatOrDefault(ObjectStat.StrafeMaxSpeed));
    }

    public void setTransform(final Transform fromTransform)
    {
        this.transform.setTransform(fromTransform);
        this.frame = new MovementFrame(fromTransform.getPosition(), fromTransform.getRotationEuler3());
        this.setMovementOptionsNeedUpdate();
    }

    @Override
    public void setMovementOptionsStats(final SpaceSubscribeInfo shipSubscribeInfo)
    {
        setMovementOptionsStats(shipSubscribeInfo.getStats());
    }

    public Transform getTransform()
    {
        return transform;
    }

    @Override
    public String toString()
    {
        return "MovementController{" +
                "transform=" + transform +
                ", frame=" + frame +
                ", maneuver=" + maneuver +
                ", nextManeuver=" + nextManeuver +
                ", nextPulseManeuver=" + nextPulseManeuver +
                ", frameTick=" + frameTick +
                ", isNewManeuver=" + isNewManeuver +
                ", movementOptions=" + movementOptions +
                ", movementOptionsNeedsUpdate=" + movementOptionsNeedsUpdate +
                ", lastCollisionTick=" + lastCollisionTick +
                '}';
    }

    public Tick getLastCollisionTick()
    {
        return lastCollisionTick;
    }

    public void setLastCollisionTick(final Tick lastCollisionTick)
    {
        this.lastCollisionTick = lastCollisionTick;
    }

    public boolean isMovingObject()
    {
        return false;
    }

    protected final Optional<Maneuver> getNextManeuver()
    {
        final Maneuver nextManeuverItem = nextManeuverQueue.poll();
        if (this.nextPulseManeuver != null)
        {
            return Optional.of(this.nextPulseManeuver);
        }
        if (nextManeuverItem != null)
        {
            return Optional.of(nextManeuverItem);
        }

        return Optional.ofNullable(this.nextManeuver);
    }

    /**
     * Unsafe method call, may be null, but due to no null return this will be faster
     * @return the next Maneuver to set or null
     */
    protected final Maneuver getNextManeuverUnsafe()
    {
        if (this.nextPulseManeuver != null)
            return this.nextPulseManeuver;
        return this.nextManeuver;
    }
    public boolean hasNextManeuver()
    {
        return this.getNextManeuverUnsafe() != null;
    }
    public Maneuver getCurrentManeuver()
    {
        if (this.maneuver == null)
        {
            final Optional<Maneuver> optNextManeuver = this.getNextManeuver();
            if (optNextManeuver.isPresent())
            {
                return optNextManeuver.get();
            }
        }
        return this.maneuver;
    }
    protected final void invalidateNextManeuver()
    {
        if (this.nextPulseManeuver != null)
        {
            this.nextPulseManeuver = null;
        } else if (this.nextManeuver != null)
        {
            this.nextManeuver = null;
        }
    }

    public Tick getLastMovementUpdateTick()
    {
        return lastMovementUpdateTick;
    }

    public void setLastMovementUpdateTick(final Tick lastMovementUpdateTick)
    {
        this.lastMovementUpdateTick = lastMovementUpdateTick;
    }

    public abstract MovementFrame getFrameOfTick(final Tick tickValue);
}
