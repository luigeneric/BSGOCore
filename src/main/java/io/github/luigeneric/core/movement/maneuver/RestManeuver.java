package io.github.luigeneric.core.movement.maneuver;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.Maneuver;
import io.github.luigeneric.core.movement.MovementFrame;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.enums.ManeuverType;
import io.github.luigeneric.linearalgebra.base.Euler3;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;

public class RestManeuver extends Maneuver
{
    private final Vector3 position;
    private final Euler3 euler3;

    public RestManeuver(final Vector3 position, final Euler3 euler3)
    {
        super(ManeuverType.Rest);
        this.position = position.copy();
        this.euler3 = euler3.copy();
    }
    public RestManeuver(final Transform transform)
    {
        this(transform.getPosition(), Euler3.fromQuaternion(transform.getRotation()));
    }

    @Override
    public MovementFrame nextFrame(final Tick tick, final MovementFrame prevFrame, final float dt)
    {
        return new MovementFrame(this.position, this.euler3, Vector3.zero(), Vector3.zero(), Euler3.zero(), 0);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeVector3(position);
        bw.writeEuler3(euler3);
    }
}
