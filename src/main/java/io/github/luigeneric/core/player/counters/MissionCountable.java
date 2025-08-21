package io.github.luigeneric.core.player.counters;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.utils.ICopy;

public class MissionCountable implements IProtocolWrite, ICopy<MissionCountable>
{
    private final long counterCardGuid;
    private long currentCount;
    private final long needCount;

    public MissionCountable(final long counterCardGuid, final long currentCount, final long needCount)
    {
        this.counterCardGuid = counterCardGuid;
        this.currentCount = currentCount;
        this.needCount = needCount;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeGUID(this.counterCardGuid);
        bw.writeInt32((int) this.currentCount);
        bw.writeInt32((int) this.needCount);
    }

    protected void setCount(final long current)
    {
        this.currentCount = current;
    }

    public long getCounterCardGuid()
    {
        return counterCardGuid;
    }

    public long getCurrentCount()
    {
        return currentCount;
    }

    public long getNeedCount()
    {
        return needCount;
    }

    @Override
    public MissionCountable copy()
    {
        return new MissionCountable(this.counterCardGuid, this.currentCount, this.needCount);
    }
}
