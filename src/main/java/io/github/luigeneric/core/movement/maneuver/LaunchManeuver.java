package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.templates.utils.SpotDesc;

public class LaunchManeuver extends LaunchAbstractBase
{

    public LaunchManeuver(final SpaceObject launcherSpaceObject,
                          final SpotDesc spotDesc, final float relativeSpeed)
    {
        super(ManeuverType.Launch, launcherSpaceObject, spotDesc, relativeSpeed);
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        if (this.startTick.equals(tick))
        {
            return this.getLaunchFrame(dt);
        }
        return this.drift(prevFrame);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.launcherSpaceObject.getObjectID());
        bw.writeUInt16(this.spotDesc.getObjectPointServerHash());
        bw.writeSingle(this.relativeSpeed);
        bw.writeDesc(this.getMovementOptions());
    }
}
