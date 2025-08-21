package io.github.luigeneric.templates.cards;


import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public class Card implements IProtocolWrite
{
    @SerializedName("cardGUID")
    protected final long cardGuid;
    @SerializedName("cardView2")
    protected final CardView cardView;
    public Card(final long cardGuid, final CardView view)
    {
        this.cardGuid = cardGuid;
        this.cardView = view;
    }
    public long getCardGuid()
    {
        return this.cardGuid;
    }

    public CardView getCardView(){return this.cardView;}

    public void write(BgoProtocolWriter bw)
    {
        bw.writeUInt32(this.cardGuid);
        bw.writeUInt16(this.cardView.getValue());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (cardGuid != card.cardGuid) return false;
        return cardView == card.cardView;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (cardGuid ^ (cardGuid >>> 32));
        result = 31 * result + cardView.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Card{" +
                "cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }
}
