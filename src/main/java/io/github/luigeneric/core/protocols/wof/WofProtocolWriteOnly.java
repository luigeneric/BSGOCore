package io.github.luigeneric.core.protocols.wof;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipItem;

import java.util.List;

public class WofProtocolWriteOnly extends WriteOnlyProtocol
{
    public WofProtocolWriteOnly()
    {
        super(ProtocolID.Wof);
    }


    /**
     *
     * @param jackpotType Item or MapPart?
     * @param jackpotItemCardGUID only for GUICard! May be ANYTHING
     * @param amount how much of the jackpotitem
     * @param isFreeWofGame free to use
     * @param costListPerStep costs for each step!
     * @return a new writer object
     */
    public BgoProtocolWriter writeInit(final JackpotType jackpotType, final long jackpotItemCardGUID, final long amount,
                                       final boolean isFreeWofGame, final List<Integer> costListPerStep
    )
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(WofProtocolServerMessage.ReplyInit.shortValue);
        bw.writeDesc(jackpotType);
        bw.writeGUID(jackpotItemCardGUID);
        bw.writeInt32((int) amount); //how much

        bw.writeBoolean(isFreeWofGame);

        bw.writeLength(costListPerStep.size());
        for(final int cost : costListPerStep)
        {
            bw.writeInt32(cost);
        }

        return bw;
    }

    /**
     * Wofdraw Reply from draw!
     * @param jackpotType jackpottype, item or mappart
     * @param itemsDraw what items to receive
     * @param jackpotItem the jackpotitem that can be received
     * @param wasFreeGame boolean value
     * @return new writer
     */
    public BgoProtocolWriter writeWofDrawReply(final JackpotType jackpotType, final List<ShipItem> itemsDraw,
                                               final ShipItem jackpotItem, final boolean wasFreeGame)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(WofProtocolServerMessage.ReplyDraw.shortValue);

        bw.writeUInt16(jackpotType.shortValue);

        //jackpotItem
        bw.writeGUID(jackpotItem.getCardGuid());
        //jackpot amount
        if (jackpotItem instanceof ItemCountable jackpotCountable)
        {
            bw.writeInt32((int) jackpotCountable.getCount());
        }
        else
        {
            bw.writeInt32(1);
        }
        //freeGame boolean
        bw.writeBoolean(wasFreeGame);

        //now the items won (WofRewardItemContainer)
        int toWrite = itemsDraw.size();
        bw.writeLength(toWrite);
        for (ShipItem item : itemsDraw)
        {
            //is jackpot Item?
            bw.writeBoolean(item.getCardGuid() == jackpotItem.getCardGuid());
            bw.writeGUID(item.getCardGuid());
            if (item instanceof ItemCountable itemCountable)
            {
                bw.writeInt32((int) itemCountable.getCount());
            }
            else bw.writeInt32(1);
        }

        //WofBonusMapParts
        bw.writeLength(0); //tmp just send no items

        return bw;
    }


    /**
     * Write all visible map indexes
     * @param visibleMapsIds indexes of visible maps
     *                       1 = Alpha
     *                       2 = Beta
     *                       3 = Gamma
     *                       4 = Delta
     *                       <p>
     *                       All "old" = 1-3
     *                       All = 1-4
     *                       </p>
     * @apiNote SetVisibleBonusLevel inside Client
     * @return a new ProtocolWriter
     */
    public BgoProtocolWriter writeReplyVisibleMaps(final List<Integer> visibleMapsIds)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(WofProtocolServerMessage.ReplyVisibleMaps.shortValue);
        bw.writeInt32Collection(visibleMapsIds);
        return bw;
    }
    public BgoProtocolWriter writeAllVisibleMaps()
    {
        return writeReplyVisibleMaps(List.of(1, 2, 3, 4));
    }
}
