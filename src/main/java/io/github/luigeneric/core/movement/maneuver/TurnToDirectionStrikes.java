package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.movement.MovementSimulation;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;

public class TurnToDirectionStrikes extends Maneuver
{
    protected final Euler3 direction;
    protected final float roll;
    protected final float slideX;
    protected final float slideY;

    public TurnToDirectionStrikes(final Euler3 direction, final float roll, final float slideX, final float slideY)
    {
        super(ManeuverType.TurnToDirectionStrikes);
        this.direction = direction;
        this.roll = roll;
        this.slideX = slideX;
        this.slideY = slideY;
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        return MovementSimulation.turnToDirectionStrikes(prevFrame, this.direction, this.roll, this.slideX, this.slideY, this.getMovementOptions(), dt);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeEuler3(this.direction);
        bw.writeSingle(this.roll);
        bw.writeSingle(this.slideX);
        bw.writeSingle(this.slideY);
        bw.writeDesc(this.getMovementOptions());

    }
}
