package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

public class StickerListCard extends Card
{
    @SerializedName("StickersCylon")
    private final Sticker[] stickersCylon;
    @SerializedName("StickersColonial")
    private final Sticker[] stickersColonial;

    public StickerListCard(long cardGuid, CardView view, Sticker[] stickersCylon, Sticker[] stickersColonial)
    {
        super(cardGuid, view);
        this.stickersCylon = stickersCylon;
        this.stickersColonial = stickersColonial;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeDescArray(stickersColonial);
        bw.writeDescArray(stickersCylon);
    }

    public static class Sticker implements IProtocolWrite
    {
        @SerializedName("ID")
        private final int id;
        @SerializedName("Texture")
        private final String texture;

        public Sticker(int id, String texture)
        {
            this.id = id;
            this.texture = texture;
        }

        @Override
        public void write(BgoProtocolWriter bw)
        {
            bw.writeUInt16(id);
            bw.writeString(texture);
        }
    }
}
