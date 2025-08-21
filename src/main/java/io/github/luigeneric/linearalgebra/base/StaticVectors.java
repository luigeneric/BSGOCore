package io.github.luigeneric.linearalgebra.base;

public final class StaticVectors
{
    private StaticVectors(){}
    public static final Vector3 BACK = new Vector3(0, 0, -1f);
    public static final Vector3 DOWN = new Vector3(0, -1f, 0);
    public static final Vector3 FORWARD = UnmodifiableDecorator.wrap(Vector3.forward());

    public static final Vector3 LEFT = new Vector3(-1f, 0, 0);
    public static final Vector3 NEGATIVE_INFINITY = new Vector3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    public static final Vector3 ONE = new Vector3(1f, 1f, 1f);
    public static final Vector3 POSITIVE_INFINITY = new Vector3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    public static final Vector3 RIGHT = UnmodifiableDecorator.wrap(new Vector3(1, 0, 0));
    public static final Vector3 UP = UnmodifiableDecorator.wrap(new Vector3(0, 1, 0));
    public static final Vector3 ZERO = UnmodifiableDecorator.wrap(new Vector3(0, 0, 0));
}
