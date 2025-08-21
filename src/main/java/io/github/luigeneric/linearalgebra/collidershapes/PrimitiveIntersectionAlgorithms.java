package io.github.luigeneric.linearalgebra.collidershapes;

import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.intersection.ClosestPointComputations;
import io.github.luigeneric.linearalgebra.intersection.IntersectionAlgorithms;
import io.github.luigeneric.linearalgebra.utility.FloatWrapper;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.linearalgebra.utility.Matrix3x3;

import static io.github.luigeneric.linearalgebra.utility.Mathf.EPSILON;

public final class PrimitiveIntersectionAlgorithms
{
    private PrimitiveIntersectionAlgorithms(){}

    public static boolean testSphereCapsulePrimitive(final SphereCollider s, final CapsuleCollider capsuleCollider)
    {
        // Compute (squared) distance between sphere center and capsule line segment
        final float dist2 = ClosestPointComputations.sqDistPointSegment(capsuleCollider.getA(), capsuleCollider.getB(), s.getCenter());
        // If (squared) distance smaller than (squared) sum of radii, they collide
        final float radius = s.getRadius() + capsuleCollider.getRadius();
        return dist2 <= radius * radius;
    }

    public static boolean intersectSphereSphere(final SphereCollider left, final SphereCollider right)
    {
        //// Calculate squared distance between centers
        //final Vector3 d = Vector3.sub(left.getCenter(), right.getCenter());
        //final float dist2 = d.sqMagnitude();

        //// Spheres intersect if squared distance is less than squared sum of radii
        //final float radiusSum = left.getRadius() + right.getRadius();
        //return dist2 <= radiusSum * radiusSum;

        return intersectSphereSphere(left.getCenter(), left.getRadius(), right.getCenter(), right.getRadius());
    }
    public static boolean intersectSphereSphere(final Vector3 c1, final float r1, final Vector3 c2, final float r2)
    {
        final float distSq = c1.distanceSq(c2);
        final float radiusSum = r1 + r2;
        return distSq <= (radiusSum * radiusSum);
    }

    public static boolean intersectOBBOBB(final OBBCollider a, final OBBCollider b)
    {
        //idea: prune with sphere
        final boolean mayIntersect = PrimitiveIntersectionAlgorithms
                .intersectSphereSphere(a.getGlobalCenter(), a.getMaximumRadius(), b.getGlobalCenter(), b.getMaximumRadius());
        if (!mayIntersect)
            return false;

        float ra, rb;
        final float[][] R = new float[3][3];
        final float[][] AbsR = new float[3][3];

        // Compute rotation matrix expressing b in a’s coordinate frame
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                R[i][j] = Vector3.dot(a.getLocalAxes(i), b.getLocalAxes(j));

        // Compute translation vector t
        // also nur ab??
        final Vector3 t = Vector3.sub(b.getGlobalCenter(), a.getGlobalCenter());
        // Bring translation into a’s coordinate frame
        final float tmpX = Vector3.dot(t, a.getLocalAxes(0));
        final float tmpY = Vector3.dot(t, a.getLocalAxes(1));
        final float tmpZ = Vector3.dot(t, a.getLocalAxes(2));
        t.set(tmpX, tmpY, tmpZ);

        // Compute common subexpressions. Add in an epsilon term to
        // counteract arithmetic errors when two edges are parallel and
        // their cross product is (near) null (see text for details)
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                AbsR[i][j] = Mathf.abs(R[i][j]) + EPSILON;
            }
        }
        // Test axes L = A0, L = A1, L = A2
        //gecheckt
        for (int i = 0; i < 3; i++)
        {
            ra = a.getHalfWidthExtents().getIndex(i);
            rb = b.getHalfWidthExtents().getIndex(0) * AbsR[i][0] +
                    b.getHalfWidthExtents().getIndex(1) * AbsR[i][1] + b.getHalfWidthExtents().getIndex(2) * AbsR[i][2];
            if (Mathf.abs(t.getIndex(i)) > ra + rb)
            {
                return false;
            }
        }
        // Test axes L = B0, L = B1, L = B2
        //gecheckt
        for (int i = 0; i < 3; i++)
        {
            ra = a.getHalfWidthExtents().getIndex(0) * AbsR[0][i] +
                    a.getHalfWidthExtents().getIndex(1) * AbsR[1][i] + a.getHalfWidthExtents().getIndex(2) * AbsR[2][i];
            rb = b.getHalfWidthExtents().getIndex(i);
            if (Mathf.abs(t.getIndex(0) * R[0][i] + t.getIndex(1) * R[1][i] + t.getIndex(2) * R[2][i]) > ra + rb)
            {
                return false;
            }
        }
        // Test axis L = A0 x B0
        //gecheckt
        ra = a.getHalfWidthExtents().getIndex(1) * AbsR[2][0] + a.getHalfWidthExtents().getIndex(2) * AbsR[1][0];
        rb = b.getHalfWidthExtents().getIndex(1) * AbsR[0][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[0][1];
        if (Mathf.abs(t.getIndex(2) * R[1][0] - t.getIndex(1) * R[2][0]) > ra + rb)
        {
            return false;
        }
        // Test axis L = A0 x B1
        //gecheckt
        ra = a.getHalfWidthExtents().getIndex(1) * AbsR[2][1] + a.getHalfWidthExtents().getIndex(2) * AbsR[1][1];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[0][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[0][0];
        if (Mathf.abs(t.getIndex(2) * R[1][1] - t.getIndex(1) * R[2][1]) > ra + rb)
        {
            return false;
        }
        // Test axis L = A0 x B2
        //
        ra = a.getHalfWidthExtents().getIndex(1) * AbsR[2][2] + a.getHalfWidthExtents().getIndex(2) * AbsR[1][2];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[0][1] + b.getHalfWidthExtents().getIndex(1) * AbsR[0][0];
        if (Mathf.abs(t.getIndex(2) * R[1][2] - t.getIndex(1) * R[2][2]) > ra + rb)
        {
            return false;
        }
        // Test axis L = A1 x B0
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[2][0] + a.getHalfWidthExtents().getIndex(2) * AbsR[0][0];
        rb = b.getHalfWidthExtents().getIndex(1) * AbsR[1][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[1][1];
        if (Mathf.abs(t.getIndex(0) * R[2][0] - t.getIndex(2) * R[0][0]) > ra + rb)
        {
            return false;
        }
        // Test axis L = A1 x B1

        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[2][1] + a.getHalfWidthExtents().getIndex(2) * AbsR[0][1];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[1][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[1][0];
        if (Mathf.abs(t.getIndex(1) * R[2][1] - t.getIndex(2) * R[0][1]) > ra + rb)
        {
            return false;
        }

        // Test axis L = A1 x B2
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[2][2] + a.getHalfWidthExtents().getIndex(2) * AbsR[0][2];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[1][1] + b.getHalfWidthExtents().getIndex(1) * AbsR[1][0];
        if (Mathf.abs(t.getIndex(1) * R[2][2] - t.getIndex(2) * R[0][2]) > ra + rb)
        {
            return false;
        }

        // Test axis L = A2 x B0
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[1][0] + a.getHalfWidthExtents().getIndex(1) * AbsR[0][0];
        rb = b.getHalfWidthExtents().getIndex(1) * AbsR[2][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[2][1];
        if (Mathf.abs(t.getIndex(1) * R[0][0] - t.getIndex(0) * R[1][0]) > ra + rb)
        {
            return false;
        }

        // Test axis L = A2 x B1
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[1][1] + a.getHalfWidthExtents().getIndex(1) * AbsR[0][1];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[2][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[2][0];
        if (Mathf.abs(t.getIndex(1) * R[0][1] - t.getIndex(0) * R[1][1]) > ra + rb)
        {
            return false;
        }

        // Test axis L = A2 x B2
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[1][2] + a.getHalfWidthExtents().getIndex(1) * AbsR[0][2];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[2][1] + b.getHalfWidthExtents().getIndex(1) * AbsR[2][0];
        if (Mathf.abs(t.getIndex(1) * R[0][2] - t.getIndex(0) * R[1][2]) > ra + rb)
        {
            return false;
        }


        // Since no separating axis is found, the OBBs must be intersecting
        return true;
    }

    public static boolean intersectOBBOBB2(final OBBCollider a, final OBBCollider b)
    {
        float s;
        float bestSep = Float.MAX_VALUE;
        Vector3 bestAxis;
        float tl;
        float ra2, rb2;
        float ra, rb;
        final float[][] R = new float[3][3];
        final float[][] AbsR = new float[3][3];
        final Matrix3x3 absR3x3 = new Matrix3x3(AbsR);
        final Transform aInv = Transform.inverse(a.getTransform());

        // Compute rotation matrix expressing b in a’s coordinate frame
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                R[i][j] = Vector3.dot(a.getLocalAxes(i), b.getLocalAxes(j));

        //this is equals to the rotation R
        //final Quaternion rOutM = Quaternion.fromMatrix(new Matrix3x3(R));


        //final Quaternion q2 = Quaternion.mult(a.getTransform().getRotation().inverseCopy(), b.getTransform().getRotation());

        // Compute translation vector t
        // also nur ab??
        final Vector3 t = Vector3.sub(b.getGlobalCenter(), a.getGlobalCenter());
        // Bring translation into a’s coordinate frame
        final float tmpX = Vector3.dot(t, a.getLocalAxes(0));
        final float tmpY = Vector3.dot(t, a.getLocalAxes(1));
        final float tmpZ = Vector3.dot(t, a.getLocalAxes(2));
        t.set(tmpX, tmpY, tmpZ);
        final Vector3 t2 = aInv.getRotation().mult(Vector3.sub(b.getGlobalCenter(), a.getGlobalCenter()));


        // Compute common subexpressions. Add in an epsilon term to
        // counteract arithmetic errors when two edges are parallel and
        // their cross product is (near) null (see text for details)
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                AbsR[i][j] = Mathf.abs(R[i][j]) + EPSILON;
            }
        }
        final Quaternion rOutMAbs = Quaternion.fromMatrix(new Matrix3x3(AbsR));


        // Test axes L = A0, L = A1, L = A2
        //gecheckt
        for (int i = 0; i < 3; i++)
        {
            Vector3 l = Vector3.rightUpForwardIndex(i);
            ra = a.getHalfWidthExtents().getIndex(i);
            ra2 = Vector3.dot(l, a.getHalfWidthExtents());
            rb = b.getHalfWidthExtents().getIndex(0) * AbsR[i][0] +
                    b.getHalfWidthExtents().getIndex(1) * AbsR[i][1] + b.getHalfWidthExtents().getIndex(2) * AbsR[i][2];
            rb2 = Mathf.abs(Vector3.dot(l, absR3x3.mult(b.getHalfWidthExtents())));
            tl = Mathf.abs(t.getIndex(i));


            if (tl > ra + rb)
            {
                return false;
            }
            s = Mathf.abs(tl - (ra + rb));
            if (s < bestSep)
            {
                bestSep = s;
                bestAxis = a.getLocalAxes(i).copy();
            }
        }
        // Test axes L = B0, L = B1, L = B2
        //gecheckt
        for (int i = 0; i < 3; i++)
        {
            ra = a.getHalfWidthExtents().getIndex(0) * AbsR[0][i] +
                    a.getHalfWidthExtents().getIndex(1) * AbsR[1][i] + a.getHalfWidthExtents().getIndex(2) * AbsR[2][i];
            rb = b.getHalfWidthExtents().getIndex(i);
            tl = Mathf.abs(t.getIndex(0) * R[0][i] + t.getIndex(1) * R[1][i] + t.getIndex(2) * R[2][i]);
            if (tl > ra + rb)
            {
                return false;
            }
            s = Mathf.abs(tl - (ra + rb));
            if (s < bestSep)
            {
                bestSep = s;
                bestAxis = b.getLocalAxes(i).copy();
            }
        }

        // Test axis L = A0 x B0
        //gecheckt
        ra = a.getHalfWidthExtents().getIndex(1) * AbsR[2][0] + a.getHalfWidthExtents().getIndex(2) * AbsR[1][0];
        rb = b.getHalfWidthExtents().getIndex(1) * AbsR[0][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[0][1];
        tl = Mathf.abs(t.getIndex(2) * R[1][0] - t.getIndex(1) * R[2][0]);
        if (tl > ra + rb)
        {
            return false;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            bestSep = s;
            bestAxis = Vector3.cross(a.getLocalRight(), b.getLocalRight());
        }

        // Test axis L = A0 x B1
        //gecheckt
        ra = a.getHalfWidthExtents().getIndex(1) * AbsR[2][1] + a.getHalfWidthExtents().getIndex(2) * AbsR[1][1];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[0][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[0][0];
        tl = Mathf.abs(t.getIndex(2) * R[1][1] - t.getIndex(1) * R[2][1]);
        if (tl > ra + rb)
        {
            return false;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            bestSep = s;
            bestAxis = Vector3.cross(a.getLocalRight(), b.getLocalUp());
        }


        // Test axis L = A0 x B2
        ra = a.getHalfWidthExtents().getIndex(1) * AbsR[2][2] + a.getHalfWidthExtents().getIndex(2) * AbsR[1][2];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[0][1] + b.getHalfWidthExtents().getIndex(1) * AbsR[0][0];
        tl = Mathf.abs(t.getIndex(2) * R[1][2] - t.getIndex(1) * R[2][2]);
        if (tl > ra + rb)
        {
            return false;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            bestSep = s;
            bestAxis = Vector3.cross(a.getLocalRight(), b.getLocalForward());
        }


        // Test axis L = A1 x B0
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[2][0] + a.getHalfWidthExtents().getIndex(2) * AbsR[0][0];
        rb = b.getHalfWidthExtents().getIndex(1) * AbsR[1][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[1][1];
        tl  = Mathf.abs(t.getIndex(0) * R[2][0] - t.getIndex(2) * R[0][0]);
        if (tl > ra + rb)
        {
            return false;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            bestSep = s;
            bestAxis = Vector3.cross(a.getLocalUp(), b.getLocalRight());
        }


        // Test axis L = A1 x B1
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[2][1] + a.getHalfWidthExtents().getIndex(2) * AbsR[0][1];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[1][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[1][0];
        tl = Mathf.abs(t.getIndex(1) * R[2][1] - t.getIndex(2) * R[0][1]);
        if (tl > ra + rb)
        {
            return false;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            bestSep = s;
            bestAxis = Vector3.cross(a.getLocalUp(), b.getLocalUp());
        }

        // Test axis L = A1 x B2
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[2][2] + a.getHalfWidthExtents().getIndex(2) * AbsR[0][2];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[1][1] + b.getHalfWidthExtents().getIndex(1) * AbsR[1][0];
        tl = Mathf.abs(t.getIndex(1) * R[2][2] - t.getIndex(2) * R[0][2]);
        if (tl > ra + rb)
        {
            return false;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            bestSep = s;
            bestAxis = Vector3.cross(a.getLocalUp(), b.getLocalForward());
        }

        // Test axis L = A2 x B0
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[1][0] + a.getHalfWidthExtents().getIndex(1) * AbsR[0][0];
        rb = b.getHalfWidthExtents().getIndex(1) * AbsR[2][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[2][1];
        tl = Mathf.abs(t.getIndex(1) * R[0][0] - t.getIndex(0) * R[1][0]);
        if (tl > ra + rb)
        {
            return false;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            bestSep = s;
            bestAxis = Vector3.cross(a.getLocalForward(), b.getLocalRight());
        }

        // Test axis L = A2 x B1
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[1][1] + a.getHalfWidthExtents().getIndex(1) * AbsR[0][1];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[2][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[2][0];
        tl = Mathf.abs(t.getIndex(1) * R[0][1] - t.getIndex(0) * R[1][1]);
        if (tl > ra + rb)
        {
            return false;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            bestSep = s;
            bestAxis = Vector3.cross(a.getLocalForward(), b.getLocalUp());
        }

        // Test axis L = A2 x B2
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[1][2] + a.getHalfWidthExtents().getIndex(1) * AbsR[0][2];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[2][1] + b.getHalfWidthExtents().getIndex(1) * AbsR[2][0];
        tl = Mathf.abs(t.getIndex(1) * R[0][2] - t.getIndex(0) * R[1][2]);
        if (tl > ra + rb)
        {
            return false;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            bestSep = s;
            bestAxis = Vector3.cross(a.getLocalForward(), b.getLocalForward());
        }


        // Since no separating axis is found, the OBBs must be intersecting
        return true;
    }

    /**
     * Calculates the separation of s=|t⋅l|–(|a⋅l|+|(C∗b)⋅l|)
     * C*b is already given since b doesnt have to be rotated over and over again
     * @param t translation vector
     * @param l axes vector
     * @param a halfwidth extents of a
     * @param b halfwidth extents of b
     * @return s, with s=|t⋅l|–(|a⋅l|+|(C∗b)⋅l|). If separated, s > 0
     */
    private static float separated(final Vector3 t, final Vector3 l, final Vector3 a, final Vector3 b)
    {
        final float tl = Mathf.abs(Vector3.dot(t, l));
        final float al = Mathf.abs(Vector3.dot(a, l));
        //(C*b)*l, b is already rotated
        final float bl = Mathf.abs(Vector3.dot(b, l));
        return tl - (al + bl);
    }

    /**
     * Returns true if sphere s intersects triangle ABC, false otherwise.
     * The point p on abc closest to the sphere center is also returned
     * @param s Sphere
     * @param t Triangle
     * @param p call by reference
     * @return Returns true if sphere s intersects triangle ABC, false otherwise.
     */
    public static boolean testSphereTriangle(final SphereCollider s, final Triangle t, final Vector3 p)
    {
        p.set(IntersectionAlgorithms.closestPtPointTriangle(t, s.getCenter()));

        // Sphere and triangle intersect if the (squared) distance from sphere
        // center to point p is less than the (squared) sphere radius
        final Vector3 v = Vector3.sub(p, s.getCenter());
        return Vector3.dot(v, v) <= s.getRadius() * s.getRadius();
    }

    public static boolean testSphereTriangle(final SphereCollider s, final Triangle t)
    {
        final Vector3 p = IntersectionAlgorithms.closestPtPointTriangle(t, s.getCenter());
        // Sphere and triangle intersect if the (squared) distance from sphere
        // center to point p is less than the (squared) sphere radius
        final Vector3 v = Vector3.sub(p, s.getCenter());
        return Vector3.dot(v, v) <= s.getRadius() * s.getRadius();
    }

    /**
     * Intersects ray r = p + td, |d| = 1, with sphere s and, if intersecting,
     * returns t value of intersection and intersection point q
     * @param p
     * @param d
     * @param s
     * @param t call by ref
     * @param q call by ref
     * @return
     */
    public static boolean intersectRaySphere(final Vector3 p, final Vector3 d, final SphereCollider s, final FloatWrapper t, final Vector3 q)
    {
        final Vector3 m = Vector3.sub(p, s.getCenter());
        final float b = Vector3.dot(m, d);
        final float c = Vector3.dot(m, m) - s.getRadius() * s.getRadius();
        // Exit if r’s origin outside s (c > 0) and r pointing away from s (b > 0)
        if (c > 0.0f && b > 0.0f) return false;
        final float discr = b*b - c;
        // A negative discriminant corresponds to ray missing sphere
        if (discr < 0.0f) return false;
        // Ray now found to intersect sphere, compute smallest t value of intersection
        float tmpt = -b - Mathf.sqrt(discr);
        // If t is negative, ray started inside sphere so clamp t to zero
        if (tmpt < 0.0f) tmpt = 0.0f;


        t.setValue(tmpt);
        q.set(Vector3.add(p, Vector3.mult(tmpt, d)));

        return true;
    }

    public static boolean testRaySphere(final Vector3 p, final Vector3 d, final SphereCollider s)
    {
        Vector3 m = Vector3.sub(p, s.getCenter());
        //float c = Vector3.dot(m, m) - s.getRadius() * s.getRadius();
        final float c = m.sqMagnitude() - s.getRadius() * s.getRadius();
        // If there is definitely at least one real root, there must be an intersection
        if (c <= 0.0f) return true;
        final float b = Vector3.dot(m, d);
        // Early exit if ray origin outside sphere and ray pointing away from sphere
        if (b > 0.0f) return false;
        final float disc = b*b - c;
        // A negative discriminant corresponds to ray missing sphere
        if (disc < 0.0f) return false;
        // Now ray must hit sphere
        return true;
    }

    public static boolean testRaySphere(final Ray ray, final SphereCollider s)
    {
        return testRaySphere(ray.origin(), ray.direction(), s);
    }


    /**
     * Intersect ray R(t) = p + t*d against AABB a.
     * @param p ray start
     * @param d ray direction
     * @param MAX_TRAVEL_DISTANCE ray travel distance(for segment)
     * @param min aabb min
     * @param max aabb max
     * @param tmin call by ref
     * @param q call by ref
     * @return When intersecting, return true, false otherwise
     */
    public static boolean intersectRayAABB(final Vector3 p, final Vector3 d, final float MAX_TRAVEL_DISTANCE,
                                           final Vector3 min, final Vector3 max,
                                           final FloatWrapper tmin, final Vector3 q)
    {
        // set to -FLT_MAX to get first hit on line
        tmin.setValue(0);
        // set to max distance ray can travel (for segment)
        float tmax = MAX_TRAVEL_DISTANCE;
        // For all three slabs
        for (int i = 0; i < 3; i++) {
            if (Mathf.abs(d.getIndex(i)) < EPSILON) {
                // Ray is parallel to slab. No hit if origin not within slab
                if (p.getIndex(i) < min.getIndex(i) || p.getIndex(i) > max.getIndex(i))
                {
                    return false;
                }
            }
            else
            {
                // Compute intersection t value of ray with near and far plane of slab
                final float ood = 1.0f / d.getIndex(i);
                float t1 = (min.getIndex(i) - p.getIndex(i)) * ood;
                float t2 = (max.getIndex(i) - p.getIndex(i)) * ood;
                // Make t1 be intersection with near plane, t2 with far plane
                if (t1 > t2)
                {
                    //Swap(t1, t2);
                    final float tmp = t1;
                    t1 = t2;
                    t2 = tmp;
                }
                // Compute the intersection of slab intersection intervals
                tmin.setValue(Mathf.max(tmin.getValue(), t1));
                tmax = Mathf.min(tmax, t2);
                // Exit with no collision as soon as slab intersection becomes empty
                if (tmin.getValue() > tmax)
                {
                    return false;
                }
            }
        }
        // Ray intersects all 3 slabs. Return point (q) and intersection t value (tmin)
        //q = p + d * tmin;
        q.set(Vector3.add(p, Vector3.mult(d, tmin.getValue())));
        return true;
    }

    public static boolean intersectCapsuleObbDifferentApproach(final CapsuleCollider capsule, final OBBCollider obb)
    {
        LineSegment lineSegmentArg = new LineSegment(capsule.getA(), capsule.getB());

        //this is the clamped segment inside the obb
        final Vector3 closestPtA = ClosestPointComputations
                .closestPtPointOBB(lineSegmentArg.a(), obb);
        final Vector3 closestPtB = ClosestPointComputations
                .closestPtPointOBB(lineSegmentArg.b(), obb);

        //now check this clamped segment against capsule

        return intersectCapsuleCapsule(
                capsule.getA(), capsule.getB(), capsule.getRadius(),
                closestPtA, closestPtB, 0);
    }

    public static boolean intersectSegmentObb(final LineSegment lineSegmentArg, final OBBCollider obb)
    {
        //Assuming the OBB is given by a center point C; a halfwidth extent
        //vector e = (e0, e1, e2); and local coordinate axes u0, u1, and u2; then a point P in world
        //space can be expressed in the OBB coordinate system as the point (x, y, z), where
        //x = (P − C) · u0, y = (P − C) · u1, and z = (P − C) · u2.

        final Vector3 c = obb.getGlobalCenter();
        final Vector3 e = obb.getHalfWidthExtents();
        //e.add(capsule.getRadius() * 0.5f);
        float s;
        float bestS = Float.MAX_VALUE;

        //segment midpoint
        final Vector3 m = Vector3.add(lineSegmentArg.a(), lineSegmentArg.b()).mult(0.5f);
        m.sub(c); //we put the box and segment to origin (0,0,0)
        //move this vector into obb space
        m.set(
                Vector3.dot(m, obb.getLocalRight()),
                Vector3.dot(m, obb.getLocalUp()),
                Vector3.dot(m, obb.getLocalForward())
        );

        final Vector3 lineSegB = Vector3.sub(lineSegmentArg.b(), c); //put the segment pt b to origin(0,0,0)
        //move into obb space
        lineSegB.set(
                Vector3.dot(lineSegB, obb.getLocalRight()),
                Vector3.dot(lineSegB, obb.getLocalUp()),
                Vector3.dot(lineSegB, obb.getLocalForward())
        );

        //Let the segment be described by a midpoint M = (mx, my, mz) and endpoints
        //M − d and M + d, where d = (dx, dy, dz) is a direction vector for the segment. The
        //halflength of the segment is ||d||

        //direction vector von b zur mitte
        final Vector3 midPointToLineSegmentB = Vector3.sub(lineSegB, m); //segment halflength vector

        //check world coordinate axes
        float adx = Mathf.abs(midPointToLineSegmentB.getX());
        s = Mathf.abs(m.getX()) - (e.getX() + adx);
        System.out.println("s:"+s);
        if (Mathf.abs(m.getX()) > e.getX() + adx)
        {
            return false;
        }
        float ady = Mathf.abs(midPointToLineSegmentB.getY());
        s = Mathf.abs(m.getY()) - (e.getY() + ady);
        System.out.println("s:"+s);
        if (Mathf.abs(m.getY()) > e.getY() + ady)
        {
            return false;
        }
        float adz = Mathf.abs(midPointToLineSegmentB.getZ());
        s = Mathf.abs(m.getZ()) - (e.getZ() + adz);
        System.out.println("s:"+s);
        if (Mathf.abs(m.getZ()) > e.getZ() + adz)
        {
            return false;
        }


        // Add in an epsilon term to counteract arithmetic errors when segment is
        // (near) parallel to a coordinate axis (see text for detail)
        adx += EPSILON; ady += EPSILON; adz += EPSILON;

        // Try cross products of segment direction vector with coordinate axes
        if (Mathf.abs(m.getY() * midPointToLineSegmentB.getZ() - m.getZ() * midPointToLineSegmentB.getY()) > e.getY() * adz + e.getZ() * ady)
        {
            return false;
        }
        if (Mathf.abs(m.getZ() * midPointToLineSegmentB.getX() - m.getX() * midPointToLineSegmentB.getZ()) > e.getX() * adz + e.getZ() * adx)
        {
            return false;
        }
        if (Mathf.abs(m.getX() * midPointToLineSegmentB.getY() - m.getY() * midPointToLineSegmentB.getX()) > e.getX() * ady + e.getY() * adx)
        {
            return false;
        }
        // No separating axis found; segment must be overlapping AABB
        return true;
    }

    /**
     *  Testing if segment s(t)=p0 + t * (p0-p1) collides with an aabb
     * @param p0
     * @param p1
     * @param center
     * @param extents
     * @return
     */
    public static boolean intersectSegmentAABB(final Vector3 p0, final Vector3 p1, final Vector3 center, final Vector3 extents)
    {
        final Vector3 c = center;

        // Box halflength extents
        final Vector3 e = extents;

        // Segment midpoint
        Vector3 m = Vector3.add(p0, p1).mult(0.5f);

        // Segment halflength vector
        //von punkt1 zur mitte der Vektor
        final Vector3 midToSeg = Vector3.sub(p1, m);

        // Translate box and segment to origin
        //???
        m = Vector3.sub(m, c);

        // Try world coordinate axes as separating axes
        float adx = Mathf.abs(midToSeg.getX());
        if (Mathf.abs(m.getX()) > e.getX() + adx) return false;
        float ady = Mathf.abs(midToSeg.getY());
        if (Mathf.abs(m.getY()) > e.getY() + ady) return false;
        float adz = Mathf.abs(midToSeg.getZ());
        if (Mathf.abs(m.getZ()) > e.getZ() + adz) return false;

        // Add in an epsilon term to counteract arithmetic errors when segment is
        // (near) parallel to a coordinate axis (see text for detail)
        adx += EPSILON; ady += EPSILON; adz += EPSILON;

        // Try cross products of segment direction vector with coordinate axes
        if (Mathf.abs(m.getY() * midToSeg.getZ() - m.getZ() * midToSeg.getY()) > e.getY() * adz + e.getZ() * ady) return false;
        if (Mathf.abs(m.getZ() * midToSeg.getX() - m.getX() * midToSeg.getZ()) > e.getX() * adz + e.getZ() * adx) return false;
        if (Mathf.abs(m.getX() * midToSeg.getY() - m.getY() * midToSeg.getX()) > e.getX() * ady + e.getY() * adx) return false;

        // No separating axis found; segment must be overlapping AABB
        return true;
    }

    public static boolean intersectCapsuleCapsule(final Vector3 cA, final Vector3 cA2, final float rA,
                                                  final Vector3 cB, final Vector3 cB2, final float rB)
    {
        // Compute (squared) distance between the inner structures of the capsules
        final Vector3 c1 = new Vector3();
        final Vector3 c2 = new Vector3();
        final float dist2 = ClosestPointComputations.closestPtSegmentSegment(cA, cA2,
                cB, cB2, c1, c2);
        // If (squared) distance smaller than (squared) sum of radii, they collide
        final float radius = rA + rB;
        return dist2 <= radius * radius;
    }
    public static boolean intersectCapsuleCapsule(final CapsuleCollider capsule1, final CapsuleCollider capsule2)
    {
        return intersectCapsuleCapsule(
                capsule1.getA(), capsule1.getB(), capsule1.getRadius(),
                capsule2.getA(), capsule2.getB(), capsule2.getRadius());
    }

    public static boolean intersectCapsuleOBB(final CapsuleCollider capsuleCollider, final OBBCollider obbCollider, final FloatWrapper t)
    {
        //expressing capsule collider in obb world space
        final Transform invT = Transform.inverse(obbCollider.getTransform());

        final Vector3 aNew = invT.applyTransform(capsuleCollider.getA());
        final Vector3 bNew = invT.applyTransform(capsuleCollider.getB());

        //final Vector3 aC = Vector3.sub(a, obbCollider.getCenter());
        //final Vector3 aNew = new Vector3(
        //        Vector3.dot(aC, obbCollider.getLocalRight()),
        //        Vector3.dot(aC, obbCollider.getLocalUp()),
        //        Vector3.dot(aC, obbCollider.getLocalForward())
        //);
        //final Vector3 bC = Vector3.sub(b, obbCollider.getCenter());
        //final Vector3 bNew = new Vector3(
        //        Vector3.dot(bC, obbCollider.getLocalRight()),
        //        Vector3.dot(bC, obbCollider.getLocalUp()),
        //        Vector3.dot(bC, obbCollider.getLocalForward())
        //);

        return intersectCapsuleAABB(aNew, bNew, capsuleCollider.getRadius(),
                obbCollider.getMinPrimitive(), obbCollider.getMaxPrimitive(),
                t);
    }

    public static boolean intersectCapsuleAABB(final Vector3 aFirst, final Vector3 bSecond, final float radiusArg,
                                               final Vector3 min, final Vector3 max, final FloatWrapper t)
    {
        final Ray ray = Ray.fromLineSegment(aFirst, bSecond);

        final Vector3 center = ray.origin();
        final Vector3 d = ray.direction();

        final AABB tempB = new AABB(min, max, false);
        // Compute the AABB resulting from expanding b by sphere radius r
        final AABB e = new AABB(min, max); //copy min max into e
        e.min().sub(radiusArg);
        e.max().add(radiusArg);
        // Intersect ray against expanded AABB e. Exit with no intersection if ray
        // misses e, else get intersection point p and time t as result
        final Vector3 p = new Vector3();
        if (!intersectRayAABB(center, d, ray.travelDistance(), e.min(), e.max(), t, p) || t.getValue() > 1.0f)
        {
            //System.out.println("instant return false");
            return false;
        }

        // Compute which min and max faces of b the intersection point p lies
        // outside of. Note, u and v cannot have the same bits set and
        // they must have at least one bit set among them
        int u = 0, v = 0;
        if (p.getX() < min.getX()) u |= 1;
        if (p.getX() > max.getX()) v |= 1;
        if (p.getY() < min.getY()) u |= 2;
        if (p.getY() > max.getY()) v |= 2;
        if (p.getZ() < min.getZ()) u |= 4;
        if (p.getZ() > max.getZ()) v |= 4;
        // ‘Or’ all set bits together into a bit mask (note: here u + v == u | v)
        final int m = u | v;
        // Define line segment [c, c+d] specified by the sphere movement
        final LineSegment seg = new LineSegment(aFirst, bSecond);
        // If all 3 bits set (m == 7) then p is in a vertex region
        if (m == 7)
        {
            // Must now intersect segment [c, c+d] against the capsules of the three
            // edges meeting at the vertex and return the best time, if one or more hit
            //float tmin = FLT_MAX;
            float tmin = Float.MAX_VALUE;
            if (intersectSegmentCapsule(seg, corner(tempB, v), corner(tempB, v ^ 1), radiusArg, t))
            {
                tmin = Mathf.min(t.getValue(), tmin);
            }
            if (intersectSegmentCapsule(seg, corner(tempB, v), corner(tempB, v ^ 2), radiusArg, t))
            {
                tmin = Mathf.min(t.getValue(), tmin);
            }
            if (intersectSegmentCapsule(seg, corner(tempB, v), corner(tempB, v ^ 4), radiusArg, t))
            {
                tmin = Mathf.min(t.getValue(), tmin);
            }

            //if (tmin == FLT_MAX) return false; // No intersection
            if (tmin == Float.MAX_VALUE)
            {
                return false; // No intersection
            }
            t.setValue(tmin);
            return true; // Intersection at time t == tmin
        }
        // If only one bit set in m, then p is in a face region
        if ((m & (m - 1)) == 0) {
            // Do nothing. Time t from intersection with
            // expanded box is correct intersection time
            return true;
        }
        // p is in an edge region. Intersect against the capsule at the edge
        return intersectSegmentCapsule(seg, corner(tempB, u ^ 7), corner(tempB, v), radiusArg, t);
    }
    public static boolean intersectMovingSphereAABB(final Vector3 aFirst, final Vector3 bSecond, final float radiusArg,
                                                    final Vector3 min, final Vector3 max, final FloatWrapper t)
    {
        final Ray ray = Ray.fromLineSegment(aFirst, bSecond);

        final float radius = radiusArg;

        final Vector3 center = ray.origin();
        final Vector3 d = ray.direction();

        final AABB b = new AABB(min, max);
        // Compute the AABB resulting from expanding b by sphere radius r
        final AABB e = new AABB(min, max); //copy min max into e
        e.min().sub(radius);
        e.max().add(radius);
        // Intersect ray against expanded AABB e. Exit with no intersection if ray
        // misses e, else get intersection point p and time t as result
        final Vector3 p = new Vector3();
        if (!intersectRayAABB(center, d, ray.travelDistance(), e.min(), e.max(), t, p) || t.getValue() > 1.0f)
        {
            System.out.println("instant return false");
            return false;
        }

        // Compute which min and max faces of b the intersection point p lies
        // outside of. Note, u and v cannot have the same bits set and
        // they must have at least one bit set among them
        int u = 0, v = 0;
        if (p.getX() < min.getX()) u |= 1;
        if (p.getX() > max.getX()) v |= 1;
        if (p.getY() < min.getY()) u |= 2;
        if (p.getY() > max.getY()) v |= 2;
        if (p.getZ() < min.getZ()) u |= 4;
        if (p.getZ() > max.getZ()) v |= 4;
        // ‘Or’ all set bits together into a bit mask (note: here u + v == u | v)
        final int m = u + v;
        // Define line segment [c, c+d] specified by the sphere movement
        final LineSegment seg = new LineSegment(aFirst, bSecond);
        // If all 3 bits set (m == 7) then p is in a vertex region
        if (m == 7)
        {
            // Must now intersect segment [c, c+d] against the capsules of the three
            // edges meeting at the vertex and return the best time, if one or more hit
            //float tmin = FLT_MAX;
            float tmin = Float.MAX_VALUE;
            if (intersectSegmentCapsule(seg, corner(b, v), corner(b, v ^ 1), radius, t))
            {
                tmin = Mathf.min(t.getValue(), tmin);
            }
            if (intersectSegmentCapsule(seg, corner(b, v), corner(b, v ^ 2), radius, t))
            {
                tmin = Mathf.min(t.getValue(), tmin);
            }
            if (intersectSegmentCapsule(seg, corner(b, v), corner(b, v ^ 4), radius, t))
            {
                tmin = Mathf.min(t.getValue(), tmin);
            }

            //if (tmin == FLT_MAX) return false; // No intersection
            if (tmin == Float.MAX_VALUE)
            {
                return false; // No intersection
            }
            t.setValue(tmin);
            return true; // Intersection at time t == tmin
        }
        // If only one bit set in m, then p is in a face region
        if ((m & (m - 1)) == 0) {
            // Do nothing. Time t from intersection with
            // expanded box is correct intersection time
            return true;
        }
        // p is in an edge region. Intersect against the capsule at the edge
        return intersectSegmentCapsule(seg, corner(b, u ^ 7), corner(b, v), radius, t);
    }

    static Vector3 corner(final AABB b, final int n)
    {
        return new Vector3(
                (((n & 1) > 0) ? b.max().getX() : b.min().getX()),
                (((n & 2) > 0) ? b.max().getY() : b.min().getY()),
                (((n & 4) > 0) ? b.max().getZ() : b.min().getZ())
        );
    }

    public static boolean intersectSegmentCapsule(final LineSegment seg,
                                                  final Vector3 cA, final Vector3 cB, final float radius,
                                                  final FloatWrapper tRef)
    {
        return intersectSegmentCapsule(seg.a(), seg.b(),cA, cB, radius, tRef);
    }

    /**
     * Calculates the closest point t on S(t)=a+t*(b-a) to the given capsule
     * @param a point1 of Segment
     * @param b point2 of Segment
     * @param cA a of capsule
     * @param cB b of capsule
     * @param radius radius of capsule
     * @param tRef t with S(t)=a+t*(b-a)
     * @return true if squared distance of segments is lower than squared radius
     */
    public static boolean intersectSegmentCapsule(final Vector3 a, final Vector3 b,
                                                  final Vector3 cA, final Vector3 cB, final float radius,
                                                  final FloatWrapper tRef)
    {
        final FloatWrapper t = new FloatWrapper();
        final FloatWrapper s = new FloatWrapper();
        final Vector3 ref1 = new Vector3();
        final Vector3 ref2 = new Vector3();
        final float sqDistance = ClosestPointComputations.closestPtSegmentSegment(a, b, cA, cB, t, s, ref1, ref2);
        final float radiusSquared = radius * radius;
        tRef.setValue(t.getValue());
        return sqDistance <= radiusSquared;
    }

    /**
     * Intersect segment S(t)=sa+t(sb-sa), 0<=t<=1 against cylinder specified by p, q and r
     * @param seg
     * @param p
     * @param q
     * @param r
     * @param t
     * @return
     */
    public static boolean intersectSegmentCylinder(final LineSegment seg, final Vector3 p, final Vector3 q,
                                                   final float r, final FloatWrapper t)
    {
        return intersectSegmentCylinder(seg.a(), seg.b(), p, q, r, t);
    }

    /**
     * http://realtimecollisiondetection.net/books/rtcd/errata/
     * Intersect segment S(t)=sa+t(sb-sa), 0<=t<=1 against cylinder specified by p, q and r
     * @param sa
     * @param sb
     * @param p
     * @param q
     * @param r
     * @param tRef
     * @return
     */
    public static boolean intersectSegmentCylinder(final Vector3 sa, final Vector3 sb, final Vector3 p, final Vector3 q,
                                                   final float r, final FloatWrapper tRef)
    {
        float t;
        final Vector3 d = Vector3.sub(q, p);
        final Vector3 m = Vector3.sub(sa, p);
        final Vector3 n = Vector3.sub(sb, sa);
        final float md = Vector3.dot(m, d);
        final float nd = Vector3.dot(n, d);
        final float dd = Vector3.dot(d, d);

        // Test if segment fully outside either endcap of cylinder
        if (md < 0.0f && md + nd < 0.0f)
        {
            return false; // Segment outside ’p’ side of cylinder
        }
        if (md > dd && md + nd > dd)
        {
            return false; // Segment outside ’q’ side of cylinder
        }
        final float nn = Vector3.dot(n, n);
        final float mn = Vector3.dot(m, n);
        final float a = dd * nn - nd * nd;
        final float k = m.sqMagnitude() - r * r;
        final float c = dd * k - md * md;
        if (Mathf.abs(a) < EPSILON) {
            // Segment runs parallel to cylinder axis
            if (c > 0.0f)
            {
                // ’a’ and thus the segment lie outside cylinder
                return false;
            }
            // Now known that segment intersects cylinder; figure out how it intersects
            if (md < 0.0f)
            {
                // Intersect segment against ’p’ endcap
                t = -mn / nn;
                tRef.setValue(t);
            }
            else if (md > dd) t = (nd - mn) / nn; // Intersect segment against ’q’ endcap
            else{
                t = 0.0f; // ’a’ lies inside cylinder
                tRef.setValue(t);
            }
            return true;
        }
        final float b = dd * mn - nd * md;
        final float discr = b * b - a * c;
        if (discr < 0.0f) return false; // No real roots; no intersection
        t = (-b - Mathf.sqrt(discr)) / a;
        tRef.setValue(t);
        if (t < 0.0f || t > 1.0f)
        {
            return false; // Intersection lies outside segment
        }
        if (md + t * nd < 0.0f)
        {
            // Intersection outside cylinder on ’p’ side
            if (nd <= 0.0f) return false; // Segment pointing away from endcap
            t = -md / nd;
            tRef.setValue(t);
            // Keep intersection if Dot(S(t) - p, S(t) - p) <= r∧2
            //return k + 2f * t * (mn + t * nn) <= 0.0f; //this is wrong
            return k + t * (2.0f * mn + t * nn) <= 0.0f;
        } else if (md + t * nd > dd) {
            // Intersection outside cylinder on ’q’ side
            if (nd >= 0.0f) return false; // Segment pointing away from endcap
            t = (dd - md) / nd;
            tRef.setValue(t);
            // Keep intersection if Dot(S(t) - q, S(t) - q) <= r∧2
            return k + dd - 2f * md + t * (2f * (mn - nd) + t * nn) <= 0.0f;
        }
        // Segment intersects cylinder between the endcaps; t is correct
        return true;

    }

    /**
     * Returns true if sphere s intersects OBB b, false otherwise.
     * The point p on the OBB closest to the sphere center is also returned
     * @param s
     * @param b
     * @param p
     * @return true, if sphere s intersects obb b
     */
    public static boolean intersectSphereOBB(final SphereCollider s, final OBBCollider b, final Vector3 p)
    {
        return intersectSphereOBB(s.getCenter(), s.getRadius(), b, p);
        /*
        // Find point p on OBB closest to sphere center
        p.setVector3(ClosestPointComputations.closestPtPointOBB(s.getCenter(), b));

        // Sphere and OBB intersect if the (squared) distance from sphere
        // center to point p is less than the (squared) sphere radius
        final Vector3 v = Vector3.sub(p, s.getCenter());
        return v.sqrMagnitude() <= s.getRadius() * s.getRadius();
         */
    }
    public static boolean intersectSphereOBB(final Vector3 s, final float radius, final OBBCollider b, final Vector3 p)
    {
        // Find point p on OBB closest to sphere center
        p.set(ClosestPointComputations.closestPtPointOBB(s, b));

        // Sphere and OBB intersect if the (squared) distance from sphere
        // center to point p is less than the (squared) sphere radius
        final Vector3 v = Vector3.sub(p, s);
        return v.sqMagnitude() <= radius * radius;
    }
}
