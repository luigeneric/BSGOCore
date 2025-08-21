package io.github.luigeneric.core.protocols.game;

import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.spaceentities.PlayerShip;

public record SectorPlayerShipFetchResult(Sector sector, PlayerShip playerShip)
{
    public static final SectorPlayerShipFetchResult NO_SECTOR = new SectorPlayerShipFetchResult(null, null);

    public boolean hasSector()
    {
        return this.sector != null;
    }
    public boolean hasPlayerShip()
    {
        return this.playerShip != null;
    }

    public boolean bothPresent()
    {
        return hasSector() && hasPlayerShip();
    }
}
