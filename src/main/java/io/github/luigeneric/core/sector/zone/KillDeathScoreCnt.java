package io.github.luigeneric.core.sector.zone;

import lombok.Getter;

@Getter
public class KillDeathScoreCnt
{
    private long killCount;
    private long deathCount;
    private float score;

    public void incrementKill(final long byDelta)
    {
        this.killCount += byDelta;
    }
    public void incrementDeath(final long byDelta)
    {
        this.deathCount += byDelta;
    }
    public void increaseScore(final float delta)
    {
        this.score += delta;
    }
}
