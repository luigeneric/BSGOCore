package io.github.luigeneric.linearalgebra.base;

import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.utils.ICopy;

import java.util.Objects;

import static io.github.luigeneric.linearalgebra.base.StaticVectors.*;

public class Vector3 implements ICopy<Vector3>
{
    private static final float K_EPSILON = 1E-05f;
    private static final String VECTOR_NULL_ERROR_MSG = "vector cannot be null";


    private float x;
    private float y;
    private float z;

    public Vector3(final float x, final float y, final float z)
    {
        if (Float.isNaN(x)) throw new ArithmeticException("x is NaN");
        if (Float.isNaN(y)) throw new ArithmeticException("y is NaN");
        if (Float.isNaN(z)) throw new ArithmeticException("z is NaN");

        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(final float x, final float y)
    {
        this(x ,y, 0);
    }
    public Vector3(final double x, final double y)
    {
        this((float) x, (float) y);
    }

    public Vector3(final Vector3 vec)
    {
        this(vec.x, vec.y, vec.z);
    }
    public Vector3()
    {
        this(ZERO);
    }

    public Vector3(final float[] r)
    {
        this(r[0], r[1], r[2]);
    }

    public static Vector3 scale(final Vector3 a, final Vector3 b)
    {
        return new Vector3(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    public static boolean isInsideMinMax(Vector3 point, Vector3 minVec, Vector3 maxVec)
    {
        return Mathf.isInsideValues(point.x, minVec.x, maxVec.x) &&
                Mathf.isInsideValues(point.y, minVec.y, maxVec.y) &&
                Mathf.isInsideValues(point.z, minVec.z, maxVec.z);

    }


    public static float[] toArray(final Vector3 v)
    {
        return new float[]{v.x, v.y, v.z};
    }
    public float[] toArray()
    {
        return toArray(this);
    }

    public static Vector3 abs(final Vector3 v)
    {
        return new Vector3(Math.abs(v.x), Math.abs(v.y), Math.abs(v.z));
    }

    public static Vector3 calcAcceleration(final Vector3 vel1, final Vector3 vel2, final float time)
    {
        return Vector3.sub(vel1, vel2).mult(1.0f / time);
    }

    public Vector3 scale(final Vector3 scale)
    {
        this.x *= scale.x;
        this.y *= scale.y;
        this.z *= scale.z;

        return this;
    }
    public void scale(final Euler3 scale)
    {
        this.x *= scale.pitch();
        this.y *= scale.yaw();
        this.z *= scale.getRoll();
    }


    public final float getIndex(final int index)
    {
        switch (index)
        {
            case 0 ->
            {
                return this.x;
            }
            case 1 ->
            {
                return this.y;
            }
            case 2 ->
            {
                return this.z;
            }
            default ->
            {
                throw new IndexOutOfBoundsException("Index for vectorAccess is out of bound: " + index);
            }
        }
    }
    public final void setIndex(final int index, final float value)
    {
        switch (index)
        {
            case 0 ->
            {
                this.x = value;
            }
            case 1 ->
            {
                this.y = value;
            }
            case 2 ->
            {
                this.z = value;
            }
            default ->
            {
                throw new IndexOutOfBoundsException("Index for vectorAccess is out of bound: " + index);
            }
        }
    }

    public static Vector3 rightUpForwardIndex(final int i)
    {
        switch (i)
        {
            case 0 ->
            {
                return RIGHT;
            }
            case 1 ->
            {
                return UP;
            }
            case 2 ->
            {
                return FORWARD;
            }
        }
        throw new IllegalArgumentException("index must be between 0 and 2, it was " + i);
    }

    public Vector3 scale(final float scale)
    {
        this.x *= scale;
        this.y *= scale;
        this.z *= scale;
        return this;
    }

    @Override
    public Vector3 copy()
    {
        return new Vector3(this);
    }

    /**
     * Returns a vector that is made from the largest components of two vectors. //unity
     * @param lhs
     * @param rhs
     * @return
     */
    public static Vector3 max(final Vector3 lhs, final Vector3 rhs)
    {
        return new Vector3(Mathf.max(lhs.x, rhs.x), Mathf.max(lhs.y, rhs.y), Mathf.max(lhs.z, rhs.z));
    }

    /**
     * Returns a vector that is made from the smallest components of two vectors.
     * @param lhs
     * @param rhs
     * @return
     */
    public static Vector3 min(final Vector3 lhs, final Vector3 rhs)
    {
        return new Vector3(Mathf.min(lhs.x, rhs.x), Mathf.min(lhs.y, rhs.y), Mathf.min(lhs.z, rhs.z));
    }

    public void set(final Vector3 vector3)
    {
        Objects.requireNonNull(vector3, "new position Vector is null");
        this.set(vector3.x, vector3.y, vector3.z);
    }
    public void set(final float x, final float y, final float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3 up()
    {
        return new Vector3(0f, 1f, 0f);
    }

    public static Vector3 left()
    {
        return new Vector3(-1f, 0f, 0f);
    }

    public static Vector3 down()
    {
        return new Vector3(0f, -1f, 0f);
    }

    public static Vector3 right()
    {
        return new Vector3(1f, 0f, 0f);
    }

    /**
     * Returns the angle in degrees between from and to
     * @param from
     * @param to
     * @return the angle in degree
     */
    public static float angle(final Vector3 from, final Vector3 to)
    {
        return Mathf.acos(Mathf.clampMin11(dot(Vector3.normalize(from), Vector3.normalize(to)))) * Mathf.radToDeg;
    }

    public static Vector3 clampMagnitude(final Vector3 vector, final float maxLength)
    {
        if (vector.sqMagnitude() > (maxLength * maxLength))
        {
            return Vector3.normalize(vector).mult(maxLength);
        }
        return vector;
    }

    public static Vector3 add(final Vector3 a, final Vector3 b)
    {
        Objects.requireNonNull(a, VECTOR_NULL_ERROR_MSG);
        Objects.requireNonNull(b, VECTOR_NULL_ERROR_MSG);
        return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vector3 add(final Vector3 a, final float x, final float y, final float z)
    {
        return new Vector3(a.x + x, a.y + y, a.z + z);
    }

    public static Vector3 add(final Vector3 a, float num)
    {
        Objects.requireNonNull(a, VECTOR_NULL_ERROR_MSG);
        if (Float.isNaN(num)) throw new ArithmeticException("num is NaN");
        return new Vector3(a.x + num, a.y + num, a.z + num);
    }

    public static Vector3 computeFromTo(final Vector3 a, final Vector3 b)
    {
        return Vector3.sub(b, a);
    }
    public static Vector3 sub(final Vector3 a, final Vector3 b)
    {
        Objects.requireNonNull(a, VECTOR_NULL_ERROR_MSG);
        Objects.requireNonNull(b, VECTOR_NULL_ERROR_MSG);
        return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
    }
    public static Vector3 sub(final Vector3 a, float num)
    {
        //Objects.requireNonNull(a, VECTOR_NULL_ERROR_MSG);
        if (Float.isNaN(num)) throw new ArithmeticException("num is NaN");

        return new Vector3(a.x - num, a.y - num, a.z - num);
    }
    public static Vector3 mult(final Vector3 v, final float num)
    {
        Objects.requireNonNull(v, VECTOR_NULL_ERROR_MSG);
        return new Vector3(v.x * num, v.y * num, v.z * num);
    }
    public static Vector3 mult(final float num, final Vector3 v)
    {
        return Vector3.mult(v, num);
    }

    public static Vector3 zero()
    {
        return new Vector3(ZERO);
    }

    public static Vector3 one()
    {
        return new Vector3(1, 1, 1);
    }
    public static Vector3 forward()
    {
        return new Vector3(0f, 0f, 1f);
    }

    public static float magnitude(final Vector3 vec)
    {
        Objects.requireNonNull(vec, VECTOR_NULL_ERROR_MSG);
        return Mathf.sqrt(sqMagnitude(vec));
    }
    public static float sqMagnitude(final Vector3 vec)
    {
        Objects.requireNonNull(vec, VECTOR_NULL_ERROR_MSG);
        return vec.x * vec.x + vec.y * vec.y + vec.z * vec.z;
    }

    public static Vector3 normalize(final Vector3 vec)
    {
        Objects.requireNonNull(vec, VECTOR_NULL_ERROR_MSG);
        final Vector3 toNormalize = vec.copy();
        return toNormalize.normalize();
    }
    public Vector3 normalize()
    {
        final float length = this.magnitude();
        if (length <= K_EPSILON)
        {
            this.set(ZERO);
            return this;
        }
        final float lenInv = 1.0f / length;

        this.x *= lenInv;
        this.y *= lenInv;
        this.z *= lenInv;

        return this;
    }

    public static Vector3 clamp(final Vector3 v, final float min, final float max)
    {
        final float tmpX = Mathf.clamp(v.x, min, max);
        final float tmpY= Mathf.clamp(v.y, min, max);
        final float tmpZ = Mathf.clamp(v.z, min, max);
        return new Vector3(tmpX, tmpY, tmpZ);
    }
    public Vector3 clamp(final float min, final float max)
    {
        this.x = Mathf.clamp(this.x, min, max);
        this.y = Mathf.clamp(this.y, min, max);
        this.z = Mathf.clamp(this.z, min, max);

        return this;
    }

    /**
     * Linearly interpolates between two points.
     *
     * Interpolates between the points a and b by the interpolant t.
     * The parameter t is clamped to the range [0, 1].
     *  This is most commonly used to find a point some fraction of the way along a line between two endpoints
     *  (e.g. to move an object gradually between those points).
     * @param a
     * @param b
     * @param t
     * @return When t = 0, Vector3.Lerp(a, b, t) returns a.
     * When t = 1, Vector3.Lerp(a, b, t) returns b.
     * When t = 0.5, Vector3.Lerp(a, b, t) returns the point midway between a and b.
     */
    public static Vector3 lerp(final Vector3 a, final Vector3 b, float t)
    {
        t = Mathf.clamp01(t);
        //without functions no new object creation so its faster
        //same as
        //final Vector3 ab = Vector3.subtract(b, a);
        //ab.mult(Mathf.clamp01(t));

        return new Vector3(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t);
    }

    public static Vector3 lerp(final Vector3 a, final Vector3 b, final Vector3 t)
    {
        return Vector3.add(a, Vector3.scale(b, t));
    }

    /**
     * Spherical interpolation
     * @param a can be non unit
     * @param b can be non unit
     * @param t value
     * @return
     */
    static public Vector3 sLerp(final Vector3 a, final Vector3 b, final float t)
    {
        //float magOf(Vector3 v) => MathF.Sqrt(Vector3.Dot(v, v));
        final float it = 1f - t; // inverse of t
        final float m0 = a.magnitude();
        final float m1 = b.magnitude();
        final float mm = m0 * m1; // combined magnitude
        if(mm == 0f)
        {
            return mult(a, it).add(mult(b, t)); // use lerp if one of the vectors is zero
        }
        var d = Vector3.dot(a, b) / mm; // unit dot
        if(1f - Math.abs(d) <= 1E-5f) { // abs(dot) is close to 1
            return d > 0f ? mult(a, it).add(mult(b, t)) // << use lerp for very small angles
                    : Quaternion.axisAngle(fastOrthogonal(a, true), Mathf.PI * t).mult((mult(a, (it + (m1/m0) * t))));
        }           // ^^ vectors are antiparallel, apply rotation on orthogonal axis, lerp mag
        final float th = Mathf.acos(d);
        final float s = Mathf.sin(th) * mm;
        final float j = Mathf.sin(it * th);
        final float k = Mathf.sin(t * th);
        // left-hand-side scalar part = mag lerp
        // right-hand-side vector part = actual slerp
        // (m0 * it + m1 * t) * (j * m1 * a + k * m0 * b) / s;
        // (productNum) * (vectorA + vectorB) / s
        final float productNum = (m0 * it + m1 * t);
        final Vector3 vectorA = mult(j * m1, a);
        final Vector3 vectorB = mult(k * m0, b);
        final Vector3 vecAPlusB = add(vectorA, vectorB);

        return vecAPlusB.mult(productNum).div(s);
    }


    /**
     *
     * @param a must be unit
     * @param b must be unit
     * @param t percentage
     * @implNote <a href="https://forum.unity.com/threads/slerp-demystified.1328832/">...</a>
     * @return
     */
    static public Vector3 sLerpUnit(final Vector3 a, final Vector3 b, final float t) {
        var d = Vector3.dot(a, b);
        if(1f - Math.abs(d) <= 1E-6f) // smaller epsilon
            return d > 0f ?
                    mult(a, (1f - t)).add(mult(b, t))
                    : Quaternion.axisAngle(fastOrthogonal(a, false), Mathf.PI * t).mult(a);
        final float th = Mathf.acos(d);
        final float s = Mathf.sin(th);
        final float j = Mathf.sin((1f - t) * th);
        final float k = Mathf.sin(t * th);
        // add(mult(j, a), mult(k, b))

        //return (j * a + k * b) / s;
        return add(mult(j, a), mult(k, b)).mult(1f/s);
    }

    /**
     * @implNote credits: <a href="https://forum.unity.com/threads/slerp-demystified.1328832/">...</a>
     * @param v
     * @param normalize
     * @return
     */
    public static Vector3 fastOrthogonal(final Vector3 v, final boolean normalize)
    {
        var sqr = v.x * v.x + v.y * v.y;
        if(sqr > 0f)
        {
            // (0,0,1) x (x,y,z)
            final float im = normalize? 1f / Mathf.sqrt(sqr) : 1f;
            return new Vector3(-v.y * im, v.x * im, 0f);
        }
        else
        {
            // (1,0,0) x (x,y,z)
            sqr = v.y * v.y + v.z * v.z;
            var im = normalize? 1f / Mathf.sqrt(sqr) : 1f;
            return new Vector3(0f, -v.z * im, v.y * im);
        }
    }



    public static float dot(final Vector3 a, final Vector3 b)
    {
        Objects.requireNonNull(a, "vector cannot be null");
        Objects.requireNonNull(b, "vector cannot be null");

        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Vector3 cross(final Vector3 a, final Vector3 b)
    {
        Objects.requireNonNull(a, "vector cannot be null");
        Objects.requireNonNull(b, "vector cannot be null");

        return new Vector3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
    }

    public Vector3 abs()
    {
        x = Mathf.abs(x);
        y = Mathf.abs(y);
        z = Mathf.abs(z);
        return this;
    }

    public Vector3 mod(final float value)
    {
        this.x %= value;
        this.y %= value;
        this.z %= value;
        return this;
    }

    public Vector3 add(final Vector3 vec)
    {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;

        return this;
    }
    public Vector3 add(float num)
    {
        //return add(this, num);

        this.x += num;
        this.y += num;
        this.z += num;

        return this;
    }

    public Vector3 sub(Vector3 vec)
    {
        //lol cant believe this was so long in this code
        //return sub(this, vec); <--- bug there is as setter missing

        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;

        return this;
    }
    public Vector3 sub(float num)
    {
        return sub(this, num);
    }
    public Vector3 mult(final float num)
    {
        this.x *= num;
        this.y *= num;
        this.z *= num;
        return this;
    }
    public static Vector3 div(Vector3 vec, float num)
    {
        if (num == 0) throw new IllegalArgumentException("num cannot be null");
        return mult(vec, 1.0f/num);
    }

    public Vector3 div(final float divBy)
    {
        if (divBy == 0)
            throw new IllegalArgumentException("divBy cannot be 0!");
        return mult(1f / divBy);
    }

    public static Vector3 div(final Vector3 a, final Vector3 b)
    {
        return new Vector3(a.x / b.x, a.y / b.y, a.z / b.z);
    }

    public Vector3 invert()
    {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;

        return this;
    }

    public static Vector3 invert(final Vector3 vec)
    {
        return new Vector3(-vec.x, -vec.y, -vec.z);
    }

    public float magnitude()
    {
        return magnitude(this);
    }
    public float sqMagnitude()
    {
        return sqMagnitude(this);
    }



    public static float getAngleAlongVector(final Vector3 vec)
    {
        if (vec.sqMagnitude() < 0.01)
        {
            return 0f;
        }
        return Mathf.radToDeg * Mathf.atan2(vec.x, vec.z);
    }



    public static float distance(final Vector3 a, final Vector3 b)
    {
        return Mathf.sqrt(distanceSquared(a, b));
    }

    public float distance(final Vector3 other)
    {
        return Vector3.distance(this, other);
    }
    public float distanceSq(final Vector3 other)
    {
        return Vector3.distanceSquared(this, other);
    }

    public static float distanceSquared(final Vector3 a, final Vector3 b)
    {
        final Vector3 sub = Vector3.sub(a, b);
        return sub.sqMagnitude();
    }


    public static float scalarTriple(final Vector3 a, final Vector3 b, final Vector3 c)
    {
        return Vector3.dot(Vector3.cross(a, b), c);
    }

    public Vector3 cross(Vector3 vec)
    {
        return cross(this, vec);
    }

    /**
     * Projects a vector onto another vector.
     * @param vector one vector
     * @param onNormal to project on
     * @return projection of vector on onNormal
     */
    public static Vector3 project(final Vector3 vector, final Vector3 onNormal)
    {
        final float num = onNormal.sqMagnitude(); //Vector3.dot(onNormal, onNormal);
        if (num < Mathf.EPSILON)
        {
            return Vector3.zero();
        }
        return Vector3.mult(onNormal, Vector3.dot(vector, onNormal) / num);
    }

    /**
     * perpendicular == senkrecht zwischen A und B_proj steht
     * @param len
     * @param dir
     * @return
     */
    public static Vector3 perpendicular(final Vector3 len, final Vector3 dir)
    {
        return Vector3.sub(len, project(len, dir));
    }

    /**
     * Same as cross by itself
     * @param v
     * @return cross of v x v
     */
    public static Vector3 perpendicular(final Vector3 v)
    {
        return Vector3.cross(v, v);
    }

    /**
     * Projects a vector onto a plane defined by a normal orthogonal to the plane.
     * @param vector
     * @param planeNormal
     * @return
     */
    public static Vector3 projectOntoPlane(final Vector3 vector, final Vector3 planeNormal)
    {
        return Vector3.sub(vector, Vector3.project(vector, planeNormal));
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Vector3 v3)
        {
            return this.x == v3.x && this.y == v3.y && this.z == v3.z;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Float.hashCode(this.x) ^ (Float.hashCode(this.y) << 2) ^ (Float.hashCode(this.z) >> 2);
    }

    @Override
    public String toString()
    {
        return "Vector3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public boolean isZero()
    {
        return this.x == 0 && this.y == 0 && this.z == 0;
    }

    /**
     * The IsZeroVector() function tests if its argument is a vector with a magnitude
     * sufficiently close to zero (according to some tolerance value.
     * Widening the tolerance intervals and treating near-parallel edges as parallel may
     * result in near-intersections being interpreted as intersections. Overall, this is much
     * more attractive than the alternative: two intersecting objects falsely reported as
     * nonintersecting due to the projection onto a zero-vector separating axis, for example.
     * @return
     */
    public boolean isZeroVector()
    {
        final float mag = this.magnitude();
        return mag < Mathf.EPSILON;
    }

    public void addX(final float value)
    {
        this.x += value;
    }
    public void addY(final float value)
    {
        this.y += value;
    }
    public void addZ(final float value)
    {
        this.z += value;
    }

    public void setX(final float value)
    {
        this.x = value;
    }
    public void setY(final float value)
    {
        this.y = value;
    }

    public void setZ(final float value)
    {
        this.z = value;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public float getZ()
    {
        return z;
    }
}