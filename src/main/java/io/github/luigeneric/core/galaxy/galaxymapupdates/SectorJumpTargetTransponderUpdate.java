package io.github.luigeneric.core.galaxy.galaxymapupdates;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.protocols.universe.GalaxyUpdateType;
import io.github.luigeneric.enums.Faction;

public class SectorJumpTargetTransponderUpdate extends GalaxyMapUpdate
{
    private final long sectorID;
    private final JumpTargetTransponderDesc[] jumpTargetTransponderDescs;

    public SectorJumpTargetTransponderUpdate(Faction faction, long sectorID, JumpTargetTransponderDesc[] jumpTargetTransponderDescs)
    {
        super(GalaxyUpdateType.SectorJumpTargetTransponders, faction);
        this.sectorID = sectorID;
        this.jumpTargetTransponderDescs = jumpTargetTransponderDescs;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.sectorID);
        bw.writeByte(this.faction.value);
        bw.writeDescArray(jumpTargetTransponderDescs);
    }

    public static class JumpTargetTransponderDesc implements IProtocolWrite
    {
        private final long playerID;
        private final long partyID;
        private final long spaceID;
        private final long expires;

        public JumpTargetTransponderDesc(long playerID, long partyID, long spaceID, long expires)
        {
            this.playerID = playerID;
            this.partyID = partyID;
            this.spaceID = spaceID;
            this.expires = expires;
        }

        @Override
        public void write(BgoProtocolWriter bw)
        {
            bw.writeUInt32(this.playerID);
            bw.writeUInt32(this.partyID);
            bw.writeUInt32(this.spaceID);
            bw.writeUInt32(this.expires);
        }
    }
}
