package io.github.luigeneric.linearalgebra.base;


import io.github.luigeneric.linearalgebra.utility.Mathf;

import static io.github.luigeneric.linearalgebra.utility.Mathf.normalizeAngle;

public class Euler3
{
    private float pitch;
    private float yaw;
    private float roll;

    public static final Euler3 ZERO = new Euler3(0,0,0);


    public Euler3(final double pitch, final double yaw, final double roll)
    {
        this((float) pitch, (float) yaw, (float) roll);
    }
    public Euler3(final float pitch, final float yaw, final float roll)
    {
        if (Float.isNaN(pitch)) throw new ArithmeticException("pitch is NaN");
        if (Float.isNaN(yaw)) throw new ArithmeticException("yaw is NaN");
        if (Float.isNaN(roll)) throw new ArithmeticException("roll is NaN");

        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }
    public Euler3(final float pitch, final float yaw)
    {
        this(pitch, yaw, 0f);
    }
    public Euler3(final Euler3 toCopyEuler3)
    {
        this.pitch = toCopyEuler3.pitch;
        this.yaw = toCopyEuler3.yaw;
        this.roll = toCopyEuler3.roll;
    }

    public Euler3 copy()
    {
        return new Euler3(this);
    }

    public static Euler3 zero()
    {
        return new Euler3(ZERO);
    }
    public static Euler3 identity()
    {
        return fromQuaternion(Quaternion.identity());
    }

    /**
     * Normalizes the current Euler
     * @param forceStraight if force straight
     * @return a reference to the current euler3
     */
    public Euler3 normalized(final boolean forceStraight)
    {
        float newPitch = normalizeAngle(pitch);
        float newYaw = yaw;
        float newRoll = roll;
        if (forceStraight && (double) Mathf.abs(newPitch) > 90.0)
        {
            newPitch = normalizeAngle(179.99f - newPitch);
            newYaw += 179.99f;
            newRoll += 179.99f;
        }
        this.setEuler3(newPitch, normalizeAngle(newYaw), normalizeAngle(newRoll));

        return this;
    }


    public void clamp(final Euler3 from, final Euler3 to)
    {
        final float pitch = Mathf.clamp(this.pitch, from.pitch, to.pitch);
        final float yaw = Mathf.clamp(this.yaw, from.yaw, to.yaw);
        final float roll = Mathf.clamp(this.roll, from.roll, to.roll);

        setEuler3(pitch, yaw, roll);
    }
    public void clamp(final Euler3 fromTo)
    {
        final float pitch = Mathf.clamp(this.pitch, -fromTo.pitch, fromTo.pitch);
        final float yaw = Mathf.clamp(this.yaw, -fromTo.yaw, fromTo.yaw);
        final float roll = Mathf.clamp(this.roll, -fromTo.roll, fromTo.roll);

        setEuler3(pitch, yaw, roll);
    }


    /**
     * Transforms this Euler into Quaternion representation
     * @apiNote formerly known as rotation()
     * @return a new Quaternion representation object equivalent to this euler angles
     */
    public Quaternion quaternion()
    {
        return Quaternion.euler(this.pitch, this.yaw, this.roll);
    }
    public Vector3 direction()
    {
        return quaternion().mult(StaticVectors.FORWARD);
    }

    /**
     * Calculates the direction out of the vector values into euler angles (degrees)
     * @param direction the direction vector, wont be affected
     * @return the direction but in euler degrees
     */
    public static Euler3 direction(final Vector3 direction)
    {
        final float yaw = Mathf.atan2(direction.getX(), direction.getZ()) * Mathf.radToDeg;
        final float pitch = -Mathf.atan2(
                direction.getY(),
                Mathf.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ())
        ) * Mathf.radToDeg;
        return new Euler3(pitch, yaw, 0f);
    }

    public float pitch()
    {
        return this.pitch;
    }
    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }
    public float yaw()
    {
        return this.yaw;
    }
    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }

    public void setEuler3(final Euler3 euler3)
    {
        this.setEuler3(euler3.pitch, euler3.yaw, euler3.roll);
    }
    public void setEuler3(final float pitch, final float yaw, final float roll)
    {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public float getRoll()
    {
        return this.roll;
    }
    public void setRoll(float roll)
    {
        this.roll = roll;
    }

    public static Euler3 rotateOverTime(final Euler3 start, final Euler3 changePerSecond, final float dt)
    {
        final Vector3 vector = changePerSecond.componentsToVector3();


        final Quaternion lhs = Quaternion.angleAxis(vector.magnitude() * dt, Vector3.normalize(vector));

        //return Euler3.quaternion(Quaternion.mult(lhs, start.quaternion()));
        return Euler3.fromQuaternion(lhs.mult(start.quaternion()));
    }
    public static Quaternion rotateOverTime(final Quaternion start, final Quaternion changePerSecond, final float dt)
    {
        final Quaternion lhs = Quaternion.slerp(StaticQuaternions.IDENTITY, changePerSecond, dt);
        return lhs.mult(start);
    }
    public static Euler3 rotateOverTimeLocal(final Euler3 start, final Euler3 changePerSecond, final float dt)
    {
        final Quaternion rhs = Quaternion.slerp(Quaternion.identity(), changePerSecond.quaternion(), dt);
        return Euler3.fromQuaternion(start.quaternion().mult(rhs));
    }

    public static Euler3 fromQuaternion(final Quaternion q)
    {
        final float qX2 = q.x() * q.x();
        final float qY2 = q.y() * q.y();
        final float qZ2 = q.z() * q.z();
        final float qW2 = q.w() * q.w();
        final float num5 = qX2 + qY2 + qZ2 + qW2;
        final float num6 = -q.z() * q.y() + q.x() * q.w();
        float pitch;
        float yaw;
        float roll = 0;
        if ((double)num6 > 0.499999 * (double)num5)
        {
            pitch = Mathf.piDiv2;
            yaw = 2f * Mathf.atan2(-q.z(), q.w());
            //roll = 0f;
        }
        else if ((double)num6 < -0.499999 * (double)num5)
        {
            pitch = -Mathf.piDiv2;
            yaw = -2f * Mathf.atan2(-q.z(), q.w());
            //roll = 0f;
        }
        else
        {
            pitch = Mathf.asin(2f * num6 / num5);
            yaw = Mathf.atan2(2f * q.y() * q.w() + 2f * q.z() * q.x(), -qX2 - qY2 + qZ2 + qW2);
            roll = Mathf.atan2(2f * q.z() * q.w() + 2f * q.y() * q.x(), -qX2 + qY2 - qZ2 + qW2);
        }
        final Euler3 e3 = new Euler3(pitch, yaw, roll);
        e3.mult(Mathf.radToDeg);
        return e3;
    }

    @SuppressWarnings("unused")
    public void componentsFromVector3(final Vector3 input)
    {
        this.pitch = input.getX();
        this.yaw = input.getY();
        this.roll = input.getZ();
    }

    public static Euler3 createComponentsFromVector3(final Vector3 input)
    {
        return new Euler3(input.getX(), input.getY(), input.getZ());
    }

    public Vector3 componentsToVector3()
    {
        return new Vector3(pitch, yaw, roll);
    }

    public Euler3 mult(final float num)
    {
        this.pitch *= num;
        this.yaw *= num;
        this.roll *= num;

        return this;
    }

    public static Euler3 mult(final Euler3 a, final float b)
    {
        return a.copy().mult(b);
        //return new Euler3(a.pitch * b, a.yaw * b, a.roll * b);
    }

    public static boolean equals(final Euler3 a, final Euler3 b)
    {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        return a.pitch == b.pitch && a.yaw == b.yaw && a.roll == b.roll;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if(obj instanceof Euler3 euler3)
        {
            return equals(this, euler3);
        }
        return false;
    }

    public Euler3 add(final Euler3 other)
    {
        this.pitch += other.pitch;
        this.yaw += other.yaw;
        this.roll += other.roll;

        return this;
    }
    public static Euler3 add(final Euler3 a, final Euler3 b)
    {
        return a.copy().add(b);
    }
    public static Euler3 sub(final Euler3 a, final Euler3 b)
    {
        return new Euler3(a.pitch - b.pitch, a.yaw - b.yaw, a.roll - b.roll);
    }

    @SuppressWarnings("unused")
    public static Euler3 negative(Euler3 a)
    {
        return new Euler3(-a.pitch, - a.yaw, -a.roll);
    }


    @Override
    public int hashCode()
    {
        return Float.hashCode(this.pitch) ^ (Float.hashCode(this.yaw) << 2) ^ (Float.hashCode(this.roll) >> 2);
    }


    @Override
    public String toString()
    {
        return "Euler3{" +
                "pitch=" + pitch +
                ", yaw=" + yaw +
                ", roll=" + roll +
                '}';
    }
}
