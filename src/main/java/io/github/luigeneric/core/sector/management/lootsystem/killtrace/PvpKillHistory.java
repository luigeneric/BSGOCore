package io.github.luigeneric.core.sector.management.lootsystem.killtrace;

import io.github.luigeneric.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
@Slf4j
public class PvpKillHistory
{
    private final Map<KillerObject, List<KilledObject>> killerMap;
    private final Lock lock;
    private final Duration KillLogLifetime = Duration.ofMinutes(30);
    private final int killThreshold = 5;

    public PvpKillHistory()
    {
        this(new HashMap<>(), new ReentrantLock());
    }


    public void addPvpKilled(final long killerId, String killerName, final long killedId, final String killedName, final LocalDateTime timeOfKillUtc)
    {
        lock.lock();
        try
        {
            final KillerObject killerObject = new KillerObject(killerId, killerName);

            final List<KilledObject> killerList = this.killerMap.getOrDefault(killerObject, new ArrayList<>());
            cleanupOldKills(killerList); // Entfernt Kills, die älter als 1 Stunde sind
            killerList.add(new KilledObject(killedId, killedName, timeOfKillUtc));
            this.killerMap.put(killerObject, killerList);

            if (checkKillThreshold(killerList))
            {
                final String killReportJson = Utils.getGson().toJson(new KillPushReport(killerId, killerName, killerList));
                log.warn("Cheat killpush detection with {}", killReportJson);
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    public void addPvpKilled(final long killerId, final String killerName, final long killedId, final String killedName)
    {
        this.addPvpKilled(killerId, killerName, killedId, killedName, LocalDateTime.now(Clock.systemUTC()));
    }

    private boolean checkKillThreshold(final List<KilledObject> kills)
    {
        final Map<Long, Integer> killCounts = new HashMap<>();
        for (KilledObject kill : kills)
        {
            killCounts.put(kill.playerId(), killCounts.getOrDefault(kill.playerId(), 0) + 1);
            if (killCounts.get(kill.playerId()) >= killThreshold)
            {
                return true;
            }
        }
        return false;
    }

    public List<KilledObject> getKilledObjectsOfId(final long killerId)
    {
        final List<KilledObject> killedObjects = this.killerMap.get(killerId);
        if (killedObjects == null)
            return List.of();

        return Collections.unmodifiableList(killedObjects);
    }

    protected void cleanupOldKills(final List<KilledObject> kills)
    {
        kills.removeIf(kill -> Duration.between(kill.localDateTime(), LocalDateTime.now(Clock.systemUTC())).compareTo(KillLogLifetime) > 0);
    }

    private String formatKillsToJson(final List<KilledObject> kills)
    {
        return kills.toString();
    }
}

