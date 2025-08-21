package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class SpecialOfferCard extends Card
{
    private final String jsonKey;
    private final String offerIconPath;
    private final String offerImagePathColonial;
    private final String getOfferImagePathCylon;
    private final long itemGroup;

    public SpecialOfferCard(long cardGuid, String jsonKey, String offerIconPath, String offerImagePathColonial, String getOfferImagePathCylon, long itemGroup)
    {
        super(cardGuid, CardView.ConversionCampaign);
        this.jsonKey = jsonKey;
        this.offerIconPath = offerIconPath;
        this.offerImagePathColonial = offerImagePathColonial;
        this.getOfferImagePathCylon = getOfferImagePathCylon;
        this.itemGroup = itemGroup;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeString(jsonKey);
        bw.writeString(offerIconPath);
        bw.writeString(offerImagePathColonial);
        bw.writeString(getOfferImagePathCylon);
        bw.writeUInt32(itemGroup);
    }
}
