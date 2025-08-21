package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.StaticVectors;
import io.github.luigeneric.linearalgebra.base.UnmodifiableDecorator;
import io.github.luigeneric.linearalgebra.base.Vector3;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class PulseManeuver extends Maneuver
{
    private final Vector3 direction;
    private final Euler3 euler3Direction;

    public PulseManeuver(final Vector3 direction)
    {
        super(ManeuverType.Pulse);
        Objects.requireNonNull(direction, "DirectionVector cannot be null");
        this.direction = direction;
        this.euler3Direction = UnmodifiableDecorator.wrap(Euler3.direction(direction));
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        if (tick.equals(this.startTick))
        {
            final MovementFrame result = super.moveToDirection(prevFrame, euler3Direction, dt);
            return new MovementFrame(result.getPosition(), result.getEuler3(), direction, StaticVectors.ZERO, result.getEuler3Speed(), result.getMode(), result.isValid());
        }

        return super.moveToDirection(prevFrame, euler3Direction, dt);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeVector3(this.direction);
        bw.writeDesc(this.getMovementOptions());
    }

    public Vector3 getDirection()
    {
        return this.direction;
    }
}
