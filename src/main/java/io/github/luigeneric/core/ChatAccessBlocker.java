package io.github.luigeneric.core;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ChatAccessBlocker
{
    private final Map<Long, LocalDateTime> banBlockers;


    public ChatAccessBlocker()
    {
        this.banBlockers = new ConcurrentHashMap<>();
    }

    public void addUser(final long userId, final LocalDateTime endChatBlock)
    {
        this.banBlockers.put(userId, endChatBlock);
    }
    public void addUser(final long userid, final long duration, final TimeUnit timeUnit)
    {
        final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        final LocalDateTime endDate = now.plusNanos(timeUnit.toNanos(duration));
        this.addUser(userid, endDate);
    }

    public boolean checkUserCanAccessChat(final long userId)
    {
        final LocalDateTime endDate = this.banBlockers.get(userId);
        if (endDate == null)
            return true;

        final boolean currentIsAfterEndDate = LocalDateTime.now(Clock.systemUTC()).isAfter(endDate);
        if (currentIsAfterEndDate)
        {
            banBlockers.remove(userId);
        }

        return currentIsAfterEndDate;
    }
}

