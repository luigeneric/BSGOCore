package io.github.luigeneric.core.player.location;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.enums.TransSceneType;

public class StarterLocation extends LocationState
{
    private final long colonialBonusGUID;
    private final long cylonBonusGUID;


    public StarterLocation(final Location location, long colonialBonusGUID, long cylonBonusGUID)
    {
        super(location, GameLocation.Starter, TransSceneType.Teaser);
        this.colonialBonusGUID = colonialBonusGUID;
        this.cylonBonusGUID = cylonBonusGUID;
    }
    public StarterLocation(final Location location)
    {
        this(location, StaticCardGUID.NeutralRewardCard.getValue(), StaticCardGUID.NeutralRewardCard.getValue());
    }

    @Override
    public void process(BgoProtocolWriter bw)
    {
        this.write(bw);
    }


    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);

        bw.writeGUID(colonialBonusGUID);
        bw.writeGUID(cylonBonusGUID);
    }
}
