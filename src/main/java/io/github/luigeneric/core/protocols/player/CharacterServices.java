package io.github.luigeneric.core.protocols.player;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

import java.util.List;

public record CharacterServices(long currentSectorId, boolean isNameChangeAllowed, long cooldownFactioNSwitch,
                                long cooldownNameChange, long lastUseFactionSwitch, long lastUseNameChange,
                                float cubitsPriceFaction, float cubitsPriceName,
                                List<MinLevelMaxLevelFactionSwitch> minLevelMaxLevelFactionSwitches) implements IProtocolWrite
{

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeByte((byte) currentSectorId);
        bw.writeBoolean(isNameChangeAllowed);
        bw.writeInt64(0);
        bw.writeInt64(cooldownFactioNSwitch);
        bw.writeInt64(cooldownNameChange);
        bw.writeInt64(lastUseFactionSwitch);
        bw.writeInt64(lastUseNameChange);
        bw.writeSingle(cubitsPriceFaction);
        bw.writeSingle(cubitsPriceName);
        bw.writeDescCollection(minLevelMaxLevelFactionSwitches);
    }
}
