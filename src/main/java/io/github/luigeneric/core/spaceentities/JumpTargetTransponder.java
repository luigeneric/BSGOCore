package io.github.luigeneric.core.spaceentities;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.WorldCard;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class JumpTargetTransponder extends SpaceObject
{
    private final LocalDateTime timeWhenActive;
    private final LocalDateTime timeWhenInactive;

    public JumpTargetTransponder(long objectID, OwnerCard ownerCard, WorldCard worldCard, Faction faction, FactionGroup factionGroup, SpaceSubscribeInfo spaceSubscribeInfo, LocalDateTime timeWhenActive, LocalDateTime timeWhenInactive)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.JumpTargetTransponder, faction, factionGroup, spaceSubscribeInfo);
        this.timeWhenActive = timeWhenActive;
        this.timeWhenInactive = timeWhenInactive;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeDateTime(timeWhenActive);
        bw.writeDateTime(timeWhenInactive);
    }
}
