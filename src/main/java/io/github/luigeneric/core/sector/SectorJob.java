package io.github.luigeneric.core.sector;

public interface SectorJob
{
    default long runJobWithTime()
    {
        var start = System.currentTimeMillis();
        run();
        return System.currentTimeMillis() - start;
    }
    void run();
}
