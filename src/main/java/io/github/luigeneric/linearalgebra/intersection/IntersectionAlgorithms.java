package io.github.luigeneric.linearalgebra.intersection;

import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.collidershapes.*;
import io.github.luigeneric.linearalgebra.utility.FloatWrapper;
import io.github.luigeneric.linearalgebra.utility.Mathf;


public final class IntersectionAlgorithms
{

    /**
     * Test if point p is contained in triangle (a, b, c)
     * @param p
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static boolean testVectorTriangle(Vector3 p, Vector3 a, Vector3 b, Vector3 c)
    {
        final FloatWrapper u = new FloatWrapper();
        final FloatWrapper v = new FloatWrapper();
        final FloatWrapper w = new FloatWrapper();

        barycentric(a, b, c, p, u, v, w);

        return v.getValue() >= 0.0f && w.getValue() >= 0.0f && (v.getValue() + w.getValue()) <= 1.0f;
    }

    /**
     * Compute barycentric coordinates (u, v, w) for
     *     point p with respect to triangle (a, b, c)
     * @param a
     * @param b
     * @param c
     * @param p
     */
    private static void barycentric(final Vector3 a, final Vector3 b, final Vector3 c, final Vector3 p,
                                    final FloatWrapper u, final FloatWrapper v, final FloatWrapper w)
    {

        final Vector3 v0 = Vector3.sub(b, a);
        final Vector3 v1 = Vector3.sub(c, a);
        final Vector3 v2 = Vector3.sub(p, a);


        final float d00 = Vector3.dot(v0, v0);
        final float d01 = Vector3.dot(v0, v1);
        final float d11 = Vector3.dot(v1, v1);
        final float d20 = Vector3.dot(v2, v0);
        final float d21 = Vector3.dot(v2, v1);
        final float denom = d00 * d11 - d01 * d01;

        final float tmpv = (d11 * d20 - d01 * d21) / denom;
        v.setValue(tmpv);

        final float tmpw = (d00 * d21 - d01 * d20) / denom;
        w.setValue(tmpw);

        final float tmpu = 1.0f - tmpv - tmpw;
        u.setValue(tmpu);
    }


    public static CollisionRecord testSphereCapsule(final SphereCollider sphere, final CapsuleCollider capsule,
                                                    final boolean reverseNormal)
    {
        //compute closest pt from sphere to capsule line segment
        final Vector3 closestPtOnSegment = ClosestPointComputations
                .closestPtPointSegment(sphere.getCenter(), capsule.getA(), capsule.getB(), new FloatWrapper());
        final Vector3 sphereToCapsule = Vector3.sub(closestPtOnSegment, sphere.getCenter());
        final float sqDistance = sphereToCapsule.sqMagnitude();
        final float radius = sphere.getRadius() + capsule.getRadius();
        if (sqDistance > radius * radius)
        {
            return null;
        }
        final float distance = Mathf.sqrt(sqDistance);
        final float pen = radius - distance;
        sphereToCapsule.normalize();
        if (reverseNormal)
        {
            sphereToCapsule.invert();
        }
        return new CollisionRecord(true, pen, sphereToCapsule, null, null);
    }


    public static CollisionRecord testCapsuleCapsule(final CapsuleCollider shape1, final CapsuleCollider shape2)
    {
        // Compute the closest points

        final Vector3 c1 = new Vector3();
        final Vector3 c2 = new Vector3();
        final float dist2 = ClosestPointComputations.closestPtSegmentSegment(shape1.getA(), shape1.getB(),
                shape2.getA(), shape2.getB(), c1, c2);
        return testSphereSphere(c1, shape1.getRadius(), shape1.getTransform(), c2, shape2.getRadius(), shape2.getTransform());
    }
    private static CollisionRecord testSphereSphere(final Vector3 c1, final float r1, final Transform t1,
                                                    final Vector3 c2, final float r2, final Transform t2)
    {
        SphereCollider shape1 = new SphereCollider(t1, c1, r1);
        shape1.getCenter().set(c1);

        return testSphereSphere(shape1, c2, r2, t2);
    }


    public static CollisionRecord testSphereSphere(final SphereCollider shape1, final Vector3 center, final float radius,
                                                   final Transform transform)
    {
        final SphereCollider shape2 = new SphereCollider(transform, center, radius);
        shape2.getCenter().set(center);
        return testSphereSphere(shape1, shape2);
    }
    public static CollisionRecord testSphereSphere(final SphereCollider shape1, final SphereCollider shape2)
    {
        // Compute the distance between the centers
        final Vector3 vectorBetweenCenters = Vector3.sub(shape2.getCenter(), shape1.getCenter());
        float squaredDistanceBetweenCenters = vectorBetweenCenters.sqMagnitude();

        // Compute the sum of the radius
        float sumRadius = shape1.getRadius() + shape2.getRadius();

        //if this happens, there is no intersection!
        if (squaredDistanceBetweenCenters > sumRadius * sumRadius)
        {
            return null;
        }

        // If the sphere collision shapes intersect
        final Vector3 centerSphere2InBody1LocalSpace =
                shape1.getTransform().copy().inverse().applyTransform(shape2.getTransform().getPosition());
        //System.out.println("test1: " + centerSphere2InBody1LocalSpace);
        final Vector3 centerSphere1InBody2LocalSpace =
                shape2.getTransform().copy().inverse().applyTransform(shape1.getTransform().getPosition());

        final Vector3 intersectionOnBody1 = centerSphere2InBody1LocalSpace.normalize().mult(shape1.getRadius());
        final Vector3 intersectionOnBody2 = centerSphere1InBody2LocalSpace.normalize().mult(shape2.getRadius());
        final float penetrationDepth = sumRadius - Mathf.sqrt(squaredDistanceBetweenCenters);

        return new CollisionRecord(penetrationDepth, vectorBetweenCenters.normalize(), intersectionOnBody1, intersectionOnBody2);
    }



    public static CollisionRecord testOBBOBB(final OBBCollider a, final OBBCollider b)
    {
        //idea: prune with sphere
        final boolean mayIntersect = PrimitiveIntersectionAlgorithms
                .intersectSphereSphere(a.getGlobalCenter(), a.getMaximumRadius(), b.getGlobalCenter(), b.getMaximumRadius());
        if (!mayIntersect)
            return null;

        float s;
        float bestSep = Float.MAX_VALUE;
        Vector3 bestAxis = null;
        float tl;
        float ra, rb;
        final float[][] R = new float[3][3];
        final float[][] AbsR = new float[3][3];
        //final Matrix3x3 absR3x3 = new Matrix3x3(AbsR);
        //final Transform aInv = Transform.inverse(a.getTransform());

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


        // Compute common subexpressions. Add in an epsilon term to
        // counteract arithmetic errors when two edges are parallel and
        // their cross product is (near) null (see text for details)
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                AbsR[i][j] = Mathf.abs(R[i][j]) + Mathf.EPSILON;
            }
        }
        //final Quaternion rOutMAbs = Quaternion.fromMatrix(new Matrix3x3(AbsR));


        // Test axes L = A0, L = A1, L = A2
        //gecheckt
        for (int i = 0; i < 3; i++)
        {
            ra = a.getHalfWidthExtents().getIndex(i);
            //ra2 = Vector3.dot(l, a.getHalfWidthExtents());
            rb = b.getHalfWidthExtents().getIndex(0) * AbsR[i][0] +
                    b.getHalfWidthExtents().getIndex(1) * AbsR[i][1] + b.getHalfWidthExtents().getIndex(2) * AbsR[i][2];
            //rb2 = Mathf.abs(Vector3.dot(l, absR3x3.mult(b.getHalfWidthExtents())));
            tl = Mathf.abs(t.getIndex(i));


            if (tl > ra + rb)
            {
                return null;
            }
            s = Mathf.abs(tl - (ra + rb));
            if (s < bestSep)
            {
                bestSep = s;
                bestAxis = a.getLocalAxes(i);
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
                return null;
            }
            s = Mathf.abs(tl - (ra + rb));
            if (s < bestSep)
            {
                bestSep = s;
                bestAxis = b.getLocalAxes(i);
            }
        }

        // Test axis L = A0 x B0
        //gecheckt
        ra = a.getHalfWidthExtents().getIndex(1) * AbsR[2][0] + a.getHalfWidthExtents().getIndex(2) * AbsR[1][0];
        rb = b.getHalfWidthExtents().getIndex(1) * AbsR[0][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[0][1];
        tl = Mathf.abs(t.getIndex(2) * R[1][0] - t.getIndex(1) * R[2][0]);
        if (tl > ra + rb)
        {
            return null;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            //bestSep = s;
            //bestAxis = Vector3.cross(a.getLocalRight(), b.getLocalRight());
        }

        // Test axis L = A0 x B1
        //gecheckt
        ra = a.getHalfWidthExtents().getIndex(1) * AbsR[2][1] + a.getHalfWidthExtents().getIndex(2) * AbsR[1][1];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[0][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[0][0];
        tl = Mathf.abs(t.getIndex(2) * R[1][1] - t.getIndex(1) * R[2][1]);
        if (tl > ra + rb)
        {
            return null;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            //bestSep = s;
            //bestAxis = Vector3.cross(a.getLocalRight(), b.getLocalUp());
        }


        // Test axis L = A0 x B2
        ra = a.getHalfWidthExtents().getIndex(1) * AbsR[2][2] + a.getHalfWidthExtents().getIndex(2) * AbsR[1][2];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[0][1] + b.getHalfWidthExtents().getIndex(1) * AbsR[0][0];
        tl = Mathf.abs(t.getIndex(2) * R[1][2] - t.getIndex(1) * R[2][2]);
        if (tl > ra + rb)
        {
            return null;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            //bestSep = s;
            //bestAxis = Vector3.cross(a.getLocalRight(), b.getLocalForward());
        }


        // Test axis L = A1 x B0
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[2][0] + a.getHalfWidthExtents().getIndex(2) * AbsR[0][0];
        rb = b.getHalfWidthExtents().getIndex(1) * AbsR[1][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[1][1];
        tl  = Mathf.abs(t.getIndex(0) * R[2][0] - t.getIndex(2) * R[0][0]);
        if (tl > ra + rb)
        {
            return null;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            //bestSep = s;
            //bestAxis = Vector3.cross(a.getLocalUp(), b.getLocalRight());
        }


        // Test axis L = A1 x B1
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[2][1] + a.getHalfWidthExtents().getIndex(2) * AbsR[0][1];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[1][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[1][0];
        tl = Mathf.abs(t.getIndex(1) * R[2][1] - t.getIndex(2) * R[0][1]);
        if (tl > ra + rb)
        {
            return null;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            //bestSep = s;
            //bestAxis = Vector3.cross(a.getLocalUp(), b.getLocalUp());
        }

        // Test axis L = A1 x B2
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[2][2] + a.getHalfWidthExtents().getIndex(2) * AbsR[0][2];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[1][1] + b.getHalfWidthExtents().getIndex(1) * AbsR[1][0];
        tl = Mathf.abs(t.getIndex(1) * R[2][2] - t.getIndex(2) * R[0][2]);
        if (tl > ra + rb)
        {
            return null;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            //bestSep = s;
            //bestAxis = Vector3.cross(a.getLocalUp(), b.getLocalForward());
        }

        // Test axis L = A2 x B0
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[1][0] + a.getHalfWidthExtents().getIndex(1) * AbsR[0][0];
        rb = b.getHalfWidthExtents().getIndex(1) * AbsR[2][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[2][1];
        tl = Mathf.abs(t.getIndex(1) * R[0][0] - t.getIndex(0) * R[1][0]);
        if (tl > ra + rb)
        {
            return null;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            //bestSep = s;
            //bestAxis = Vector3.cross(a.getLocalForward(), b.getLocalRight());
        }

        // Test axis L = A2 x B1
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[1][1] + a.getHalfWidthExtents().getIndex(1) * AbsR[0][1];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[2][2] + b.getHalfWidthExtents().getIndex(2) * AbsR[2][0];
        tl = Mathf.abs(t.getIndex(1) * R[0][1] - t.getIndex(0) * R[1][1]);
        if (tl > ra + rb)
        {
            return null;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            //bestSep = s;
            //bestAxis = Vector3.cross(a.getLocalForward(), b.getLocalUp());
        }

        // Test axis L = A2 x B2
        ra = a.getHalfWidthExtents().getIndex(0) * AbsR[1][2] + a.getHalfWidthExtents().getIndex(1) * AbsR[0][2];
        rb = b.getHalfWidthExtents().getIndex(0) * AbsR[2][1] + b.getHalfWidthExtents().getIndex(1) * AbsR[2][0];
        tl = Mathf.abs(t.getIndex(1) * R[0][2] - t.getIndex(0) * R[1][2]);
        if (tl > ra + rb)
        {
            return null;
        }
        s = Mathf.abs(tl - (ra + rb));
        if (s < bestSep)
        {
            //bestSep = s;
            //bestAxis = Vector3.cross(a.getLocalForward(), b.getLocalForward());
        }
        if (bestAxis == null)
            return null;
        bestAxis = bestAxis.copy();
        bestAxis.normalize();
        final Vector3 ab = Vector3.sub(b.getGlobalCenter(), a.getGlobalCenter());
        final float dot = Vector3.dot(ab, bestAxis);
        if (dot < 0)
        {
            bestAxis.invert();
        }

        // Since no separating axis is found, the OBBs must be intersecting
        return new CollisionRecord(bestSep, bestAxis, null, null);
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


    public static Vector3 closestPtPointTriangle(final Triangle triangle, final Vector3 p)
    {
        final Vector3 a = triangle.getA();
        final Vector3 b = triangle.getB();
        final Vector3 c = triangle.getC();

        final Vector3 ab = Vector3.sub(b, a);
        final Vector3 ac = Vector3.sub(c, a);
        final Vector3 ap = Vector3.sub(p, a);

        final float d1 = Vector3.dot(ab, ap);
        final float d2 = Vector3.dot(ac, ap);
        if (d1 <= 0.0f && d2 <= 0.0f) return a; // barycentric coordinates (1,0,0)
        // Check if P in vertex region outside B
        final Vector3 bp = Vector3.sub(p, b);
        final float d3 = Vector3.dot(ab, bp);
        final float d4 = Vector3.dot(ac, bp);
        if (d3 >= 0.0f && d4 <= d3) return b; // barycentric coordinates (0,1,0)
        // Check if P in edge region of AB, if so return projection of P onto AB
        final float vc = d1*d4 - d3*d2;
        if (vc <= 0.0f && d1 >= 0.0f && d3 <= 0.0f) {
            final float v = d1 / (d1 - d3);
            return Vector3.add(a, Vector3.mult(ab, v)); // barycentric coordinates (1-v,v,0)
        }
        // Check if P in vertex region outside C
        Vector3 cp = Vector3.sub(p, c);
        final float d5 = Vector3.dot(ab, cp);
        final float d6 = Vector3.dot(ac, cp);
        if (d6 >= 0.0f && d5 <= d6) return c; // barycentric coordinates (0,0,1)

        // Check if P in edge region of AC, if so return projection of P onto AC
        final float vb = d5*d2 - d1*d6;
        if (vb <= 0.0f && d2 >= 0.0f && d6 <= 0.0f) {
            final float w = d2 / (d2 - d6);
            return Vector3.add(a, Vector3.mult(ac, w)); // barycentric coordinates (1-w,0,w)
        }
        // Check if P in edge region of BC, if so return projection of P onto BC
        final float va = d3*d6 - d5*d4;
        if (va <= 0.0f && (d4 - d3) >= 0.0f && (d5 - d6) >= 0.0f) {
            final float w = (d4 - d3) / ((d4 - d3) + (d5 - d6));
            return Vector3.add(b, Vector3.mult(Vector3.sub(c, b), w)); // barycentric coordinates (0,1-w,w)
        }
        // P inside face region. Compute Q through its barycentric coordinates (u,v,w)
        final float denom = 1.0f / (va + vb + vc);
        final float v = vb * denom;
        final float w = vc * denom;
        return Vector3.add(Vector3.add(a, Vector3.mult(ab, v)), Vector3.mult(ac, w)); // = u*a + v*b + w*c, u = va * denom = 1.0f - v - w
    }


    public static CollisionRecord testSphereOBB(final Vector3 center, final float radius, final OBBCollider b, final boolean reverseNormal)
    {
        // Find point p on OBB closest to sphere center
        final Vector3 p = ClosestPointComputations.closestPtPointOBB(center, b);

        // Sphere and OBB intersect if the (squared) distance from sphere
        // center to point p is less than the (squared) sphere radius
        final Vector3 v = Vector3.sub(p, center);
        final float sqRadius = radius * radius;
        final float sqDistance = v.sqMagnitude();
        if (sqDistance > sqRadius)
        {
            return null;
        }
        final Vector3 normalSB = Vector3.sub(b.getGlobalCenter(), center);
        normalSB.normalize();

        //v.normalize();
        if (reverseNormal)
        {
            //v.invert();
            normalSB.invert();
        }

        final float distance = Mathf.sqrt(sqDistance);
        final float penDepth = radius - distance;

        return new CollisionRecord(penDepth, normalSB, null, null);
    }

    public static CollisionRecord testSphereOBB(final SphereCollider s, final OBBCollider b, final boolean reverseNormal)
    {
        return testSphereOBB(s.getCenter(), s.getRadius(), b, reverseNormal);
    }

    public static CollisionRecord testCapsuleObb3(final CapsuleCollider capsule, final OBBCollider obb, final boolean inverseNormal)
    {
        //idea: prune maximum radius!
        final boolean mayIntersect = PrimitiveIntersectionAlgorithms.intersectSphereSphere(
                capsule.getTransform().getPosition(), capsule.getPruneSphereRadius(),
                obb.getGlobalCenter(), obb.getMaximumRadius());
        if (!mayIntersect)
        {
            return null;
        }

        //this is the clamped segment inside the obb
        final Vector3 closestPtA = ClosestPointComputations
                .closestPtPointOBB(capsule.getA(), obb);
        final Vector3 closestPtB = ClosestPointComputations
                .closestPtPointOBB(capsule.getB(), obb);

        //now check this clamped segment against capsule
        //get closest points on both segments
        final Vector3 c1 = new Vector3();
        final Vector3 c2 = new Vector3();
        final float sqDist = ClosestPointComputations.closestPtSegmentSegment(capsule.getA(), capsule.getB(),
                closestPtA, closestPtB, c1, c2);
        //distannce is bigger than radius -> no intersection
        if (sqDist > capsule.getRadius()*capsule.getRadius())
        {
            return null;
        }
        final float penetrationDepth = capsule.getRadius() - Mathf.sqrt(sqDist);
        final Vector3 normal = Vector3.sub(c2, c1); //c1 -> c2
        normal.normalize();

        if (inverseNormal)
        {
            normal.invert();
        }
        return new CollisionRecord(penetrationDepth, normal);
    }


}

