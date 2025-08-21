package io.github.luigeneric.core.movement;


import io.github.luigeneric.core.movement.maneuver.PulseManeuver;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.cards.MovementCard;
import io.github.luigeneric.templates.utils.ObjectStats;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicMovementController extends MovementController
{
    protected final MovementCard movementCard;
    protected MovementFrame lastFrame;
    protected MovementFrame nextFrame;

    public DynamicMovementController(final Transform transform, final MovementCard movementCard)
    {
        super(transform);
        this.movementCard = movementCard;
        this.isNewManeuver = false;
    }


    @Override
    public void move(final Tick tick, final float dt) throws IllegalStateException
    {
        final Maneuver nextManeuver = this.getNextManeuverUnsafe();
        if (this.maneuver == null)
        {
            if (nextManeuver == null)
            {
                throw new IllegalStateException("cannot call move while maneuver is null");
            }
            this.maneuver = nextManeuver;
        }

        if (nextManeuver != null)
        {
            this.invalidateNextManeuver();

            nextManeuver.setMovementOptions(this.movementOptions);
            this.maneuver = nextManeuver;
            this.maneuver.setStartTick(tick);
            this.isNewManeuver = true;
            this.movementOptionsNeedsUpdate = false;
        }
        if (this.movementOptionsNeedsUpdate)
        {
            this.movementOptionsNeedsUpdate = false;
            this.isNewManeuver = true;
            this.maneuver.setStartTick(tick);
            this.maneuver.setMovementOptions(this.movementOptions);
            if (this.maneuver instanceof PulseManeuver pulseManeuver)
            {
                pulseManeuver.getDirection().set(this.getFrame().getLinearSpeed());
            }
        }

        this.frameTick = tick;
        this.lastFrame = this.frame;
        if (nextManeuver == null && this.nextFrame != null)
        {
            /*
            if (maneuver.maneuverType == ManeuverType.Follow)
            {
                log.info("use old nextframe");
            }
             */
            this.frame = this.nextFrame;
        }
        else
        {
            //calculate frame if nextFrame is null else just abuse fact that nextFrame exists
            this.frame = this.maneuver.nextFrame(tick, lastFrame.deepCopy(), dt);
        }
        this.nextFrame = null;



        this.transform.setPositionRotation(frame.getPosition(), frame.getRotation());
    }

    @Override
    public MovementFrame getLastFrame()
    {
        return this.lastFrame;
    }


    public MovementCard getMovementCard()
    {
        return this.movementCard;
    }

    @Override
    public void setMovementOptionsStats(ObjectStats stats)
    {
        super.setMovementOptionsStats(stats);
        this.movementOptions.applyCard(this.getMovementCard());
    }

    @Override
    public void setMovementOptionsStats(final SpaceSubscribeInfo spaceSubscribeInfo)
    {
        super.setMovementOptionsStats(spaceSubscribeInfo);
        this.movementOptions.applyCard(this.getMovementCard());
    }

    @Override
    public boolean isMovingObject()
    {
        return true;
    }

    @Override
    public MovementFrame getFrameOfTick(final Tick tickValue)
    {
        final int offsetValue = this.frameTick.getValue() - tickValue.getValue();

        if (offsetValue == 0)
        {
            return this.frame;
        }
        else if (offsetValue == -1)
        {
            return this.lastFrame;
        }
        else if (offsetValue == 1)
        {
            this.nextFrame = this.maneuver.nextFrame(tickValue, this.frame, 0.1f);
            return this.nextFrame;
        }
        else
        {
            //log.error("DynamicMovementUpdate bad offset {}", offsetValue);
            final float offsetDt = offsetValue * 0.1f;
            final Vector3 offsetPos = this.frame.getFuturePosition(offsetDt);
            final Euler3 offsetEuler = this.frame.getFutureEuler3(offsetDt);

            return new MovementFrame(offsetPos, offsetEuler,
                    this.frame.getLinearSpeed(), this.frame.getStrafeSpeed(),
                    this.frame.getEuler3Speed(), this.frame.getMode(), true
            );
        }
    }
}