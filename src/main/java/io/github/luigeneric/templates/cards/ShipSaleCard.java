package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

import java.util.List;

public class ShipSaleCard extends Card
{
    private final List<Long> rewardCardsColonial;
    private final List<Long> getRewardCardsCylon;

    public ShipSaleCard(long cardGuid, List<Long> rewardCardsColonial, List<Long> getRewardCardsCylon)
    {
        super(cardGuid, CardView.ShipSale);
        this.rewardCardsColonial = rewardCardsColonial;
        this.getRewardCardsCylon = getRewardCardsCylon;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32Collection(rewardCardsColonial);
        bw.writeUInt32Collection(getRewardCardsCylon);
    }
}
