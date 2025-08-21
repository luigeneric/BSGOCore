package io.github.luigeneric.core.protocols.zone;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

enum KillSpamFlags implements IProtocolWrite
{
    Environment(0),
    Normal(1),
    Nemesis(2),
    Revenge(4),
    KillSpree(8),
    SpreeKiller(16),
    TopGunKill(32),
    ContributionKill(64);

    private final byte value;

    KillSpamFlags(int value)
    {
        this.value = (byte) value;
    }

    public byte getValue()
    {
        return value;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte(this.value);
    }
}
