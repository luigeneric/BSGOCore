package io.github.luigeneric.core.movement.maneuver;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.movement.MovementSimulation;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Vector2;
import io.github.luigeneric.linearalgebra.base.Vector3;

public class TurnByPitchYawStrikes extends Maneuver
{
    private final Vector3 pitchYawRollFactor;
    private final Vector2 strafeDirection;
    private final float strafeMagnitude;

    public TurnByPitchYawStrikes(final Vector3 pitchYawRollFactor, final Vector2 strafeDirection, final float strafeMagnitude)
    {
        super(ManeuverType.TurnByPitchYawStrikes);
        this.pitchYawRollFactor = pitchYawRollFactor;
        this.strafeDirection = strafeDirection;
        this.strafeMagnitude = strafeMagnitude;
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        return MovementSimulation.turnByPitchYawStrikes(prevFrame, this.pitchYawRollFactor,
                this.strafeDirection, this.strafeMagnitude, this.getMovementOptions());
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeVector3(pitchYawRollFactor);
        bw.writeVector2(strafeDirection);
        bw.writeSingle(strafeMagnitude);
        bw.writeDesc(this.getMovementOptions());
    }

    public Vector3 getPitchYawRollFactor()
    {
        return pitchYawRollFactor;
    }

    public Vector2 getStrafeDirection()
    {
        return strafeDirection;
    }

    public float getStrafeMagnitude()
    {
        return strafeMagnitude;
    }
}
