package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.StaticVectors;

@Deprecated
public class FlipManeuver extends Maneuver
{
    public FlipManeuver()
    {
        super(ManeuverType.Flip);
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        return MovementFrame.invalid();
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeVector3(StaticVectors.ZERO); //wont be used
        bw.writeDesc(this.getMovementOptions());
    }
}
