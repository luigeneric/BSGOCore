package io.github.luigeneric.core.sector.zone;

import io.github.luigeneric.core.protocols.zone.TournamentRankingData;
import io.github.luigeneric.core.sector.SectorJob;
import io.github.luigeneric.core.sector.management.ObjectLeftSubscriber;
import io.github.luigeneric.core.sector.objleft.ObjectLeftDescription;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.templates.zonestemplates.ZoneTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SectorZoneManagement implements SectorJob, ObjectLeftSubscriber
{
    private final ZoneTemplate zoneTemplate;
    private final ZoneLeaderboard zoneLeaderboard;

    public SectorZoneManagement(ZoneTemplate zoneTemplate)
    {
        this.zoneTemplate = zoneTemplate;
        this.zoneLeaderboard = new ZoneLeaderboard(new HashMap<>(), new HashMap<>());
    }

    public List<TournamentRankingData> getTournamentRankingData()
    {
        return zoneLeaderboard.getTournamentRankingData();
    }

    public Optional<ZoneTemplate> getZoneTemplate()
    {
        return Optional.ofNullable(zoneTemplate);
    }
    public boolean isZone()
    {
        return zoneTemplate != null && zoneTemplate.sectorGuid() != 0;
    }

    @Override
    public void run()
    {
        if (!isZone())
            return;
    }

    @Override
    public void onUpdate(final ObjectLeftDescription arg)
    {
        if (RemovingCause.Death != arg.getRemovingCause())
            return;
    }
}
