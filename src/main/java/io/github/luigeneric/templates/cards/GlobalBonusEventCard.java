package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class GlobalBonusEventCard extends Card
{
    private final long bannerCardColonial;
    private final long bannerCardCylon;

    public GlobalBonusEventCard(long cardGuid, long bannerCardColonial, long bannerCardCylon)
    {
        super(cardGuid, CardView.GlobalBonusEvent);
        this.bannerCardColonial = bannerCardColonial;
        this.bannerCardCylon = bannerCardCylon;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeBoolean(false);
        bw.writeUInt32(bannerCardColonial);
        bw.writeUInt32(bannerCardCylon);
    }
}
