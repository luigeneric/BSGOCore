package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.movement.MovementSimulation;
import io.github.luigeneric.core.movement.QWEASD;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;

public class TurnManeuver extends Maneuver
{
    private final QWEASD qweasd;
    public TurnManeuver(final QWEASD qweasd)
    {
        super(ManeuverType.Turn);
        this.qweasd = qweasd;
        //this.movementOptions.setGear(Gear.Regular);
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        if (!prevFrame.isValid())
        {
            return MovementFrame.invalid();
        }
        //return Simulation.wasd(prevFrame.copy(), this.qweasd.pitch(), this.qweasd.yaw(), this.qweasd.roll(), this.movementOptions);
        //since roll is never used, there is no need to expensive generate roll information
        return MovementSimulation.wasd(prevFrame, this.qweasd.pitch(), this.qweasd.yaw(), this.getMovementOptions());
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte((byte) this.qweasd.getBitmask());
        bw.writeDesc(this.getMovementOptions());
    }
}
