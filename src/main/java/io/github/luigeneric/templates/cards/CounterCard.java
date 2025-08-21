package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class CounterCard extends Card
{
    @SerializedName("Name")
    private final String name;
    public CounterCard(long cardGuid, String name)
    {
        super(cardGuid, CardView.Counter);
        this.name = name;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeString(name);
    }

    public String getName()
    {
        return name;
    }
}
