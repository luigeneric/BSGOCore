package io.github.luigeneric.templates.cards;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class ModuleCard extends Card
{
    @SerializedName("ColonialPrefab")
    private final String colonialPrefabName;
    @SerializedName("CylonPrefab")
    private final String cylonPrefabName;

    public ModuleCard(long cardGuid, String colonialPrefabName, String cylonPrefabName)
    {
        super(cardGuid, CardView.Module);
        this.colonialPrefabName = colonialPrefabName;
        this.cylonPrefabName = cylonPrefabName;
    }
    //is ok too
    public ModuleCard(long cardGuid)
    {
        this(cardGuid, "", "");
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeString(colonialPrefabName);
        bw.writeString(cylonPrefabName);
    }
}
