package io.github.luigeneric.core.sector.management;

public class RespawnLocationInfo
{
    private final long sectorID;
    private final long carrierPlayerID;

    public RespawnLocationInfo(final long sectorID, final long carrierPlayerID)
    {
        this.sectorID = sectorID;
        this.carrierPlayerID = carrierPlayerID;
    }

    public long getSectorID()
    {
        return sectorID;
    }

    public long getCarrierPlayerID()
    {
        return carrierPlayerID;
    }
}
