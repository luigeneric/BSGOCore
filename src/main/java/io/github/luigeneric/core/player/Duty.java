package io.github.luigeneric.core.player;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.utils.collections.IServerItem;

public class Duty implements IProtocolWrite, IServerItem
{
    private int serverID;
    private final long guid;

    public Duty(final int serverID, final long guid)
    {
        this.serverID = serverID;
        this.guid = guid;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt16(this.serverID);
        bw.writeGUID(this.guid);
    }

    @Override
    public int getServerID()
    {
        return this.serverID;
    }

    @Override
    public void setServerID(int serverID)
    {
        this.serverID = serverID;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Duty duty = (Duty) o;

        return guid == duty.guid;
    }

    @Override
    public int hashCode()
    {
        return (int) (guid ^ (guid >>> 32));
    }
}
