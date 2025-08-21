package io.github.luigeneric.core.protocols.notification;

public enum OutpostAttackedType
{
    OutpostUnderAttack(2),
    OutpostHeavyDamage(3),
    OutpostDied(4);

    public final byte value;

    OutpostAttackedType(int i)
    {
        this.value = (byte) i;
    }
}
