package io.github.luigeneric.templates.cards;


import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AvatarCatalogueCard extends Card
{
    @SerializedName("avatarIndexes")
    private final AvatarIndex[] avatarIndexes;

    public AvatarCatalogueCard(long cardGuid, AvatarIndex[] avatarIndexes)
    {
        super(cardGuid, CardView.AvatarCatalogue);
        this.avatarIndexes = avatarIndexes;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeDescArray(avatarIndexes);
    }

    @Override
    public String toString()
    {
        return "AvatarCatalogueCard{" +
                "avatarIndexes=" + Arrays.toString(avatarIndexes) +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public static class AvatarIndex implements IProtocolWrite
    {
        @SerializedName("race")
        private final String race;
        @SerializedName("sex")
        private final String sex;

        @SerializedName("items")
        private final Map<String, List<String>> items;
        @SerializedName("textures")
        private final Map<String, List<String>> textures;
        @SerializedName("materials")
        private final Map<String, Map<String, List<String>>> materials;


        public AvatarIndex(String race, String sex, Map<String, List<String>> items, Map<String, List<String>> textures,
                           Map<String, Map<String, List<String>>> materials)
        {
            this.race = race;
            this.sex = sex;
            this.items = items;
            this.textures = textures;
            this.materials = materials;
        }

        @Override
        public String toString()
        {
            return "AvatarIndex{" +
                    "race='" + race + '\'' +
                    ", sex='" + sex + '\'' +
                    ", items=" + items +
                    ", textures=" + textures +
                    ", materials=" + materials +
                    '}';
        }

        @Override
        public void write(BgoProtocolWriter bw)
        {
            bw.writeString(sex);
            bw.writeString(race);

            bw.writeUInt16(items.size());
            for(Map.Entry<String, List<String>> pair : items.entrySet())
            {
                bw.writeString(pair.getKey());
                int pairCount = pair.getValue().size();
                bw.writeLength(pairCount);
                for (int i = 0; i < pairCount; i++)
                {
                    bw.writeString(pair.getValue().get(i));
                }
            }

            bw.writeUInt16(materials.size());
            for(Map.Entry<String, Map<String, List<String>>> pair : materials.entrySet())
            {
                bw.writeString(pair.getKey());
                int pairValueSize = pair.getValue().size();
                bw.writeLength(pairValueSize);

                for(Map.Entry<String, List<String>> pair2 : pair.getValue().entrySet())
                {
                    bw.writeString(pair2.getKey());
                    int pair2Count = pair2.getValue().size();
                    bw.writeLength(pair2Count);
                    for(int i = 0; i < pair2Count; i++)
                    {
                        bw.writeString(pair2.getValue().get(i));
                    }
                }
            }

            bw.writeLength(textures.size());
            for(Map.Entry<String, List<String>> pair3 : textures.entrySet())
            {
                bw.writeString(pair3.getKey());
                int pairCount = pair3.getValue().size();
                bw.writeLength(pairCount);
                for (int i = 0; i < pairCount; i++)
                {
                    bw.writeString(pair3.getValue().get(i));
                }
            }

        }
    }


}
