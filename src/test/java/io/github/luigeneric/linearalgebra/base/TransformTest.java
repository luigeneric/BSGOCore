package io.github.luigeneric.linearalgebra.base;

import io.quarkus.logging.Log;
import org.junit.jupiter.api.Test;
class TransformTest
{
    @Test
    void testLocalSpace()
    {
        final Transform a = new Transform(new Vector3(4,4,4), Quaternion.identity());
        final var invA = Transform.inverse(a);
        final Vector3 b = new Vector3(1, 0, 0);
        final var inside = a.applyTransform(b);


        System.out.println("inside: " + inside);
    }

    @Test
    void testTranslation()
    {
        Transform transform = new Transform(new Vector3(0, 20, 0), Quaternion.identity());

        transform.translate(new Vector3(0, 50, 0), Transform.CoordinateSystemType.Self);

        System.out.println(transform);
    }

    @Test
    void testRotationOfWeapons()
    {
        var globalQ = new Quaternion(-6.657903E-08f, 0, 0, 1);
        var bullet11Q = new Quaternion(0.9659258, -3.139461E-07, -8.412166E-08, 0.2588191);
        Transform globalT = new Transform(Vector3.zero(), globalQ);
        Transform bulletT = new Transform(Vector3.zero(), bullet11Q);

        final Transform finalT = Transform.toLocal(globalT, bulletT);
        Log.info(finalT);

    }

    @Test
    void testBInLocalSpaceOfA()
    {


    }
}