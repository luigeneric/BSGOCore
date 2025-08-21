package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.utils.SpotDesc;

public class TargetLaunchManeuver extends LaunchAbstractBase
{
    private final SpaceObject targetSpaceObject;

    public TargetLaunchManeuver(final SpaceObject launcherSpaceObject, final SpotDesc spotDesc,
                                final float relativeSpeed, final SpaceObject targetSpaceObject)
    {
        super(ManeuverType.TargetLaunch, launcherSpaceObject, spotDesc, relativeSpeed);
        this.targetSpaceObject = targetSpaceObject;
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        if (this.startTick.equals(tick))
        {
            return this.getLaunchFrame(dt);
        }

        return this.getFollowFrame(prevFrame, dt);
    }

    private MovementFrame getFollowFrame(final MovementFrame prevFrame, final float dt)
    {
        if (this.targetSpaceObject == null || targetSpaceObject.getRemovingCauseDirect() != null)
        {
            return this.drift(prevFrame);
        }

        final MovementFrame prevFrameTarget = targetSpaceObject.getMovementController().getLastFrame();

        final Euler3 direction = Euler3.direction(Vector3.sub(prevFrameTarget.getPosition(), prevFrame.getPosition()));
        return this.moveToDirection(prevFrame, direction, dt);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.launcherSpaceObject.getObjectID());
        bw.writeUInt32(this.targetSpaceObject.getObjectID());
        bw.writeUInt16(this.spotDesc.getObjectPointServerHash());
        bw.writeSingle(this.relativeSpeed);
        bw.writeDesc(this.getMovementOptions());
    }

    public SpaceObject getTargetSpaceObject()
    {
        return targetSpaceObject;
    }
}
