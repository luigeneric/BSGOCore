package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.templates.utils.TournamentType;

import java.util.List;

public class TournamentCard extends Card
{
    private final TournamentType type;
    private final byte minLevel;
    private final List<Long> allowedTiers;

    public TournamentCard(long cardGuid, TournamentType type, byte minLevel, List<Long> allowedTiers)
    {
        super(cardGuid, CardView.Tournament);
        this.type = type;
        this.minLevel = minLevel;
        this.allowedTiers = allowedTiers;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeByte(type.value);
        bw.writeByte(minLevel);
        bw.writeUInt32Collection(allowedTiers);
    }
}
