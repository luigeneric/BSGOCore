package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;

public class DirectionalManeuver extends Maneuver
{
    private final Euler3 direction;

    public DirectionalManeuver(final Euler3 direction)
    {
        super(ManeuverType.Directional);
        this.direction = direction;
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        return super.moveToDirection(prevFrame, direction, dt);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.ensureDeltaCapacity(3 * 4 + 11 * 4 + 1);
        bw.writeEuler3(direction);
        bw.writeDesc(getMovementOptions());
    }
}
