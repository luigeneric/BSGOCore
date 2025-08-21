package io.github.luigeneric.linearalgebra.utility;


import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.StaticVectors;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;

public class Algorithm3D
{
    public static Vector3 getPointPositionRelativeTo(final Vector3 localPos, final Vector3 globalPos, final Quaternion globalRot)
    {
        return Vector3.add(Quaternion.mult(globalRot, localPos), globalPos);
    }

    public static Quaternion getRelativeRotationTo(final Quaternion globalRotation, final Quaternion localRotation)
    {
        return Quaternion.mult(globalRotation, localRotation);
    }

    /**
     * Consider two vectors and between an angle e.g. 90deg. (your WeaponAngle)
     * @param from vector order doesn't matter
     * @param to vector order doesn't matter
     * @param angle float value in deg. if set to 0, it's considered the same as 360deg
     * @return true if your weapon could fire
     */
    public static boolean isInsideAngle(final Vector3 from, final Vector3 to, final float angle)
    {
        return angle == 0f || Vector3.angle(from, to) <= angle;
    }

    public static boolean isInsideRange(final Vector3 fromTo, final float minDistance, final float maxDistance)
    {
        final float magSq = fromTo.sqMagnitude();

        return isInsideRangeSquared(magSq, minDistance * minDistance, maxDistance * maxDistance);
    }

    public static boolean isInsideRange(final Vector3 from, final Vector3 to, final float minDistance, final float maxDistance)
    {
        //calc distance
        final float distanceSq = from == to ? 0 : Vector3.distanceSquared(from, to);
        final float minDistanceSq = minDistance * minDistance;
        final float maxDistanceSq = maxDistance * maxDistance;

        return isInsideRangeSquared(distanceSq, minDistanceSq, maxDistanceSq);
    }


    public static boolean isInsideRangeSquared(final float distanceSq, final float minDistanceSq, final float maxDistanceSq)
    {
        if (minDistanceSq > maxDistanceSq)
            return isInsideRangeSquared(distanceSq, maxDistanceSq, minDistanceSq);

        return distanceSq <= maxDistanceSq && distanceSq >= minDistanceSq;
    }


    public static boolean isWeaponPositionInRange(final Transform shipTransformGlobal, final Transform weaponTransformLocal,
                                                  final Vector3 targetPos,
                                                  final float weaponMinRange, final float weaponMaxRange, final float weaponAngle)
    {
        //get the final position based on the global and local transforms
        final Transform tFinal = weaponTransformLocal.toGlobalSpaceOf(shipTransformGlobal);

        //pos to target vector (the direction)
        final Vector3 to = Vector3.sub(targetPos, tFinal.getPosition());

        final boolean isInRange = isInsideRange(to, weaponMinRange, weaponMaxRange);
        if (!isInRange)
        {
            return false;
        }

        final Vector3 from = Quaternion.mult(tFinal.getRotation(), StaticVectors.FORWARD);

        return isInsideAngle(from, to, weaponAngle);
    }
}
