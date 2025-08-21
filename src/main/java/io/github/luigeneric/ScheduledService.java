package io.github.luigeneric;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ScheduledService
{
    private ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    void postCtor()
    {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    public ScheduledFuture<?> schedule(final Runnable runnable, final long delay, final TimeUnit timeUnit)
    {
        return scheduledExecutorService.schedule(runnable, delay, timeUnit);
    }
}
