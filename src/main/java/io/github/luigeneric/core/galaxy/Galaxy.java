package io.github.luigeneric.core.galaxy;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.galaxy.galaxymapupdates.*;
import io.github.luigeneric.core.protocols.universe.UniverseProtocolWriteOnly;
import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.sector.management.GalaxyBonus;
import io.github.luigeneric.core.sector.management.OutpostState;
import io.github.luigeneric.core.sector.management.SectorRegistry;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.utility.Mathf;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.utils.MapStarDesc;
import io.github.luigeneric.utils.TimestampedCounter;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class Galaxy
{
    @Getter
    private final GalaxyMapCard galaxyMapCard;
    private final Map<Long, IGalaxySubscriber> galaxySubscribers;
    private final SectorRegistry sectorRegistry;
    private List<GalaxyMapUpdate> updateLstColonial;
    private List<GalaxyMapUpdate> updateLstCylon;
    private final GalaxyBonus galaxyBonus;
    private final UniverseProtocolWriteOnly universeWriter;
    private final Lock lock;

    public Galaxy(final SectorRegistry sectorRegistry, final GalaxyBonus galaxyBonus, Catalogue catalogue)
    {
        this.galaxyMapCard = catalogue.galaxyMapCard();
        this.galaxySubscribers = new HashMap<>();
        this.sectorRegistry = sectorRegistry;
        this.galaxyBonus = galaxyBonus;
        this.lock = new ReentrantLock();
        this.universeWriter = new UniverseProtocolWriteOnly();
    }

    private void lock()
    {
        lock.lock();
    }
    private void unlock()
    {
        lock.unlock();
    }

    public void addSubscriber(final IGalaxySubscriber galaxySubscriber)
    {
        lock();
        try
        {
            this.galaxySubscribers.put(galaxySubscriber.getID(), galaxySubscriber);
            final List<GalaxyMapUpdate> updateLst = galaxySubscriber.getFaction().equals(Faction.Colonial) ?
                    this.updateLstColonial : this.updateLstCylon;
            final BgoProtocolWriter bw = universeWriter.writeUpdates(updateLst);
            galaxySubscriber.mapUpdateReceived(bw);
        }
        finally
        {
            unlock();
        }
    }
    public void removeSubscriber(final IGalaxySubscriber galaxySubscriber)
    {
        lock();
        try
        {
            this.galaxySubscribers.remove(galaxySubscriber.getID());
        }
        finally
        {
            unlock();
        }
    }

    private List<GalaxyMapUpdate> getAllGalaxyMapUpdates()
    {
        final List<GalaxyMapUpdate> galaxyMapUpdates = new ArrayList<>();

        //is outpost?
        galaxyMapUpdates.addAll(this.getOutpostStates(Faction.Colonial));
        galaxyMapUpdates.addAll(this.getOutpostStates(Faction.Cylon));

        //is miningShip in Sector?
        galaxyMapUpdates.addAll(this.getMiningShipsUpdate());

        //RCP update is for all
        galaxyMapUpdates.addAll(getRCPUpdate(100, 1));

        //getKillCounterUpdates
        galaxyMapUpdates.addAll(this.getKillCount());

        //testing
        galaxyMapUpdates.addAll(getDynamicMissionUpdate());

        return galaxyMapUpdates;
    }

    private List<GalaxyMapUpdate> getDynamicMissionUpdate()
    {
        final List<GalaxyMapUpdate> galaxyMapUpdates = new ArrayList<>();

        //galaxyMapUpdates.add(new SectorDynamicMissionUpdate(Faction.Neutral, 10L, (short) 1));
        //galaxyMapUpdates.add(new SectorDynamicMissionUpdate(Faction.Colonial, 46, (short) 1));
        //galaxyMapUpdates.add(new SectorDynamicMissionUpdate(Faction.Cylon, 47, (short) 1));
        galaxyMapUpdates.add(new SectorDynamicMissionUpdate(Faction.Ancient, 63, (short) 1));

        return galaxyMapUpdates;
    }

    private List<GalaxyMapUpdate> getRCPUpdate(final float opRCP, final float miningRCP)
    {
        final List<GalaxyMapUpdate> galaxyMapUpdates = new ArrayList<>();

        float rcpColo = 0f;
        float rcpCylo = 0f;

        for (MapStarDesc star : this.galaxyMapCard.getStars().values())
        {
            final Optional<Sector> optSector = this.sectorRegistry.getSectorById(star.getId());
            if (optSector.isEmpty())
                continue;
            final Sector sector = optSector.get();

            final List<SpaceObject> miningShips = sector.getCtx().spaceObjects().getSpaceObjectsOfEntityType(SpaceEntityType.MiningShip);
            final long sizeTotalMiningShips = miningShips.size();
            final long colonialMiningShips = miningShips
                    .stream()
                    .filter(spaceObject -> spaceObject.getFaction().equals(Faction.Colonial))
                    .count();
            final long cylonMiningShips = sizeTotalMiningShips - colonialMiningShips;

            rcpColo += miningRCP * colonialMiningShips;
            rcpCylo += miningRCP * cylonMiningShips;

            final OutpostState coloOpState = sector.getColonialOpState();
            final OutpostState cyloOpState = sector.getCylonOpState();
            final float deltaColo = coloOpState.getDelta();
            final float deltaCylo = cyloOpState.getDelta();

            rcpColo += deltaColo == 1f ? opRCP : 0f;
            rcpCylo += deltaCylo == 1f ? opRCP : 0f;
        }



        final GalaxyMapUpdate rcpColonial = new GalaxyRcpUpdate(Faction.Colonial, rcpColo);
        final GalaxyMapUpdate rcpCylon = new GalaxyRcpUpdate(Faction.Cylon, rcpCylo);

        galaxyMapUpdates.add(rcpColonial);
        galaxyMapUpdates.add(rcpCylon);

        updateBonusRcpSector(rcpColo, rcpCylo);

        return galaxyMapUpdates;
    }
    public List<GalaxyMapUpdate> getKillCount()
    {
        final List<GalaxyMapUpdate> killUpdates = new ArrayList<>();
        //get all kill-counters of sectorguid, faction, killcount
        for (MapStarDesc star : this.galaxyMapCard.getStars().values())
        {
            final Optional<Sector> optSector = this.sectorRegistry.getSectorById(star.getId());
            if (optSector.isEmpty())
                continue;
            final Sector sector = optSector.get();
            final TimestampedCounter colonialKilledCounter = sector.getSpaceObjectRemover().getKillCounterFaction(Faction.Colonial);
            final TimestampedCounter cylonKilledCounter = sector.getSpaceObjectRemover().getKillCounterFaction(Faction.Cylon);
            final GalaxyMapUpdate coloKilledCounterUpdate = new SectorPvpKillUpdate(
                    colonialKilledCounter.getFaction(),
                    sector.getId(),
                    colonialKilledCounter.getCount()
            );
            final GalaxyMapUpdate cylonKilledCounterUpdate = new SectorPvpKillUpdate(
                    cylonKilledCounter.getFaction(),
                    sector.getId(),
                    cylonKilledCounter.getCount()
            );

            killUpdates.add(coloKilledCounterUpdate);
            killUpdates.add(cylonKilledCounterUpdate);
        }

        return killUpdates;
    }

    public void updateBonusRcpSector(final float colonialRcp, final float cylonRcp)
    {
        final double miningBonusColo = this.getMiningBonus(colonialRcp);
        final double miningBonusCylo = this.getMiningBonus(cylonRcp);

        this.galaxyBonus.setMiningBonus(Faction.Colonial, miningBonusColo);
        this.galaxyBonus.setMiningBonus(Faction.Cylon, miningBonusCylo);

        this.galaxyBonus.setOpBonus(Faction.Colonial, getOpBonus(colonialRcp, cylonRcp, true));
        this.galaxyBonus.setOpBonus(Faction.Cylon, getOpBonus(colonialRcp, cylonRcp, false));
    }

    private float getOpBonus(final float rcpColo, final float rcpCylo, final boolean isColo)
    {
        final float base = this.galaxyMapCard.getBaseScalingMultiplier();
        final float num = isColo ? rcpColo : rcpCylo;
        final float num2 = isColo ? rcpCylo : rcpColo;

        final float num3 = num - num2;
        final float num4 = Mathf.abs(num3);
        final List<Integer> list = new ArrayList<>(this.galaxyMapCard.getSectorScalingMultiplier().keySet());
        list.sort(Integer::compare);
        Collections.reverse(list);
        int num5 = 0;
        for (int num6 : list)
        {
            if (num4 >= (float) num6)
            {
                num5 = this.galaxyMapCard.getSectorScalingMultiplier().get(num6);
                break;
            }
        }
        num5 *= (int) base;
        if (num3 > 0)
            num5 *= -1;

        return num5;
    }

    //in client GetBonus
    private double getMiningBonus(final float rcp)
    {
        final int[] tiers = this.galaxyMapCard.getTiers();
        if (rcp < tiers[0])
            return 0f;

        for (int i = 1; i < tiers.length; i++)
        {
            if (tiers[i-1] <= rcp && rcp < tiers[i])
            {
                return 5. * (double)i / 100.;
            }
        }
        final double baseBonus = 5. * (double) tiers.length;
        return baseBonus * 0.01;
    }

    private List<GalaxyMapUpdate> getGalaxyMapUpdates(final Faction faction)
    {
        final List<GalaxyMapUpdate> galaxyMapUpdates = new ArrayList<>();

        galaxyMapUpdates.addAll(this.getGalaxyMapUpdatesConquest(faction));
        galaxyMapUpdates.addAll(this.getOutpostPoints(faction));

        //heavy fight blinking sector if the kill-count number is geater equal to 3
        //galaxyMapUpdates.add(new SectorPvpKillUpdate(Faction.Colonial, 10, 2));
        //galaxyMapUpdates.add(new SectorPvpKillUpdate(Faction.Cylon, 10, 3));


        for (final Sector sector : this.sectorRegistry.getSectors())
        {
            final GalaxyMapUpdate slotUd = new SectorPlayerSlotUpdate(faction, sector.getId(), sector.getSectorSlotData());
            galaxyMapUpdates.add(slotUd);
        }

        return galaxyMapUpdates;
    }

    private Collection<? extends GalaxyMapUpdate> getMiningShipsUpdate()
    {
        final List<GalaxyMapUpdate> galaxyMapUpdates = new ArrayList<>();

        for (final MapStarDesc star : this.galaxyMapCard.getStars().values())
        {
            final Optional<Sector> optSector = this.sectorRegistry.getSectorById(star.getId());
            if (optSector.isEmpty()) continue;
            final Sector sector = optSector.get();
            //VERSION 1
            final Map<Boolean, List<SpaceObject>> coloMiningCount = sector.getCtx().spaceObjects()
                    .getSpaceObjectsOfEntityType(SpaceEntityType.MiningShip).stream()
                    .collect(Collectors.partitioningBy(x -> x.getFaction().equals(Faction.Colonial)));
            final int coloSize = coloMiningCount.get(true).size();
            final int cyloSize = coloMiningCount.get(false).size();

            //this one is less intense but less accurate
            //VERSION 2
            /*
            final int anyColo = sector.getSpaceObjects().getSpaceObjectsOfEntityType(SpaceEntityType.MiningShip)
                    .stream().anyMatch(obj -> obj.getFaction().equals(Faction.Colonial)) ? 1 : 0;
            final int anyCylo = sector.getSpaceObjects().getSpaceObjectsOfEntityType(SpaceEntityType.MiningShip)
                    .stream().anyMatch(obj -> obj.getFaction().equals(Faction.Cylon)) ? 1 : 0;
             */

            galaxyMapUpdates.add(new SectorMiningShipUpdate(Faction.Colonial, star.getId(), coloSize));
            galaxyMapUpdates.add(new SectorMiningShipUpdate(Faction.Cylon, star.getId(), cyloSize));
        }

        return galaxyMapUpdates;
    }

    private List<GalaxyMapUpdate> getOutpostStates(final Faction faction)
    {
        final List<GalaxyMapUpdate> galaxyMapUpdates = new ArrayList<>();

        for (final MapStarDesc star : this.galaxyMapCard.getStars().values())
        {
            final Optional<Sector> optSector = this.sectorRegistry.getSectorById(star.getId());
            if (optSector.isEmpty())
            {
                log.warn("Skipping sector in getOutpostStates {} because it's null", star.getId());
                continue;
            }
            final Sector sector = optSector.get();
            final OutpostState opState = faction.equals(Faction.Colonial) ? sector.getColonialOpState() : sector.getCylonOpState();
            galaxyMapUpdates.add(new SectorOutpostStateUpdate(faction, sector.getId(), opState.getDelta()));
        }

        return galaxyMapUpdates;
    }
    private List<GalaxyMapUpdate> getOutpostPoints(final Faction faction)
    {
        List<GalaxyMapUpdate> galaxyMapUpdates = new ArrayList<>();

        for (final MapStarDesc star : this.galaxyMapCard.getStars().values())
        {
            final Optional<Sector> optSector = this.sectorRegistry.getSectorById(star.getId());
            if (optSector.isEmpty())
            {
                log.warn("Skipping sector in getOutpostPoints {} because it's null", star.getId());
                continue;
            }
            final Sector sector = optSector.get();
            final OutpostState opState = faction.equals(Faction.Colonial) ? sector.getColonialOpState() : sector.getCylonOpState();
            galaxyMapUpdates.add(new SectorOutpostPointsUpdate(faction, sector.getId(), opState.getOpPoints()));
        }

        return galaxyMapUpdates;
    }

    private List<GalaxyMapUpdate> getGalaxyMapUpdatesConquest(final Faction faction)
    {
        List<GalaxyMapUpdate> galaxyMapUpdates = new ArrayList<>();

        if (faction.equals(Faction.Colonial))
        {
            //final GalaxyMapUpdate conquestCol = new ConquestLocationUpdate(Faction.Colonial, 10, LocalDateTime.now(Clock.systemUTC()).plusMinutes(10));
            //galaxyMapUpdates.add(conquestCol);
            final GalaxyMapUpdate conquestPrice = new ConquestPriceUpdate(Faction.Colonial, 9999);
            galaxyMapUpdates.add(conquestPrice);
        }
        else
        {
            //final GalaxyMapUpdate conquestCol = new ConquestLocationUpdate(Faction.Colonial, 10, LocalDateTime.now(Clock.systemUTC()).plusMinutes(10));
            //galaxyMapUpdates.add(conquestCol);
            final GalaxyMapUpdate conquestPrice = new ConquestPriceUpdate(Faction.Cylon, 9999);
            galaxyMapUpdates.add(conquestPrice);
        }
        return galaxyMapUpdates;
    }


    /**
     * The Update loop of the galaxy
     */
    @Scheduled(every = "1s")
    @RunOnVirtualThread
    public void run()
    {
        lock();
        try
        {
            final List<GalaxyMapUpdate> allUpdates = this.getAllGalaxyMapUpdates();

            this.updateLstColonial =  this.getGalaxyMapUpdates(Faction.Colonial);
            this.updateLstCylon = this.getGalaxyMapUpdates(Faction.Cylon);

            this.updateLstColonial.addAll(allUpdates);
            this.updateLstCylon.addAll(allUpdates);

            final BgoProtocolWriter bwColonial = universeWriter.writeUpdates(updateLstColonial);
            final BgoProtocolWriter bwCylon = universeWriter.writeUpdates(updateLstCylon);


            for (final IGalaxySubscriber galaxySubscriber : this.galaxySubscribers.values())
            {
                final BgoProtocolWriter bw = galaxySubscriber.getFaction().equals(Faction.Colonial) ?
                        bwColonial : bwCylon;

                galaxySubscriber.mapUpdateReceived(bw);
            }
        }
        finally
        {
            unlock();
        }
    }
}
