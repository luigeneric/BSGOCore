package io.github.luigeneric.core;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public interface IConnection
{
    boolean send(final BgoProtocolWriter bw);
    BgoProtocolReader recvNextMessage();
    boolean isClosed();
    void closeConnection(final String reason);
}
