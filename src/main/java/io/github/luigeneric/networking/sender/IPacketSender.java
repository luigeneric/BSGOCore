package io.github.luigeneric.networking.sender;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

import java.util.Collection;

public interface IPacketSender
{
    /**
     * Sends the buffer to the client socket
     * @param bw the full byte buffer to send
     * @return true if send operation was fine, false if send failed or the sender got shutdown
     */
    public boolean send(final BgoProtocolWriter bw);

    /**
     * Sends a collection of n buffers
     * @param bws protocol buffers
     * @return false if one of the send operations is false, this will drop the whole send operation and stop sending the remaining buffers
     */
    public boolean send(final Collection<BgoProtocolWriter> bws);

    /**
     * Sets a flag to shutdown the send process
     */
    public void shutdown();
}
