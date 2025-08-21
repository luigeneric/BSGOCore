package io.github.luigeneric.templates.cards;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

import java.util.List;

public class EventShopCard extends Card
{
    private final String shopNameCylon;
    private final String shopNameColonial;
    private final String shopDescriptionCylon;
    private final String shopDescriptionColonial;
    private final String shopErrorMissingRessources;
    private final String shopErrorCannotBuy;
    private final List<Long> eventRessources;

    public EventShopCard(long cardGuid, String shopNameCylon, String shopNameColonial, String shopDescriptionCylon,
                         String shopDescriptionColonial, String shopErrorMissingRessources, String shopErrorCannotBuy,
                         List<Long> eventRessources)
    {
        super(cardGuid, CardView.EventShop);
        this.shopNameCylon = shopNameCylon;
        this.shopNameColonial = shopNameColonial;
        this.shopDescriptionCylon = shopDescriptionCylon;
        this.shopDescriptionColonial = shopDescriptionColonial;
        this.shopErrorMissingRessources = shopErrorMissingRessources;
        this.shopErrorCannotBuy = shopErrorCannotBuy;
        this.eventRessources = eventRessources;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeString(shopNameCylon);
        bw.writeString(shopNameColonial);
        bw.writeString(shopDescriptionCylon);
        bw.writeString(shopDescriptionColonial);
        bw.writeString(shopErrorMissingRessources);
        bw.writeString(shopErrorCannotBuy);
        bw.writeUInt32Collection(eventRessources);
    }
}
