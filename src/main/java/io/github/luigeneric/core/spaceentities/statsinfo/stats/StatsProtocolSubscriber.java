package io.github.luigeneric.core.spaceentities.statsinfo.stats;


import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.spaceentities.statsinfo.buffer.BasePropertyBuffer;

public interface StatsProtocolSubscriber
{
    ProtocolID getProtocolID();

    /**
     * Sends the basepropertybuffer to send given subscriber
     *
     * @param spacePropertyBuffer
     * @return true if the send call was successfully
     */
    boolean sendSpacePropertyBuffer(final BasePropertyBuffer spacePropertyBuffer);
    long userId();
}
