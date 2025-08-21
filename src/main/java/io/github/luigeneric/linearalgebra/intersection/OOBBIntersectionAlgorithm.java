package io.github.luigeneric.linearalgebra.intersection;

import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.linearalgebra.collidershapes.CollisionRecord;
import io.github.luigeneric.linearalgebra.collidershapes.OBBCollider;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OOBBIntersectionAlgorithm
{
    private OOBBIntersectionAlgorithm(){}


    public static CollisionRecord checkForCollision(final OBBCollider a, final OBBCollider b)
    {
        /**
        final boolean mayIntersect = PrimitiveIntersectionAlgorithms
                .intersectSphereSphere(a.getGlobalCenter(), a.getMaximumRadius(), b.getGlobalCenter(), b.getMaximumRadius());
        if (!mayIntersect)
            return null;
         */

        float s;
        float bestSep = Float.MAX_VALUE;
        Vector3 bestAxis = null;
        float tl;
        float ra, rb;


        final float[][] rotationMatrix = computeRotationMatrix(a, b);

        // Compute translation vector t
        final Vector3 t = Vector3.sub(b.getGlobalCenter(), a.getGlobalCenter());
        // translation into a’s coordinate frame
        final float tmpX = Vector3.dot(t, a.getLocalAxes(0));
        final float tmpY = Vector3.dot(t, a.getLocalAxes(1));
        final float tmpZ = Vector3.dot(t, a.getLocalAxes(2));
        t.set(tmpX, tmpY, tmpZ);


        // Compute common subexpressions. Add in an epsilon term to
        final float[][] absoluteRotationMatrix = computeAbsMatrix(rotationMatrix);
        //final Quaternion rOutMAbs = Quaternion.fromMatrix(new Matrix3x3(absoluteRotationMatrix));


        // Test axes L = A0, L = A1, L = A2
        //checked
        for (int i = 0; i < 3; i++)
        {
            ra = a.getHalfWidthExtents().getIndex(i);
            //ra2 = Vector3.dot(l, a.getHalfWidthExtents());
            rb = b.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[i][0] +
                    b.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[i][1] + b.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[i][2];
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
        //checked
        for (int i = 0; i < 3; i++)
        {
            ra = a.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[0][i] +
                    a.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[1][i] + a.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[2][i];
            rb = b.getHalfWidthExtents().getIndex(i);
            tl = Mathf.abs(t.getIndex(0) * rotationMatrix[0][i] + t.getIndex(1) * rotationMatrix[1][i] + t.getIndex(2) * rotationMatrix[2][i]);
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
        //checked
        ra = a.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[2][0] + a.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[1][0];
        rb = b.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[0][2] + b.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[0][1];
        tl = Mathf.abs(t.getIndex(2) * rotationMatrix[1][0] - t.getIndex(1) * rotationMatrix[2][0]);
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
        //checked
        ra = a.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[2][1] + a.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[1][1];
        rb = b.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[0][2] + b.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[0][0];
        tl = Mathf.abs(t.getIndex(2) * rotationMatrix[1][1] - t.getIndex(1) * rotationMatrix[2][1]);
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
        ra = a.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[2][2] + a.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[1][2];
        rb = b.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[0][1] + b.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[0][0];
        tl = Mathf.abs(t.getIndex(2) * rotationMatrix[1][2] - t.getIndex(1) * rotationMatrix[2][2]);
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
        ra = a.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[2][0] + a.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[0][0];
        rb = b.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[1][2] + b.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[1][1];
        tl  = Mathf.abs(t.getIndex(0) * rotationMatrix[2][0] - t.getIndex(2) * rotationMatrix[0][0]);
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
        ra = a.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[2][1] + a.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[0][1];
        rb = b.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[1][2] + b.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[1][0];
        tl = Mathf.abs(t.getIndex(1) * rotationMatrix[2][1] - t.getIndex(2) * rotationMatrix[0][1]);
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
        ra = a.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[2][2] + a.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[0][2];
        rb = b.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[1][1] + b.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[1][0];
        tl = Mathf.abs(t.getIndex(1) * rotationMatrix[2][2] - t.getIndex(2) * rotationMatrix[0][2]);
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
        ra = a.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[1][0] + a.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[0][0];
        rb = b.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[2][2] + b.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[2][1];
        tl = Mathf.abs(t.getIndex(1) * rotationMatrix[0][0] - t.getIndex(0) * rotationMatrix[1][0]);
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
        ra = a.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[1][1] + a.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[0][1];
        rb = b.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[2][2] + b.getHalfWidthExtents().getIndex(2) * absoluteRotationMatrix[2][0];
        tl = Mathf.abs(t.getIndex(1) * rotationMatrix[0][1] - t.getIndex(0) * rotationMatrix[1][1]);
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
        ra = a.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[1][2] + a.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[0][2];
        rb = b.getHalfWidthExtents().getIndex(0) * absoluteRotationMatrix[2][1] + b.getHalfWidthExtents().getIndex(1) * absoluteRotationMatrix[2][0];
        tl = Mathf.abs(t.getIndex(1) * rotationMatrix[0][2] - t.getIndex(0) * rotationMatrix[1][2]);
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
     * // counteract arithmetic errors when two edges are parallel and
     * // their cross product is (near) null (see text for details)
     *
     * @param rotationMatrix a float matrix for rotation
     * @return absolute rotation matrix
     */
    private static float[][] computeAbsMatrix(final float[][] rotationMatrix)
    {
        final float[][] absoluteRotationMatrix = new float[3][3];
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                absoluteRotationMatrix[i][j] = Mathf.abs(rotationMatrix[i][j]) + Mathf.EPSILON;
            }
        }
        return absoluteRotationMatrix;
    }

    /**
     * Compute rotation matrix expressing b in a’s coordinate frame
     * @param a
     * @param b
     * @return Rotated matrix
     */
    private static float[][] computeRotationMatrix(OBBCollider a, OBBCollider b)
    {
        final float[][] R = new float[3][3];
        for (int i = 0; i < 3; i++)
        {
            final Vector3 localI = a.getLocalAxes(i);
            for (int j = 0; j < 3; j++)
            {
                R[i][j] = Vector3.dot(localI, b.getLocalAxes(j));
            }
        }
        return R;
    }
}
