package io.github.luigeneric.core.movement;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.linearalgebra.base.*;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MovementFrame implements IProtocolWrite
{
    private final Vector3 position;
    private final Euler3 euler3;
    private final Vector3 linearSpeed;
    private final Vector3 strafeSpeed;
    private final Euler3 euler3Speed;
    private final byte mode;
    private boolean valid;
    private Quaternion rotationCached;


    public MovementFrame(final Vector3 position, final Euler3 euler3, final Vector3 linearSpeed, final Vector3 strafeSpeed,
                         final Euler3 euler3Speed, final byte mode, final boolean valid)
    {
        this.position = position;
        this.euler3 = euler3;
        this.linearSpeed = linearSpeed;
        this.strafeSpeed = strafeSpeed;
        this.euler3Speed = euler3Speed;
        this.mode = mode;
        this.valid = valid;

        this.rotationCached = null;
    }
    public MovementFrame(final Vector3 position, final Euler3 euler3, final Vector3 linearSpeed,
                         final Vector3 strafeSpeed, final Euler3 euler3Speed, final int mode)
    {
        this(position, euler3, linearSpeed, strafeSpeed, euler3Speed, (byte) mode, true);
    }

    public MovementFrame(final Vector3 position, final Euler3 euler3)
    {
        this(position, euler3, Vector3.zero(), Vector3.zero(), Euler3.zero(), (byte) 0);
    }

    private MovementFrame(final MovementFrame toCopyFrame)
    {
        this(toCopyFrame.position.copy(), toCopyFrame.euler3.copy(), toCopyFrame.linearSpeed.copy(), toCopyFrame.strafeSpeed.copy(),
                toCopyFrame.euler3Speed.copy(), toCopyFrame.mode, toCopyFrame.valid);
    }

    public final MovementFrame flatCopy()
    {
        return new MovementFrame(
                this.position,
                this.euler3,
                this.linearSpeed,
                this.strafeSpeed,
                this.euler3Speed,
                this.mode,
                this.valid
        );
    }
    public final MovementFrame deepCopy()
    {
        return new MovementFrame(this);
    }

    public final Quaternion getRotation()
    {
        if (this.rotationCached != null)
        {
            return this.rotationCached;
        }

        this.rotationCached = this.euler3.quaternion();

        return UnmodifiableDecorator.wrap(this.rotationCached);
    }

    /**
     * Calculates a directional Vector using Vector3.FORWARD and the rotation
     *
     * @return a new Vector3 object pointing in the direction of the ship
     */
    public final Vector3 lookDirection()
    {
        return this.getRotation().mult(StaticVectors.FORWARD);
    }

    public final Vector3 getFuturePosition(final float dt)
    {
        final Vector3 linPlusStrafe = Vector3.add(this.linearSpeed, this.strafeSpeed);
        linPlusStrafe.mult(dt);
        linPlusStrafe.add(this.position);
        return linPlusStrafe;
    }

    public static MovementFrame invalid()
    {
        final MovementFrame invalid =
                new MovementFrame(Vector3.zero(), Euler3.identity(), Vector3.zero(), Vector3.zero(), Euler3.zero(), (byte) 0);
        invalid.valid = false;
        return invalid;
    }


    /**
     * The next Euler3 is based on the given time passed
     * @param dt the time passed
     * @return first new Euler3 object containing the current euler3
     */
    public final Euler3 getFutureEuler3(final float dt)
    {
        return switch (this.mode)
        {
            case 0 -> (Euler3.mult(this.euler3Speed, dt)).add(euler3).normalized(true);
            case 1 -> (Euler3.mult(this.euler3Speed, dt)).add(euler3).normalized(false);
            case 2 -> Euler3.rotateOverTime(this.euler3, this.euler3Speed, dt);
            case 3 -> Euler3.rotateOverTimeLocal(this.euler3, this.euler3Speed, dt);
            default -> Euler3.zero();
        };
    }
    public final Quaternion getFutureRotation(final float t)
    {
        if (this.mode == 2)
        {
            return Euler3.rotateOverTime(this.euler3, this.euler3Speed, t).quaternion();
        }
        if (this.mode == 3)
        {
            return Euler3.rotateOverTimeLocal(this.euler3, this.euler3Speed, t).quaternion();
        }
        return Euler3.add(this.euler3, Euler3.mult(this.euler3Speed, t)).quaternion();
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.ensureDeltaCapacity(
                3 * 4 + //pos
                3 * 4 + //euler
                3 * 4 + //linearspeed
                3 * 4 + //strafespeed
                3 * 4 + //euler3speed
                1 //mode
                );
        bw.writeVector3(this.position);
        bw.writeEuler3(this.euler3);
        bw.writeVector3(this.linearSpeed);
        bw.writeVector3(this.strafeSpeed);
        bw.writeEuler3(this.euler3Speed);
        bw.writeByte(this.mode);
    }

    public final Euler3 getEuler3()
    {
        return UnmodifiableDecorator.wrap(euler3);
    }


    public final Vector3 getPosition()
    {
        return UnmodifiableDecorator.wrap(this.position);
    }

    public final Vector3 getLinearSpeed()
    {
        return UnmodifiableDecorator.wrap(this.linearSpeed);
    }

    public final Vector3 getStrafeSpeed()
    {
        return UnmodifiableDecorator.wrap(this.strafeSpeed);
    }

    public final Euler3 getEuler3Speed()
    {
        return UnmodifiableDecorator.wrap(euler3Speed);
    }

    public final byte getMode()
    {
        return this.mode;
    }

    public final boolean isValid()
    {
        return this.valid;
    }


    @Override
    public String toString()
    {
        return "MovementFrame{" +
                "position=" + position +
                ", euler3=" + euler3 +
                ", linearSpeed=" + linearSpeed +
                ", strafeSpeed=" + strafeSpeed +
                ", euler3Speed=" + euler3Speed +
                ", mode=" + mode +
                ", valid=" + valid +
                '}';
    }

    public Transform getTransform()
    {
        return new Transform(this.position, this.getRotation(), true);
    }
    public Transform getNextTransform(final float dt)
    {
        return new Transform(this.getFuturePosition(dt), this.getFutureEuler3(dt));
    }
}
