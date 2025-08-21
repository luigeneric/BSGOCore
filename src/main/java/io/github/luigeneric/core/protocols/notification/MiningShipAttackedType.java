package io.github.luigeneric.core.protocols.notification;

public enum MiningShipAttackedType
{
    ShipUnderAttackSimple(1),
    ShipUnderAttack(2),
    /**
     * Heavy damage
     */
    ShipDamaged(3),
    ShipDrivenOff(4);

    public final byte value;

    MiningShipAttackedType(int i)
    {
        this.value = (byte) i;
    }
}
