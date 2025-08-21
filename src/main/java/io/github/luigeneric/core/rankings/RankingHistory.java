package io.github.luigeneric.core.rankings;

import io.github.luigeneric.binaryreaderwriter.BgoTimeStamp;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.core.database.CounterRecord;
import io.github.luigeneric.core.database.DbProvider;
import io.github.luigeneric.core.protocols.ranking.RankDescription;
import io.github.luigeneric.enums.RankingGroup;
import io.github.luigeneric.enums.RankingType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class RankingHistory implements Runnable
{
    public static final long PAGE_SIZE = 17;
    private final UsersContainer usersContainer;
    private final DbProvider dbProvider;
    private final TimeUnit timeUnit;
    private final long timeUntilReroll;
    /**
     * Regular/Delta -> Group(Asteroid or whatever) -> page
     */
    private final Map<RankingType, Map<RankingGroup, Map<Long, RankDescription>>> rankDescriptions;
    private Map<Long, CounterRecord> lastCounterFetch;
    private BgoTimeStamp lastTimeFetched;
    private final ReadWriteLock readWriteLock;

    public RankingHistory(final UsersContainer usersContainer,
                          final DbProvider dbProvider,
                          final TimeUnit timeUnit,
                          final long timeUntilReroll)
    {
        this.usersContainer = usersContainer;
        this.dbProvider = dbProvider;
        this.timeUnit = timeUnit;
        this.timeUntilReroll = timeUntilReroll;
        this.rankDescriptions = new HashMap<>();
        this.readWriteLock = new ReentrantReadWriteLock();
    }


    @Override
    public void run()
    {
        readWriteLock.writeLock().lock();
        try
        {
            fetchCounters();
            setupRankings();
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    private void setupRankings()
    {

    }

    private void fetchCounters()
    {
        this.lastCounterFetch = dbProvider.fetchAllCounters();
        this.lastTimeFetched = BgoTimeStamp.now();
    }

    public Optional<BgoTimeStamp> getLastTimeFetched()
    {
        return Optional.ofNullable(lastTimeFetched);
    }

    public TimeUnit getTimeUnit()
    {
        return timeUnit;
    }

    public long getTimeUntilReroll()
    {
        return timeUntilReroll;
    }
}
