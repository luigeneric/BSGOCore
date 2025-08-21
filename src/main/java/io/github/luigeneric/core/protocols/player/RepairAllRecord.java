package io.github.luigeneric.core.protocols.player;

public record RepairAllRecord(int shipId, boolean useCubits, float costsShipRepair, float costsAllSystems)
{
    @Override
    public String toString()
    {
        return "RepairAllRecord{" +
                "shipId=" + shipId +
                ", useCubits=" + useCubits +
                ", costsShipRepair=" + costsShipRepair +
                ", costsAllSystems=" + costsAllSystems +
                ", costsAll=" + (costsShipRepair+costsAllSystems) +
                '}';
    }
}
