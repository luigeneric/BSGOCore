package io.github.luigeneric.core.sector.management;


import io.github.luigeneric.MicrometerRegistry;
import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.sector.creation.SectorFactory;
import io.github.luigeneric.core.sector.creation.SectorRandomGenerationUtils;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.cards.ZoneCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.utils.MapStarDesc;
import io.github.luigeneric.utils.Utils;
import io.quarkus.virtual.threads.VirtualThreads;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class SectorRegistry
{
    private final SectorFactory sectorFactory;

    private final ExecutorService executorService;
    private final Map<Long, Sector> sectorMap;
    private final Map<Long, Sector> zonesMap;
    private boolean isShutdown;
    private final Catalogue catalogue;
    private final MicrometerRegistry micrometerRegistry;
    @Getter
    private final SectorRandomGenerationUtils sectorRandomGenerationUtils;

    public SectorRegistry(final SectorFactory sectorFactory, Catalogue catalogue, final MicrometerRegistry micrometerRegistry,
                          final SectorRandomGenerationUtils sectorRandomGenerationUtils, @VirtualThreads final ExecutorService executorService)
    {
        this.sectorFactory = sectorFactory;
        this.catalogue = catalogue;
        this.isShutdown = false;
        this.micrometerRegistry = micrometerRegistry;

        this.executorService = executorService;
        this.sectorMap = new ConcurrentSkipListMap<>();
        this.zonesMap = new ConcurrentSkipListMap<>();
        this.sectorRandomGenerationUtils = sectorRandomGenerationUtils;
        setupStandardSectors();
    }

    public void setupStandardSectors()
    {
        final Optional<GalaxyMapCard> optGalaxMapCard = catalogue.fetchCard(StaticCardGUID.GalaxyMap, CardView.GalaxyMap);
        if (optGalaxMapCard.isEmpty())
            return;
        final GalaxyMapCard galaxyMapCard = optGalaxMapCard.get();

        for (Map.Entry<Long, MapStarDesc> mapStar : galaxyMapCard.getStars().entrySet())
        {
            final long id = mapStar.getValue().getId();
            final Sector tmpSector = sectorFactory.createSector(id);
            micrometerRegistry.setMeterRegistrySectorUsers(tmpSector);
            this.addSector(tmpSector);
        }
    }
    public void startZone(final long zoneGuid)
    {
        final Optional<ZoneCard> zoneCard = catalogue.fetchCard(zoneGuid, CardView.Zone);

    }


    public void addSector(final Sector sector) throws IllegalStateException
    {
        if (isShutdown)
        {
            throw new IllegalStateException("SectorRegistry is shutdown, no new sectors available!");
        }
        sectorMap.put(sector.getId(), sector);
        executorService.execute(sector);
    }

    public Optional<Sector> getSectorById(long id)
    {
        return Optional.ofNullable(sectorMap.get(id));
    }
    public List<Sector> getSectors()
    {
        return this.sectorMap.values().stream().toList();
    }

    public SectorFactory getSectorFactory()
    {
        return sectorFactory;
    }


    public void shutdown()
    {
        this.isShutdown = true;
        this.sectorMap.values().forEach(Sector::shutDownSector);
        Utils.jdk20CloseExecutorServiceLanguageLevel(executorService);
    }
}
