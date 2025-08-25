package io.github.luigeneric.core.protocols;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.templates.cards.Card;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.ShipCard;
import io.github.luigeneric.templates.cards.ShipListCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class CatalogueProtocol extends BgoProtocol
{
    private final Catalogue catalogue;
    public CatalogueProtocol(ProtocolContext ctx)
    {
        super(ProtocolID.Catalogue, ctx);
        this.catalogue = CDI.current().select(Catalogue.class).get();
        final Optional<ShipListCard> optShipLstColo = catalogue.fetchCard(StaticCardGUID.ShipListCardColonial.getValue(), CardView.ShipList);
        final Optional<ShipListCard> optShipLstCylo = catalogue.fetchCard(StaticCardGUID.ShipListCardCylon.getValue(), CardView.ShipList);
        if (optShipLstColo.isEmpty() || optShipLstCylo.isEmpty())
        {
            throw new NullPointerException("shiplst cards are null!");
        }
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        if (msgType != 1)
        {
            log.error(user().getUserLog() + "wrong msgtype received in CatalogueProtocol: " + msgType);
            return;
        }

        final int num = br.readUint16();
        for(int i = 0; i < num; i++)
        {
            final long guid = br.readUint32(); //GUID
            final int rawView = br.readUint16(); //VIEW
            if (rawView == 0)
            {
                log.warn("Could not get correct CardView is 0");
            }
            final CardView view = CardView.valueOf(rawView);
            if (view == null)
            {
                throw new IOException("CardView was null, therefore kill the connection! Value: " + rawView);
            }

            BgoProtocolWriter bw = catalogue.getProtocolWriter(guid, view);
            boolean userSendFlag = true;
            //there is no protocolbuffer in cache -> create new buffer
            if (bw == null)
            {
                final Optional<Card> optCard = fetchCard(rawView, guid);
                if (optCard.isPresent())
                {
                    final Card card = optCard.get();
                    bw = writeCard(card);
                    catalogue.putProtocolWriter(guid, CardView.valueOf(rawView), bw);
                }
                else
                {
                    //Log.errorIn("Card should not be send because it's null! " + guid + " " + rawView);
                    userSendFlag = false;
                }
            }
            if (userSendFlag && user() != null)
                this.user().send(bw);
        }
    }


    public Optional<Card> fetchCard(final int cardView, final long cardGUID)
    {
        final Optional<Card> fetchedCard = catalogue.fetchCard(cardGUID, CardView.valueOf(cardView));
        if (fetchedCard.isEmpty())
            return fetchedCard;
        if (fetchedCard.get() instanceof ShipCard shipCard)
        {
            return shipCardFilter(shipCard);
        }
        return fetchedCard;
    }

    private Optional<Card> shipCardFilter(final ShipCard shipCard)
    {
        if (shipCard.getHangarId() != -1)
        {
            return Optional.of(shipCard);
        }
        return Optional.empty();
    }


    public BgoProtocolWriter writeCard(final Card card)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeUInt16(2);
        bw.writeDesc(card);

        return bw;
    }
}
