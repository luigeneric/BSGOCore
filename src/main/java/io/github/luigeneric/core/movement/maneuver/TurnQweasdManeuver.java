package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.movement.MovementSimulation;
import io.github.luigeneric.core.movement.QWEASD;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;

public class TurnQweasdManeuver extends Maneuver
{
    private final QWEASD qweasd;
    public TurnQweasdManeuver(final QWEASD qweasd)
    {
        super(ManeuverType.TurnQweasd);
        this.qweasd = qweasd;
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        if (!prevFrame.isValid())
        {
            return MovementFrame.invalid();
        }
        return MovementSimulation.qweasd(prevFrame, this.qweasd.pitch(), this.qweasd.yaw(), this.qweasd.roll(), this.getMovementOptions(), dt);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte((byte) this.qweasd.getBitmask());
        bw.writeDesc(getMovementOptions());
    }

    public QWEASD getQweasd()
    {
        return qweasd;
    }
}
