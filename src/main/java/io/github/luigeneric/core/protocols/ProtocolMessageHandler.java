package io.github.luigeneric.core.protocols;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;

import java.io.IOException;

public interface ProtocolMessageHandler
{
    void handle(BgoProtocolReader br) throws IOException;
}
