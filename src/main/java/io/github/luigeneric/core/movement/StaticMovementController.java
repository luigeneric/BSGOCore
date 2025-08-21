package io.github.luigeneric.core.movement;

import io.github.luigeneric.core.movement.maneuver.RestManeuver;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;

public class StaticMovementController extends MovementController
{
    public StaticMovementController(final Transform transform) throws NullPointerException
    {
        super(transform);
        this.setNextManeuver(new RestManeuver(getPosition(), Euler3.fromQuaternion(getRotation())));
    }

    @Override
    public void move(final Tick tick, final float dt)
    {
        //copy not nessesary because copy process is already done in caller
        if (this.maneuver == null)
        {
            final Maneuver next = this.getNextManeuverUnsafe();
            if (next != null)
            {
                this.maneuver = next;
                this.maneuver.setStartTick(tick);
            }
        }
        //This should never happen as of the current state of the code but to prevent future errors, lets keep it
        if (this.maneuver.startTick == null)
            this.maneuver.setStartTick(tick);
        //Not sure if this is actually needed
        this.frameTick = tick;
        if (this.movementOptionsNeedsUpdate)
        {
            this.isNewManeuver = true;
            this.movementOptionsNeedsUpdate = false;
            this.maneuver.setStartTick(tick);
        }

    }

    /**
     * Since this frame is always static we will just use the standard frame
     * @return the MovementFrame at current-minus
     */
    @Override
    public MovementFrame getLastFrame()
    {
        return this.frame;
    }

    @Override
    public MovementFrame getFrameOfTick(final Tick tickValue)
    {
        return frame;
    }
}
