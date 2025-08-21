package io.github.luigeneric.templates.sectortemplates;


import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.SpaceObjectTemplate;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

public class SectorDesc
{
    @Getter
    private final long sectorID;
    @Getter
    private final long zoneGUID;
    @Getter
    private final List<PlayerSpawnAreaTemplate> spawnAreaTemplates;
    @Getter
    private final List<SpaceObjectTemplate> spaceObjectTemplates;
    @Getter
    private final AsteroidDesc asteroidDesc;
    @Getter
    private final PlanetoidDesc planetoidDesc;
    @Getter
    private final MiningShipConfig miningShipConfig;
    @Getter
    private final List<BotSpawnTemplate> botSpawnTemplates;
    @Getter
    private final OutpostProgressTemplate colonialProgressTemplate;
    @Getter
    private final OutpostProgressTemplate cylonProgressTemplate;
    private final CometSectorDesc cometSectorDesc;


    public SectorDesc(final long sectorID,
                      final long zoneGUID,
                      final List<PlayerSpawnAreaTemplate> spawnAreaTemplates,
                      final List<SpaceObjectTemplate> spaceObjectTemplates,
                      final AsteroidDesc asteroidDesc,
                      final PlanetoidDesc planetoidDesc,
                      final MiningShipConfig miningShipConfig,
                      final List<BotSpawnTemplate> botSpawnTemplates,
                      final OutpostProgressTemplate colonialProgressTemplate,
                      final OutpostProgressTemplate cylonProgressTemplate,
                      final CometSectorDesc cometSectorDesc
    )
    {
        this.sectorID = sectorID;
        this.zoneGUID = zoneGUID;
        this.spawnAreaTemplates = spawnAreaTemplates;
        this.spaceObjectTemplates = spaceObjectTemplates;
        this.asteroidDesc = asteroidDesc;
        this.planetoidDesc = planetoidDesc;
        this.miningShipConfig = miningShipConfig;
        this.botSpawnTemplates = botSpawnTemplates;
        this.colonialProgressTemplate = colonialProgressTemplate;
        this.cylonProgressTemplate = cylonProgressTemplate;
        this.cometSectorDesc = cometSectorDesc;
    }

    public CometSectorDesc getCometSectorDesc()
    {
        return Objects.requireNonNullElseGet(cometSectorDesc, () -> new CometSectorDesc(0, false, 0));
    }
}
