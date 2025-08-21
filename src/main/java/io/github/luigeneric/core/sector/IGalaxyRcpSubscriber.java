package io.github.luigeneric.core.sector;

public interface IGalaxyRcpSubscriber
{
    void updateMiningSpeedBonus(final float bonusColonial, final float bonusCylon);
    void updateOutpostHpBonus(final float bonusColonial, final float bonusCylon);
}
