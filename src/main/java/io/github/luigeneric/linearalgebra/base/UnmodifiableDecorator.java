package io.github.luigeneric.linearalgebra.base;

public final class UnmodifiableDecorator
{
    private static final String UNSUPPORTED_EXCEPTION_MSG = "Vector3 not modifiable";
    public static UnmodifiableVector3 wrap(final Vector3 vec)
    {
        return new UnmodifiableVector3(vec);
    }
    public static UnmodifiableEuler3 wrap(final Euler3 euler3)
    {
        return new UnmodifiableEuler3(euler3);
    }
    public static UnmodifiableQuaternion wrap(final Quaternion q)
    {
        return new UnmodifiableQuaternion(q);
    }
    public static class UnmodifiableVector3 extends Vector3
    {
        public UnmodifiableVector3(Vector3 vec)
        {
            super(vec);
        }

        @Override
        public float[] toArray()
        {
            return super.toArray();
        }

        @Override
        public void scale(Euler3 scale)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 scale(final Vector3 scale)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 scale(float scale)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void set(Vector3 vector3)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void set(float x, float y, float z)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 normalize()
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 clamp(float min, float max)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 abs()
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 add(Vector3 vec)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 add(float num)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 sub(Vector3 vec)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 sub(float num)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 mult(float num)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void addX(float value)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void addY(float value)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void addZ(float value)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void setX(float value)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void setY(float value)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void setZ(float value)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Vector3 invert()
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }
    }


    public static class UnmodifiableQuaternion extends Quaternion
    {
        private static final String UNSUPPORTED_EXCEPTION_MSG = "Quaternion not modifiable";
        public UnmodifiableQuaternion(float x, float y, float z, float w)
        {
            super(x, y, z, w);
        }

        public UnmodifiableQuaternion(Vector3 xyz, float w)
        {
            super(xyz, w);
        }

        public UnmodifiableQuaternion(Quaternion q)
        {
            super(q);
        }

        @Override
        public Quaternion mult(final Quaternion rotation)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void mult(float f)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Quaternion inverse()
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void normalize()
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void setEulerAngles(Vector3 vector3)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Quaternion setRotation(Quaternion newRotation) throws NullPointerException
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }
    }

    public static class UnmodifiableEuler3 extends Euler3
    {
        private static final String UNSUPPORTED_EXCEPTION_MSG = "Euler3 not modifiable";
        public UnmodifiableEuler3(double pitch, double yaw, double roll)
        {
            super(pitch, yaw, roll);
        }

        public UnmodifiableEuler3(Euler3 toCopyEuler3)
        {
            super(toCopyEuler3);
        }

        @Override
        public void clamp(Euler3 fromTo)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void setPitch(float pitch)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void setYaw(float yaw)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void setEuler3(Euler3 euler3)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void setEuler3(float pitch, float yaw, float roll)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public void setRoll(float roll)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Euler3 mult(float num)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }

        @Override
        public Euler3 add(Euler3 other)
        {
            throw new UnsupportedOperationException(UNSUPPORTED_EXCEPTION_MSG);
        }
    }
}
