package io.github.luigeneric.linearalgebra.base;


import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.linearalgebra.utility.Matrix3x3;

import java.util.Objects;

import static io.github.luigeneric.linearalgebra.base.StaticQuaternions.IDENTITY;

/**
 * Port most of this from <a href="https://github.com/Unity-Technologies/UnityCsReference/blob/master/Runtime/Export/Math/Quaternion.cs">UnityEngine Reference</a>
 * <br>
 * Remaining stuff is from here and there, few from here aswell: <a href="https://en.wikipedia.org/wiki/Quaternion">Wikipedia</a>
 */
public class Quaternion
{
    public static final float K_EPSILON = 1E-06f;

    private float x;
    private float y;
    private float z;
    private float w;

    public Quaternion(final float x, final float y, final float z, final float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    public Quaternion(final double x, final double y, final double z, final double w)
    {
        this((float) x, (float) y, (float) z, (float) w);
    }

    public Quaternion(final Vector3 xyz, final float w)
    {
        this(xyz.getX(), xyz.getY(), xyz.getZ(), w);
    }

    public Quaternion(final Quaternion q)
    {
        this(q.x, q.y, q.z, q.w);
    }

    public Quaternion copy()
    {
        return new Quaternion(this);
    }

    public static Quaternion fromToRotation(final Vector3 fromDirection, final Vector3 toDirection)
    {
        final Vector3 axis = Vector3.cross(fromDirection, toDirection);
        final float angle = Vector3.angle(fromDirection, toDirection);
        return Quaternion.angleAxis(angle, axis.normalize());
    }


    public Vector3 xyz()
    {
        return new Vector3(x, y, z);
    }


    /**
     * Modifies the current quaternion
     * @param rotation the other rotation to use to rotate the current rotation
     * @return a reference to this/calling rotation
     */
    public Quaternion mult(final Quaternion rotation)
    {
        final float tmpX = this.x;
        final float tmpY = this.y;
        final float tmpZ = this.z;
        final float tmpW = this.w;

        this.x = tmpW * rotation.x + tmpX * rotation.w + tmpY * rotation.z - tmpZ * rotation.y;
        this.y = tmpW * rotation.y + tmpY * rotation.w + tmpZ * rotation.x - tmpX * rotation.z;
        this.z = tmpW * rotation.z + tmpZ * rotation.w + tmpX * rotation.y - tmpY * rotation.x;
        this.w = tmpW * rotation.w - tmpX * rotation.x - tmpY * rotation.y - tmpZ * rotation.z;

        return this;
    }


    public static Quaternion mult(final Quaternion lhs, final Quaternion rhs)
    {
        return lhs.copy().mult(rhs);
    }

    public void mult(final float f)
    {
        this.x *= f;
        this.y *= f;
        this.z *= f;
        this.w *= f;
    }
    public static Quaternion identity()
    {
        return new Quaternion(IDENTITY);
    }
    public static Quaternion right()
    {
        return new Euler3( 0, 89.9999f, 0).quaternion();
    }
    public static Quaternion left()
    {
        return new Euler3(0, -89.999f, 0).quaternion();
    }
    public static Quaternion randomRotation(final int num)
    {
        final int cleaned = num % 3;
        switch (cleaned)
        {
            case 0 ->
            {
                return identity();
            }
            case 1 ->
            {
                return right();
            }
            case 2 ->
            {
                return left();
            }
        }
        return identity();
    }

    public static Quaternion normalize(final Quaternion q)
    {
        Quaternion resQ = new Quaternion(q);
        resQ.normalize();
        return resQ;
    }

    public void normalize()
    {
        float scale = 1.0f / this.length();
        mult(scale);
    }

    /**
     * This weird way of inversing a quaternion is used by unity ... I dont know why...
     * ... unity quaternions are always normalized thats why the other inverse functions are keeping erroring (rounding errors)
     * ... forgot K_EPSILON omg
     * @param rotation rotation object to inverse, won't affect the given rotation
     * @return a new rotation object
     */
    public static Quaternion inverse(final Quaternion rotation)
    {
        return rotation.copy().inverse();
    }


    public Quaternion inverse()
    {
        final float lengthSq = this.lengthSquared();
        final boolean epsilonOK = (1.0f - lengthSq) <= K_EPSILON;

        final float num = epsilonOK ? 1.0F : lengthSq;
        final float mult = num / lengthSq;

        this.x *= -mult;
        this.y *= -mult;
        this.z *= -mult;
        this.w *= mult;

        return this;
    }

    public Vector3 getEulerAngles()
    {
        return Euler3.fromQuaternion(this).componentsToVector3();
    }

    public static Vector3 direction(final Quaternion quaternion)
    {
        return Quaternion.mult(quaternion, StaticVectors.FORWARD);
    }
    public Vector3 direction()
    {
        return direction(this);
    }

    @SuppressWarnings("unused")
    public void setEulerAngles(Vector3 vector3)
    {
        this.x = Quaternion.fromEulerRad(vector3.mult(Mathf.degToRad)).x;
        this.y = Quaternion.fromEulerRad(vector3.mult(Mathf.degToRad)).y;
        this.z = Quaternion.fromEulerRad(vector3.mult(Mathf.degToRad)).z;
        this.w = Quaternion.fromEulerRad(vector3.mult(Mathf.degToRad)).w;

    }
    public float length()
    {
        return Mathf.sqrt(lengthSquared());
    }
    public float lengthSquared()
    {
        return x * x + y * y + z * z + w * w;
    }


    private static Vector3 normalizeAngles(final Vector3 angles)
    {
        return new Vector3(normalizeAngle(angles.getX()),
                normalizeAngle(angles.getY()),
                normalizeAngle(angles.getZ()));
    }
    private static float normalizeAngle(float angle)
    {
        while (angle > 360)
            angle -= 360;
        while (angle < 0)
            angle += 360;
        return angle;
    }

    /**
     * Returns a rotation that rotates
     * z degrees around the z axis,
     * x degrees around the x axis,
     * y degrees around the y axis
     * (in that order)
     * @implNote Very close to the unity implementation!
     * @param euler Euler3 in Vector representation
     * @return a new quaternion object
     */
    public static Quaternion euler(final Vector3 euler)
    {
        final Vector3 tmpEuler = euler.copy();
        tmpEuler.mult(Mathf.degToRad);

        return Quaternion.fromEulerRad(tmpEuler);
    }

    /**
     * Returns a rotation that rotates z degrees around the z axis, x degrees around the x axis, and y degrees around the y axis (in that order)
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Quaternion euler(final float x, final float y, final float z)
    {
        final Vector3 rv = new Vector3(x, y, z);
        rv.mult(Mathf.degToRad);
        return Quaternion.fromEulerRad(rv);
    }


    /**
     * Quaternion from EulerRadians
     * @implNote is actually very close to the unity implementation, former versions
     * where either too accurate and unity implementation was less accurate or vice versa.
     * @param euler Vector3 euler in radians, won't affect the given vector object
     * @return a new Quaternion object that represents the given euler angles.
     */
    private static Quaternion fromEulerRad(final Vector3 euler)
    {
        final float yaw = euler.getX();
        final float pitch = euler.getY();
        final float roll = euler.getZ();
        final float rollOver2 = roll * 0.5f;
        final float sinRollOver2 = Mathf.sin(rollOver2);
        final float cosRollOver2 = Mathf.cos(rollOver2);
        final float pitchOver2 = pitch * 0.5f;
        final float sinPitchOver2 = Mathf.sin(pitchOver2);
        final float cosPitchOver2 = Mathf.cos(pitchOver2);
        final float yawOver2 = yaw * 0.5f;
        final float sinYawOver2 = Mathf.sin(yawOver2);
        final float cosYawOver2 = Mathf.cos(yawOver2);

        return new Quaternion(
                sinYawOver2 * cosPitchOver2 * cosRollOver2 + cosYawOver2 * sinPitchOver2 * sinRollOver2,
                cosYawOver2 * sinPitchOver2 * cosRollOver2 - sinYawOver2 * cosPitchOver2 * sinRollOver2,
                cosYawOver2 * cosPitchOver2 * sinRollOver2 - sinYawOver2 * sinPitchOver2 * cosRollOver2,
                cosYawOver2 * cosPitchOver2 * cosRollOver2 + sinYawOver2 * sinPitchOver2 * sinRollOver2
        );
    }

    public Vector3 mult(final Vector3 vec)
    {
        return Quaternion.mult(this, vec);
    }

    /**
     * Rotates a given Vektor v by the given rotation (matrix x vector = vector)
     *
     * @param rotation the rotation the vector needs to be rotated by, won't change the given Quaternion
     * @param point    the vector to rotate, won't affect the given vector
     * @return a new vector: v rotated by rotation
     * @see <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">Rotation matrix from axis and angle</a>
     */
    public static Vector3 mult(final Quaternion rotation, final Vector3 point)
    {
        final float x = rotation.x * 2f;
        final float y = rotation.y * 2f;
        final float z = rotation.z * 2f;
        final float xx = rotation.x * x;
        final float yy = rotation.y * y;
        final float zz = rotation.z * z;
        final float xy = rotation.x * y;
        final float xz = rotation.x * z;
        final float yz = rotation.y * z;
        final float wx = rotation.w * x;
        final float wy = rotation.w * y;
        final float wz = rotation.w * z;

        return new Vector3(
                (1f - (yy + zz)) * point.getX() + (xy - wz) * point.getY() + (xz + wy) * point.getZ(),
                (xy + wz) * point.getX() + (1f - (xx + zz)) * point.getY() + (yz - wx) * point.getZ(),
                (xz - wy) * point.getX() + (yz + wx) * point.getY() + (1f - (xx + yy)) * point.getZ()
        );
    }

    /**
     *
     * @param q normal Quaternion
     * @param axis call by reference
     * @return angle
     */
    private static float toAxisAngleRad(final Quaternion q, final Vector3 axis)
    {
        if (Mathf.abs(q.w) > 1.0f)
            q.normalize();
        final float angle = 2.0f * Mathf.acos(q.w);
        final float den = Mathf.sqrt(1.0 - q.w * q.w);
        if (den > 0.0001f)
        {
            axis.set(Vector3.div(q.xyz(), den));
        }
        else
        {
            axis.set(StaticVectors.RIGHT);
        }
        return angle;
    }

    /**
     * Dot product of two quaternions
     * @param a won't change a
     * @param b won't change b
     * @return scalar value
     */
    public static float dot(final Quaternion a, final Quaternion b)
    {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    }

    /**
     * Calculates the angle between two quaternions
     * @param a won't change a
     * @param b won't change b
     * @return the angle in deg between both rotations
     */
    public static float angle(final Quaternion a, final Quaternion b)
    {
        final float f = Quaternion.dot(a, b);
        return Mathf.acos(Mathf.min(Mathf.abs(f), 1f)) * 2f * Mathf.radToDeg;
    }

    /**
     * Rotates a quaternion to the other one
     * @param from from won't change
     * @param to won't change
     * @param maxDegreesDelta
     * @return new quaternion
     */
    public static Quaternion rotateTowards(final Quaternion from, final Quaternion to, final float maxDegreesDelta)
    {
        final float angle = Quaternion.angle(from, to);
        if (angle == 0f)
        {
            return to.copy();
        }
        final float t = Mathf.min(1f, maxDegreesDelta / angle);
        return Quaternion.slerpUnclamped(from, to, t);
    }


    /**
     * Sets every value to the negative version
     */
    public void negative()
    {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
    }

    /**
     *
     * @param a won't change a
     * @param b won't change b
     * @param tArg not important
     * @return a new Quaternion object
     */
    public static Quaternion slerp(final Quaternion a, final Quaternion b, final float tArg)
    {
        final float t = Mathf.clamp01(tArg);
        return slerpUnclamped(a, b, t);
    }

    /**
     * slerp quaternion
     * @param aArg won't change initial quaternion
     * @param bArg won't change initial quaternion
     * @param t idk
     * @return new quaternion
     */
    private static Quaternion slerpUnclamped(final Quaternion aArg, final Quaternion bArg, final float t)
    {
        final Quaternion a = aArg.copy();
        final Quaternion b = bArg.copy();


        if (a.lengthSquared() == 0.0f)
        {
            if (b.lengthSquared() == 0.0f)
            {
                return identity();
            }
            return b;
        }
        else if (b.lengthSquared() == 0.0f)
        {
            return a;
        }


        float cosHalfAngle = a.w * b.w + Vector3.dot(a.xyz(), b.xyz());

        if (cosHalfAngle >= 1.0f || cosHalfAngle <= -1.0f)
        {
            return a;
        }
        else if (cosHalfAngle < 0.0f)
        {
            b.negative();
            cosHalfAngle = -cosHalfAngle;
        }

        float blendA;
        float blendB;
        if (cosHalfAngle < 0.99f)
        {
            final float halfAngle = Mathf.acos(cosHalfAngle);
            final float sinHalfAngle = Mathf.sin(halfAngle);
            final float oneOverSinHalfAngle = 1.0f / sinHalfAngle;
            blendA = Mathf.sin(halfAngle * (1.0f - t)) * oneOverSinHalfAngle;
            blendB = Mathf.sin(halfAngle * t) * oneOverSinHalfAngle;
        }
        else
        {
            blendA = 1.0f - t;
            blendB = t;
        }
        final Vector3 tmpV = new Vector3(a.xyz().mult(blendA).add(b.xyz().mult(blendB)));
        final float tmpF = blendA * a.w + blendB * b.w;
        final Quaternion result = new Quaternion(tmpV, tmpF);
        if (result.lengthSquared() > 0.0f)
            return normalize(result);
        else
            return identity();
    }

    /**
     * Creates a rotation which rotates /angle/ degrees around /axis/.
     * @param degrees the degrees to rotate
     * @param axisArg the axis, won't be affected
     * @return a new Quaternion object
     */
    public static Quaternion angleAxis(final float degrees, final Vector3 axisArg)
    {
        /*
        final var axis = axisArg.copy();
        axis.normalize();
        final float rad = degrees * Mathf.degToRad * 0.5f;
        axis.mult(Mathf.sin(rad));
        return new Quaternion(axis, Mathf.cos(rad));
         */
        return axisAngle(axisArg, Mathf.degToRad * degrees);
    }

    /**
     * Returns Quaternion after rotated axis around degrees in radians
     * @param axisArg axis arg, won't be affected
     * @param rad degrees in radians
     * @return a new Quaternion object with it's axis rotated around rad
     */
    public static Quaternion axisAngle(final Vector3 axisArg, final float rad)
    {
        final Vector3 axis = axisArg.copy();
        axis.normalize();
        final float radFinal = rad * 0.5f;
        axis.mult(Mathf.sin(radFinal));
        return new Quaternion(axis, Mathf.cos(radFinal));
    }

    public static Quaternion fromMatrix(final Matrix3x3 matrix)
    {
        float x,y,z,w;
        // Get the trace of the matrix
        float r, s, trace = matrix.getTrace();

        if (trace < 0.0f) {
            if (matrix.at(1, 1) > matrix.at(0, 0)) {
                if (matrix.at(2, 2) > matrix.at(1, 1)) {
                    r = Mathf.sqrt(matrix.at(2, 2) - matrix.at(0, 0) - matrix.at(1, 1) + 1.0f);
                    s = 0.5f / r;

                    // Compute the quaternion
                    x = (matrix.at(2, 0) + matrix.at(0, 2)) * s;
                    y = (matrix.at(1, 2) + matrix.at(2, 1)) * s;
                    z = 0.5f * r;
                    w = (matrix.at(1, 0) - matrix.at(0, 1)) * s;
                } else {
                    r = Mathf.sqrt(matrix.at(1, 1) - matrix.at(2, 2) - matrix.at(0, 0) + 1.0f);
                    s = 0.5f / r;

                    // Compute the quaternion
                    x = (matrix.at(0, 1) + matrix.at(1, 0)) * s;
                    y = 0.5f * r;
                    z = (matrix.at(1, 2) + matrix.at(2, 1)) * s;
                    w = (matrix.at(0, 2) - matrix.at(2, 0)) * s;
                }
            } else if (matrix.at(2, 2) > matrix.at(0, 0)) {
                r = Mathf.sqrt(matrix.at(2, 2) - matrix.at(0, 0) - matrix.at(1, 1) + 1.0f);
                s = 0.5f / r;

                // Compute the quaternion
                x = (matrix.at(2, 0) + matrix.at(0, 2)) * s;
                y = (matrix.at(1, 2) + matrix.at(2, 1)) * s;
                z = 0.5f * r;
                w = (matrix.at(1, 0) - matrix.at(0, 1)) * s;
            } else {
                r = Mathf.sqrt(matrix.at(0, 0) - matrix.at(1, 1) - matrix.at(2, 2) + 1.0f);
                s = 0.5f / r;

                // Compute the quaternion
                x = 0.5f * r;
                y = (matrix.at(0, 1) + matrix.at(1, 0)) * s;
                z = (matrix.at(2, 0) - matrix.at(0, 2)) * s;
                w = (matrix.at(2, 1) - matrix.at(1, 2)) * s;
            }
        } else {
            r = Mathf.sqrt(trace + 1.0f);
            s = 0.5f / r;

            // Compute the quaternion
            x = (matrix.at(2, 1) - matrix.at(1, 2)) * s;
            y = (matrix.at(0, 2) - matrix.at(2, 0)) * s;
            z = (matrix.at(1, 0) - matrix.at(0, 1)) * s;
            w = 0.5f * r;
        }

        return new Quaternion(x,y,z,w);
    }

    public static boolean equals(Quaternion lhs, Quaternion rhs)
    {
        if (lhs == null && rhs == null) return true;
        if (lhs == null || rhs == null) return false;

        //return Quaternion.dot(lhs, rhs) > 0.999999f;
        return lhs.x == rhs.x && lhs.y == rhs.y && lhs.z == rhs.z && lhs.w == rhs.w;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (obj instanceof Quaternion)
        {
            return equals((Quaternion) obj);
        }
        return false;
    }

    public boolean equals(Quaternion q)
    {
        return equals(this, q);
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(x, y, z, w);
    }

    @Override
    public String toString()
    {
        return "Quaternion{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                '}';
    }

    public float x()
    {
        return x;
    }

    public float y()
    {
        return y;
    }

    public float z()
    {
        return z;
    }

    public float w()
    {
        return w;
    }


    /**
     *
     * @param axis  axis is call by reference
     * @return the angle
     */
    public float toAngleAxis(final Vector3 axis)
    {
        final float angle = Quaternion.toAxisAngleRad(this, axis);
        return angle * Mathf.radToDeg;
    }

    /**
     * Sets the current rotation params to the given ones
     * @param newRotation the Quaternion with the new values
     * @return a reference to the this object
     * @throws NullPointerException if newRotation is null
     */
    public Quaternion setRotation(final Quaternion newRotation) throws NullPointerException
    {
        Objects.requireNonNull(newRotation, "currentRotation Quaternion cannot be null");
        this.x = newRotation.x;
        this.y = newRotation.y;
        this.z = newRotation.z;
        this.w = newRotation.w;
        return this;
    }
}
