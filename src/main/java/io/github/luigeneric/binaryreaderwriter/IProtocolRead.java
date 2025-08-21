package io.github.luigeneric.binaryreaderwriter;

import java.io.IOException;

public interface IProtocolRead
{
    void read(final BgoProtocolReader br) throws IOException;
}
