package io.github.luigeneric.core.movement.maneuver;


import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Vector3;

public class TeleportManeuver extends Maneuver
{
    private final Vector3 position;

    public TeleportManeuver(final Vector3 position)
    {
        super(ManeuverType.Teleport);
        this.position = position;
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        return new MovementFrame(this.position, prevFrame.getEuler3(), Vector3.zero(), Vector3.zero(), Euler3.zero(), (byte) 0);
    }
}
