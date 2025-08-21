package io.github.luigeneric.core.movement.maneuver;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.movement.MovementSimulation;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;


public class DirectionalWithoutRollManeuver extends Maneuver
{
    private final Euler3 direction;


    public DirectionalWithoutRollManeuver(final Euler3 direction)
    {
        super(ManeuverType.DirectionalWithoutRoll);
        this.direction = direction;
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        return MovementSimulation.moveToDirectionWithoutRoll(prevFrame, direction, getMovementOptions());
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeEuler3(this.direction);
        bw.writeDesc(getMovementOptions());
    }
}
