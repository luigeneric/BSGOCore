package io.github.luigeneric.linearalgebra.base;

public final class StaticQuaternions
{
    public StaticQuaternions(){}

    public static final Quaternion IDENTITY = UnmodifiableDecorator.wrap(new Quaternion(0f, 0f, 0f, 1f));
}
