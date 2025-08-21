package io.github.luigeneric.core.player.container;


import io.github.luigeneric.binaryreaderwriter.IProtocolRead;

public abstract class IContainerID implements IProtocolRead
{
    private final ContainerType containerType;

    public IContainerID(ContainerType containerType)
    {
        this.containerType = containerType;
    }

    public ContainerType getContainerType()
    {
        return this.containerType;
    }
}
