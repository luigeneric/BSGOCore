package io.github.luigeneric.core.protocols.dialog;


import io.github.luigeneric.core.galaxy.Galaxy;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.counters.Mission;
import io.github.luigeneric.core.player.counters.MissionBook;
import io.github.luigeneric.core.player.counters.MissionCountable;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.cards.MissionCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.missiontemplates.MissionSectorDesc;
import io.github.luigeneric.templates.missiontemplates.MissionTemplate;
import io.github.luigeneric.templates.missiontemplates.MissionTemplates;
import io.github.luigeneric.templates.utils.MapStarDesc;
import io.github.luigeneric.utils.BgoRandom;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class MissionDistributor
{
    private final Player player;
    private final Galaxy galaxy;
    private final BgoRandom bgoRandom;
    private final Catalogue catalogue;

    public MissionDistributor(Player player, Galaxy galaxy, BgoRandom bgoRandom)
    {
        this.player = player;
        this.galaxy = galaxy;
        this.bgoRandom = bgoRandom;
        this.catalogue = CDI.current().select(Catalogue.class).get();
    }

    /**
     * Updates the current player mission book
     * @return true if updated, false in any other case
     */
    public synchronized boolean updateMissionBook()
    {
        final MissionBook missionBook = player.getCounterFacade().missionBook();
        final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        final int lastTimeDayOfYear = missionBook.getLastTimeMissionsRequested().getDayOfYear();
        final int currentDayOfYear = now.getDayOfYear();
        final int lastYear = missionBook.getLastTimeMissionsRequested().getYear();
        final int nowYear = now.getYear();


        //check for same day last time missions fetched
        final boolean alreadyFetchedToday = lastTimeDayOfYear == currentDayOfYear;
        log.info("Already fetched today {} lastTimeDay {} currentDay {}", alreadyFetchedToday, lastTimeDayOfYear, currentDayOfYear);
        if (alreadyFetchedToday && lastYear == nowYear)
        {
            return false;
        }


        final Map<Integer, MissionTemplate> missionTemplates = MissionTemplates.getMissionTemplates(player.getFaction());
        var requiredMissionLst = missionTemplates.values().stream()
                .filter(missionTemplate -> missionBook.getByID(missionTemplate.id()).isEmpty())
                .toList();

        for (final MissionTemplate missionTemplate : requiredMissionLst)
        {
            final long guid = missionTemplate.missionGuid();
            final Optional<MissionCard> optionalMissionCard = catalogue.fetchCard(guid, CardView.Mission);
            if (optionalMissionCard.isEmpty())
            {
                log.warn("MissionCard to fetch mission guid {} was null!", guid);
                continue;
            }
            final MissionCard missionCard = optionalMissionCard.get();
            final boolean isLevelRequirements = missionCard.checkMinMaxLevel(player.getSkillBook().get());
            if (!isLevelRequirements)
            {
                log.info("player does not have level requirements for mission currentLvl: {}", player.getSkillBook().get());
                continue;
            }
            final long sectorGuid = getSectorGUIDBasedOnId(getSectorIdBasedOnTemplate(missionTemplate));
            final Map<Long, MissionCountable> missionCountableMap = new HashMap<>();

            Arrays.stream(missionTemplate.missionCountEntries())
                    .map(missionCountEntry -> new MissionCountable(missionCountEntry.guid(), 0, missionCountEntry.needCount()))
                    .forEach(missionCountable -> missionCountableMap.put(missionCountable.getCounterCardGuid(), missionCountable));

            final Mission mission = new Mission(
                    missionTemplate.id(),
                    guid,
                    sectorGuid,
                    missionCountableMap
            );
            missionBook.addItem(mission);
        }
        missionBook.setLastTimeMissionsRequested(now);
        return true;
    }

    private long getSectorGUIDBasedOnId(final long sectorId)
    {
        if (sectorId == 0)
            return 0;
        final GalaxyMapCard galaxyMapCard = galaxy.getGalaxyMapCard();
        final Optional<MapStarDesc> optStart = galaxyMapCard.getStar(sectorId);
        return optStart.map(MapStarDesc::getSectorGuid).orElse(0L);
    }
    private long getSectorIdBasedOnTemplate(final MissionTemplate missionTemplate)
    {
        final MissionSectorDesc missionSectorDesc = missionTemplate.missionSectorDesc();
        if (missionSectorDesc.isGlobal())
            return 0;

        //use static id
        if (!missionSectorDesc.useRandomSector() || missionSectorDesc.staticSectorId() != 0)
        {
            return missionSectorDesc.staticSectorId();
        }

        //use random sector --> filter first

        //get all available sectors
        final GalaxyMapCard galaxyMapCard = galaxy.getGalaxyMapCard();
        final Set<Long> sectorIds = galaxyMapCard.getStars().keySet();

        //filter blacklist
        final Set<Long> filteredBlacklist = sectorIds.stream()
                .filter(Predicate.not(missionSectorDesc::isOnBlacklist))
                .collect(Collectors.toSet());

        //filter whitelist
        final List<Long> filteredWhitelist = filteredBlacklist.stream()
                .filter(missionSectorDesc::isOnWhitelist)
                .toList();


        return filteredWhitelist.get(bgoRandom.nextInt(filteredWhitelist.size()));
    }

}
