package io.github.luigeneric.core.sector.removeme;


import io.github.luigeneric.enums.RemovingCause;

public class DisconnectedRemoveMeDescription extends RemoveMeDescription
{

    public DisconnectedRemoveMeDescription()
    {
        super(RemovingCause.Disconnection);
    }
}
