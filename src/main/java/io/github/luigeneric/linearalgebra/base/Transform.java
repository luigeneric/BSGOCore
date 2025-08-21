package io.github.luigeneric.linearalgebra.base;


import io.github.luigeneric.utils.ICopy;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Transform implements ICopy<Transform>
{
    private final Vector3 position;
    private final Quaternion rotation;

    public Transform(final Vector3 position, final Quaternion rotation, final boolean copy)
    {
        Objects.requireNonNull(position);
        Objects.requireNonNull(rotation);

        if (copy)
        {
            this.position = position.copy();
            this.rotation = rotation.copy();
        }
        else
        {
            this.position = position;
            this.rotation = rotation;
        }
    }
    public Transform(final Vector3 position, final Euler3 euler3, final boolean copy)
    {
        this(position, euler3.quaternion(), copy);
    }

    /**
     * Standard constructor, won't copy the given position and rotation arguments
     * @param position point of the Transform, using the reference
     * @param rotation rotation of the Transform using the reference
     */
    public Transform(final Vector3 position, final Quaternion rotation)
    {
        this(position, rotation, false);
    }

    public Transform(final Vector3 position, final Euler3 euler3)
    {
        this(position, euler3.quaternion(), false);
    }

    public Transform(final Vector3 position)
    {
        this(position, Quaternion.identity());
    }

    public Transform()
    {
        this(Vector3.zero());
    }
    public static Transform identity()
    {
        return new Transform(Vector3.zero(), Quaternion.identity(), false);
    }
    private Transform(final Transform transform)
    {
        this(transform.position, transform.rotation, true);
    }

    @Override
    public Transform copy()
    {
        return new Transform(this);
    }


    /**
     * // Return the inverse of the transform
     * RP3D_FORCE_INLINE Transform Transform::getInverse() const {
     *     const Quaternion& invQuaternion = mOrientation.getInverse();
     *     return Transform(invQuaternion * (-mPosition), invQuaternion);
     * }
     * @return a reference
     */
    public Transform inverse()
    {
        final Transform invT = Transform.inverse(this);
        return this.setTransform(invT);
    }
    public static Transform inverse(final Transform t)
    {
        final Quaternion invQ = Quaternion.inverse(t.getRotation());

        final Vector3 invPos = Vector3.invert(t.getPosition());
        final Vector3 newPos = invQ.mult(invPos);
        return new Transform(newPos, invQ);
    }
    public Transform setTransform(final Transform transform)
    {
        this.position.set(transform.position);
        this.rotation.setRotation(transform.rotation);

        return this;
    }
    public void setPositionRotation(final Vector3 position, final Quaternion rotation)
    {
        this.position.set(position);
        this.rotation.setRotation(rotation);
    }

    /**
     * Calculates the transform in the globalChild space of another transform.
     * EXAMPLE Transform localShip1 = Transform.toLocal(globalPlanetoid, globalShip1);
     *      puts globalShip1 into the local space of globalPlanetoid
     * @param globalParent The globalParent transform to calculate the globalChild space in.
     * @param globalChild The globalChild transform to convert to the globalParent transform's space.
     * @return The transformed globalChild transform.
     */
    public static Transform toLocal(final Transform globalParent, final Transform globalChild)
    {
        final Quaternion inverseRotation = Quaternion.inverse(globalParent.rotation);
        final Vector3 relativePosition = inverseRotation.mult(Vector3.sub(globalChild.position, globalParent.position));
        final Quaternion relativeRotation = Quaternion.mult(inverseRotation, globalChild.rotation);
        return new Transform(relativePosition, relativeRotation);
    }

    /**
     * Computes the local transform of this object in the space of a given global transform.
     * This operation calculates the relative transformation by combining the global and local transformations.
     * Note: This does not modify the current transform.
     *
     * Transforms a local Transform into the space of a global Transform by
     *      * creating a new Transform
     *
     * @param global The global transform in which this local transform is to be evaluated.
     * @return A new Transform representing this local transform in the space of the global transform.
     */
    public Transform toGlobalSpaceOf(final Transform global)
    {
        //relative rotation
        final Quaternion relativeRotation = Quaternion.mult(global.rotation, this.rotation);
        final Vector3 relativePosition = Quaternion.mult(global.rotation, this.position);
        relativePosition.add(global.position);

        return new Transform(relativePosition, relativeRotation);
    }

    /**
     * Is the same as old "applyOnVector3" but slightly better performance due to less memory consumption
     * Can also be used to get the relative position:
     *  invert this Transform, multiply inverse with Vector b. it will return the relative position of Vector b to this
     * @param vec Vector3 to multiply with
     * @return a new Vector3 with applied Transformation(Rotation + Position)
     */
    public Vector3 applyTransform(final Vector3 vec)
    {
        final Vector3 newPos = Quaternion.mult(this.rotation, vec);
        return newPos.add(this.position);
    }

    /**
     *
     * @param quaternion
     * @param coordinateSystemType
     */
    public void rotate(final Quaternion quaternion, final CoordinateSystemType coordinateSystemType)
    {
        if (coordinateSystemType == CoordinateSystemType.Self)
        {
            this.rotation.mult(quaternion);
        }
        //world rotation
        else
        {
            this.rotation.mult(Quaternion.inverse(this.rotation).mult(quaternion).mult(this.rotation));
        }
    }
    public void rotate(final Vector3 eulerAngles, final CoordinateSystemType coordinateSystemType)
    {
        final Quaternion quaternion = Quaternion.euler(eulerAngles);
        rotate(quaternion, coordinateSystemType);
    }

    /**
     * Translates the current position by a Vector translation
     * @param translation moves by the translation Vector
     * @param relativeTo World or Local coordinate system
     */
    public void translate(final Vector3 translation, final CoordinateSystemType relativeTo)
    {
        if (relativeTo == CoordinateSystemType.World)
        {
            this.position.add(translation);
        }
        else
        {
            //final Vector3 relativeVector = relativeToMyPosition(translation);
            //this.position.add(relativeVector);
            this.position.add(this.transformDirection(translation));
        }
    }

    public Vector3 transformDirection(final Vector3 direction)
    {
        return this.rotation.mult(direction);
    }


    /**
     * My Position is center000, v is calculated relative to me
     * @param v vector that should be calculated relative to my position
     * @return a new Vector
     */
    public Vector3 relativeToMyPosition(final Vector3 v)
    {
        final Vector3 tmp = Vector3.sub(v, position);
        tmp.set(Quaternion.inverse(rotation).mult(tmp));
        return tmp;
    }

    public Euler3 getRotationEuler3()
    {
        return Euler3.fromQuaternion(rotation);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transform transform = (Transform) o;

        if (!position.equals(transform.position)) return false;
        return rotation.equals(transform.rotation);
    }

    @Override
    public int hashCode()
    {
        int result = position.hashCode();
        result = 31 * result + rotation.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Transform{" +
                "position=" + position +
                ", rotation=" + rotation +
                '}';
    }

    public enum CoordinateSystemType
    {
        /**
         * global
         */
        World,
        /**
         * local
         */
        Self
    }
}
