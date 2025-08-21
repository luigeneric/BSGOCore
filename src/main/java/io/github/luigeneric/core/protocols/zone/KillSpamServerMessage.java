package io.github.luigeneric.core.protocols.zone;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

/**
 * THIS INTERNAL NAMING BIGPOINT AAHHHHHHHHHHHHH
 *
 * This is who killed who in tournament view
 * so a keep track of everything window on the right side
 *
 * @param flags revente, killingspree, contribution and so on
 * @param aggressorId almost always used(the primary player)
 * @param deadPlayerId sometimes used for killed by or used when player got killed
 * @param score will be pruned to int on client side
 */
public record KillSpamServerMessage(
        KillSpamFlags flags,
        long aggressorId,
        long deadPlayerId,
        float score
) implements IProtocolWrite
{
    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw
                .writeDesc(flags)
                .writeUInt32(aggressorId)
                .writeUInt32(deadPlayerId)
                .writeSingle(score);
    }
}
