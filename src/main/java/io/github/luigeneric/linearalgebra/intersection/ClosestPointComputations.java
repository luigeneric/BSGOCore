package io.github.luigeneric.linearalgebra.intersection;

import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.collidershapes.OBBCollider;
import io.github.luigeneric.linearalgebra.utility.FloatWrapper;
import io.github.luigeneric.linearalgebra.utility.Mathf;

public final class ClosestPointComputations
{
    private ClosestPointComputations(){}


    /**
     * Calculates the squared distance between point c and segment ab using do product
     * @param a segment start
     * @param b segment end
     * @param c ponint
     * @return the squared distance between point c and segment ab
     */
    public static float sqDistPointSegment(final Vector3 a, final Vector3 b, final Vector3 c)
    {
        final Vector3 ab = Vector3.sub(b, a);
        final Vector3 ac = Vector3.sub(c, a);
        final Vector3 bc = Vector3.sub(c, b);
        final float e = Vector3.dot(ac, ab);

        // Handle cases where c projects outside ab
        if (e <= 0.0f) return ac.sqMagnitude();//Vector3.dot(ac, ac);
        final float f = ab.sqMagnitude(); //Vector3.dot(ab, ab);
        if (e >= f) return bc.sqMagnitude(); //Vector3.dot(bc, bc);
        // Handle cases where c projects onto ab
        return ac.sqMagnitude() /*Vector3.dot(ac, ac)*/ - e * e / f;
    }

    /**
     * Given segment ab and point c, computes closest point d on ab.
     * Also returns t for the position of d, d(t) = a + t*(b - a)
     * @param a first point of line
     * @param b second point of line
     * @param c the point on the line
     * @param t the call by value: t
     */
    public static Vector3 closestPtPointSegment(final Vector3 c, final Vector3 a, final Vector3 b, final FloatWrapper t)
    {
        final Vector3 d = Vector3.zero();
        closestPtPointSegment(c, a, b, t, d);
        return d;
    }

    /**
     * Given segment ab and point c, computes closest point d on ab.
     * Also returns t for the position of d, d(t) = a + t*(b - a)
     * @param a first point of line
     * @param b second point of line
     * @param c the point on the line
     * @param t the CALL BY VALUE: t
     * @param d the CALL BY VALUE: point d
     */
    public static void closestPtPointSegment(final Vector3 c, final Vector3 a, final Vector3 b, final FloatWrapper t, final Vector3 d)
    {
        final Vector3 ab = Vector3.sub(b, a);

        /*
        float tmpTt = Vector3.dot(Vector3.subtract(c, a), ab) / Vector3.dot(ab, ab);
        // If outside segment, clamp t (and therefore d) to the closest endpoint
        //if (tmpTt < 0.0f) tmpTt = 0.0f;
        //if (tmpTt > 1.0f) tmpTt = 1.0f;
        tmpTt = Mathf.clamp01(tmpTt);
        // Compute projected position from the clamped t
        d.setVector3(Vector3.add(a, Vector3.mult(ab, tmpTt)));
        t.setValue(tmpTt);
        */

        //this should be a faster variant due to the elimination of division
        float tmpT = Vector3.dot(Vector3.sub(c, a), ab);
        if (tmpT <= 0.0f) {
            // c projects outside the [a,b] interval, on the a side; clamp to a
            t.setValue(0);
            //d = a;
            d.set(a);
        } else {
            final float denom = ab.sqMagnitude(); //Vector3.dot(ab, ab); // Always nonnegative since denom = ||ab||∧2
            if (tmpT >= denom) {
                // c projects outside the [a,b] interval, on the b side; clamp to b
                t.setValue(1.f);
                d.set(b);
            } else {
                // c projects inside the [a,b] interval; must do deferred divide now
                tmpT = tmpT / denom;
                //d = a + tmpT * ab;
                d.set(Vector3.add(a, Vector3.mult(ab, tmpT)));
                t.setValue(tmpT);
            }
        }
    }


    /**
     * Closest Point on AABB to Point
     * Given point p, return the point q on or in AABB b that is closest to p
     * @param point
     * @param minArg
     * @param maxArg
     * @return
     */
    public static Vector3 closestPtPointAABB(final Vector3 point, final Vector3 minArg, final Vector3 maxArg)
    {
        // For each coordinate axis, if the point coordinate value is
        // outside box, clamp it to the box, else keep it as is

        final float[] p = point.toArray();
        final float[] min = minArg.toArray();
        final float[] max = maxArg.toArray();
        final float[] q = new float[3];

        for (int i = 0; i < 3; i++)
        {
            //float v = p[i];
            //if (v < min[i]) v = min[i]; // v = max(v, b.min[i])
            //if (v > max[i]) v = max[i]; // v = min(v, b.max[i])
            q[i] = Mathf.clampSafe(p[i], min[i], max[i]);;
        }


        return new Vector3(q);
    }

    /**
     * Computes the square distance between a point p and an AABB b
     * @param point point to clamp
     * @param minArg min vec
     * @param maxArg max vec
     * @return
     */
    public static float sqDistPointAABB(final Vector3 point, final Vector3 minArg, final Vector3 maxArg)
    {
        final float[] p = point.toArray();
        final float[] min = minArg.toArray();
        final float[] max = maxArg.toArray();

        float sqDist = 0.0f;
        for (int i = 0; i < 3; i++) {
            // For each axis count any excess distance outside box extents
            final float v = p[i];
            if (v < min[i]) sqDist += (min[i] - v) * (min[i] - v);
            if (v > max[i]) sqDist += (v - max[i]) * (v - max[i]);
        }
        return sqDist;
    }


    /**
     * Computes closest points C1 and C2 of
     * S1(s)=P1+s*(Q1-P1) and
     * S2(t)=P2+t*(Q2-P2)
     * distance between between S1(s) and S2(t)
     * and yes s and t are not returned
     * @param p1 point1 of SegmentA
     * @param q1 point2 of SegmentA
     * @param p2 point1 of SegmentB
     * @param q2 point2 of SegmentB
     * @param c1
     * @param c2
     * @return
     */
    public static float closestPtSegmentSegment(final Vector3 p1, final Vector3 q1, final Vector3 p2, final Vector3 q2,
                                                final Vector3 c1, final Vector3 c2)
    {
        float s,t;
        final Vector3 d1 = Vector3.sub(q1, p1); // Direction vector of segment S1
        final Vector3 d2 = Vector3.sub(q2, p2); // Direction vector of segment S2
        final Vector3 r = Vector3.sub(p1, p2);
        final float a = d1.sqMagnitude(); // Squared length of segment S1, always nonnegative
        final float e = d2.sqMagnitude(); // Squared length of segment S2, always nonnegative
        final float f = Vector3.dot(d2, r);
        // Check if either or both segments degenerate into points
        if (a <= Mathf.EPSILON && e <= Mathf.EPSILON) {
            // Both segments degenerate into points
            s = t = 0.0f;

            c1.set(p1);
            c2.set(p2);

            return Vector3.dot(Vector3.sub(c1, c2), Vector3.sub(c1, c2));
        }
        if (a <= Mathf.EPSILON)
        {
            // First segment degenerates into a point
            s = 0.0f;
            t = f / e; // s = 0 => t = (b*s + f) / e = f / e
            t = Mathf.clamp01(t);
        }
        else
        {
            float c = Vector3.dot(d1, r);
            if (e <= Mathf.EPSILON) {
                // Second segment degenerates into a point
                t = 0;
                s = Mathf.clamp01(-c / a);// t = 0 => s = (b*t - c) / a = -c / a
            }
            else
            {
                // The general nondegenerate case starts here
                float b = Vector3.dot(d1, d2);
                float denom = a*e-b*b; // Always nonnegative
                // If segments not parallel, compute closest point on L1 to L2 and
                // clamp to segment S1. Else pick arbitrary s (here 0)
                if (denom != 0.0f) {
                    s = (Mathf.clamp01((b*f - c*e) / denom));
                }
                else
                {
                    s = 0f;
                }
                // Compute point on L2 closest to S1(s) using
                // t = Dot((P1 + D1*s) - P2,D2) / Dot(D2,D2) = (b*s + f) / e
                //t.setValue((b*s.getValue() + f) / e);
                t = (b * s + f) / e;
                // If t in [0,1] done. Else clamp t, recompute s for the new value
                // of t using s = Dot((P2 + D2*t) - P1,D1) / Dot(D1,D1)= (t*b - c) / a
                // and clamp s to [0, 1]
                if (t < 0.0f)
                {
                    t = 0;
                    s = (Mathf.clamp01(-c / a));
                }
                else if (t > 1.0f)
                {
                    t = 1f;
                    s = Mathf.clamp01((b-c) / a);
                }
            }
        }
        c1.set(Vector3.add(p1, Vector3.mult(d1, s)));
        c2.set(Vector3.add(p2, Vector3.mult(d2, t)));
        return Vector3.dot(Vector3.sub(c1, c2), Vector3.sub(c1, c2));
    }
    /**
     * Computes closest points C1 and C2 of
     * S1(s)=P1+s*(Q1-P1) and
     * S2(t)=P2+t*(Q2-P2)
     * returning s and t. Function result is squared
     * distance between between S1(s) and S2(t)
     * @param p1
     * @param q1
     * @param p2
     * @param q2
     * @return squared distance between S1 & S2
     */
    public static float closestPtSegmentSegment(final Vector3 p1, final Vector3 q1, final Vector3 p2, final Vector3 q2,
                                                final FloatWrapper s, final FloatWrapper t, final Vector3 c1, final Vector3 c2)
    {
        final Vector3 d1 = Vector3.sub(q1, p1); // Direction vector of segment S1
        final Vector3 d2 = Vector3.sub(q2, p2); // Direction vector of segment S2
        final Vector3 r = Vector3.sub(p1, p2);
        final float a = d1.sqMagnitude(); // Squared length of segment S1, always nonnegative
        final float e = d2.sqMagnitude(); // Squared length of segment S2, always nonnegative
        final float f = Vector3.dot(d2, r);
        // Check if either or both segments degenerate into points
        if (a <= Mathf.EPSILON && e <= Mathf.EPSILON) {
            // Both segments degenerate into points

            s.setValue(0); //already set to 0
            t.setValue(0); //already set to 0

            c1.set(p1);
            c2.set(p2);

            return Vector3.dot(Vector3.sub(c1, c2), Vector3.sub(c1, c2));
        }
        if (a <= Mathf.EPSILON)
        {
            // First segment degenerates into a point
            s.setValue(0); //already set to 0
            t.setValue(f / e); // s = 0 => t = (b*s + f) / e = f / e
            t.setValue(Mathf.clampSafe(t.getValue(), 0.0f, 1.0f));
        }
        else
        {
            float c = Vector3.dot(d1, r);
            if (e <= Mathf.EPSILON) {
                // Second segment degenerates into a point
                t.setValue(0);
                s.setValue(Mathf.clamp01(-c / a));// t = 0 => s = (b*t - c) / a = -c / a
            }
            else
            {
                // The general nondegenerate case starts here
                float b = Vector3.dot(d1, d2);
                float denom = a*e-b*b; // Always nonnegative
                // If segments not parallel, compute closest point on L1 to L2 and
                // clamp to segment S1. Else pick arbitrary s (here 0)
                if (denom != 0.0f) {
                    s.setValue(Mathf.clamp01((b*f - c*e) / denom));
                }
                else
                {
                    s.setValue(0f);
                }
                // Compute point on L2 closest to S1(s) using
                // t = Dot((P1 + D1*s) - P2,D2) / Dot(D2,D2) = (b*s + f) / e
                t.setValue((b*s.getValue() + f) / e);
                // If t in [0,1] done. Else clamp t, recompute s for the new value
                // of t using s = Dot((P2 + D2*t) - P1,D1) / Dot(D1,D1)= (t*b - c) / a
                // and clamp s to [0, 1]
                if (t.getValue() < 0.0f)
                {
                    t.setValue(0f);
                    s.setValue(Mathf.clamp01(-c / a));
                }
                else if (t.getValue() > 1.0f)
                {
                    t.setValue(1f);
                    s.setValue(Mathf.clamp01((b-c) / a));
                }
            }
        }
        c1.set(Vector3.add(p1, Vector3.mult(d1, s.getValue())));
        c2.set(Vector3.add(p2, Vector3.mult(d2, t.getValue())));
        return Vector3.dot(Vector3.sub(c1, c2), Vector3.sub(c1, c2));
    }


    /**
     * Compute the point R on (or in) B closest to P
     * @param p Point to check
     * @param b The bounding box
     * @return the Point R
     */
    public static Vector3 closestPtPointOBB(final Vector3 p, final OBBCollider b)
    {
        final Vector3 d = Vector3.sub(p, b.getGlobalCenter());
        //start result at center of box; make steps from there
        final Vector3 q = b.getGlobalCenter().copy();

        //for each obb axis
        for (int i = 0; i < 3; i++)
        {
            // ...project d onto that axis to get the distance
            // along the axis of d from the box center
            // If distance farther than the box extents, clamp to the box
            final float ei = b.getHalfWidthExtents().getIndex(i);
            final float dist = Mathf.clampSafe(Vector3.dot(d, b.getLocalAxes(i)), -ei, ei);

            //dist = Mathf.clamp(dist, -ei, ei);
            //if (dist > ei) dist = ei;
            //if (dist < -ei) dist = -ei;
            // Step that distance along the axis to get world coordinate

            //math
            //  b' = b * dist
            //  q = q + b'
            //q.set(Vector3.add(q, Vector3.mult(dist, b.getLocalAxes(i))));


            final Vector3 axesMultByDistance = Vector3.mult(b.getLocalAxes(i), dist);
            q.add(axesMultByDistance);
        }
        return q;
    }


    public static Vector3 closestPtOnObbToCapsule(final Vector3[] pts, final Vector3 capsuleA, final Vector3 capsuleB)
    {
        float min = Float.MAX_VALUE;
        Vector3 minVec = null;
        for (final Vector3 pt : pts)
        {
            final float tmp = sqDistPointSegment(capsuleA, capsuleB, pt);
            if (tmp < min)
            {
                min = tmp;
                minVec = pt;
            }
        }
        return minVec;
    }


    /**
     * Obtain the squared distance between the point P and the closest point on OBB B
     * @param p Point to check
     * @param b box to check
     * @return squared distance
     */
    public static float sqDistPointOBB(final Vector3 p, final OBBCollider b, final Vector3 closestPt)
    {
        final Vector3 closest = closestPtPointOBB(p, b);
        closestPt.set(closest);
        final Vector3 pClosest = Vector3.sub(closest, p);
        return pClosest.sqMagnitude();
    }

    /**
     * Computes the square distance between point p and OBB b
     * @param p point
     * @param b box
     * @return squared distance
     */
    public static float sqDistPointOBB(final Vector3 p, final OBBCollider b)
    {
        final Vector3 v = Vector3.sub(p, b.getGlobalCenter());
        float sqDist = 0.0f;
        for (int i = 0; i < 3; i++)
        {
            // Project vector from box center to p on each axis, getting the distance
            // of p along that axis, and count any excess distance outside box extents
            final float d = Vector3.dot(v, b.getLocalAxes(i)); //z.B. auf x(rechts) v projezieren
            float excess = 0.0f;
            final float ei = b.getHalfWidthExtents().getIndex(i);
            if (d < -ei)
                excess = d + ei;
            else if (d > ei)
                excess = d - ei;
            sqDist += excess * excess;
        }
        return sqDist;
    }
}
