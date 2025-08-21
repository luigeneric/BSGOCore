package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Vector3;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FollowManeuver extends Maneuver
{
    private final SpaceObject followTarget;

    public FollowManeuver(final SpaceObject followTarget)
    {
        super(ManeuverType.Follow);
        this.followTarget = followTarget;
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        if (this.followTarget == null)
        {
            return super.drift(prevFrame);
        }
        //final MovementFrame lastFrame = followTarget.getMovementController().getLastFrame();
        var followerManeuver = followTarget.getMovementController().getCurrentManeuver();
        //if (followerManeuver != null)
        //    log.info("Frame followTarget maneuver type: {}", followerManeuver.getManeuverType());

        final MovementFrame lastFrame = followTarget.getMovementController().getFrameOfTick(Tick.valueOf(tick.getValue()-1));
        final Vector3 otherPrevPos = lastFrame.getPosition();
        final Euler3 direction = Euler3.direction(Vector3.sub(otherPrevPos, prevFrame.getPosition()));
        return super.moveToDirection(prevFrame, direction, dt);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        if (followTarget != null)
        {
            bw.writeUInt32(this.followTarget.getObjectID());
        }
        else
        {
            //this could lead to bugs if there is an object with the id 0
            bw.writeUInt32(0);
        }

        bw.writeDesc(this.getMovementOptions());
    }
}
