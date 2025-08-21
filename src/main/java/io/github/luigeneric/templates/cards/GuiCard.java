package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

import java.util.Arrays;

public class GuiCard extends Card
{
    private String key;
    public short level;
    private String guiAtlasTexturePath = "";
    private int frameIndex;
    private String guiIcon = "";
    private String guiAvatarSlotTexturePath = "";
    private String guiTexturePath = "";
    private String[] args;


    public GuiCard(long cardGUID, String key, short level, String guiAtlasTexturePath,
                   int frameIndex, String guiIcon, String guiAvatarSlotTexturePath, String guiTexturePath, String[] args)
    {
        super(cardGUID, CardView.GUI);
        this.key = key;
        this.level = level;
        this.guiAtlasTexturePath = guiAtlasTexturePath;
        this.frameIndex = frameIndex;
        this.guiIcon = guiIcon;
        this.guiAvatarSlotTexturePath = guiAvatarSlotTexturePath;
        this.guiTexturePath = guiTexturePath;
        this.args = args;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeString(key);
        bw.writeByte((byte)level);
        bw.writeString(guiAtlasTexturePath);
        bw.writeUInt16(frameIndex);
        bw.writeString(guiIcon);
        bw.writeString(guiAvatarSlotTexturePath);
        bw.writeString(guiTexturePath);
        bw.writeStringArray(args);
    }

    @Override
    public String toString()
    {
        return "GuiCard{" +
                "key='" + key + '\'' +
                ", level=" + level +
                ", guiAtlasTexturePath='" + guiAtlasTexturePath + '\'' +
                ", frameIndex=" + frameIndex +
                ", guiIcon='" + guiIcon + '\'' +
                ", guiAvatarSlotTexturePath='" + guiAvatarSlotTexturePath + '\'' +
                ", guiTexturePath='" + guiTexturePath + '\'' +
                ", args=" + Arrays.toString(args) +
                ", cardGuid=" + cardGuid +
                ", cardView=" + cardView +
                '}';
    }

    public String getKey()
    {
        return key;
    }

    public short getLevel()
    {
        return level;
    }

    public String getGuiAtlasTexturePath()
    {
        return guiAtlasTexturePath;
    }

    public int getFrameIndex()
    {
        return frameIndex;
    }

    public String getGuiIcon()
    {
        return guiIcon;
    }

    public String getGuiAvatarSlotTexturePath()
    {
        return guiAvatarSlotTexturePath;
    }

    public String getGuiTexturePath()
    {
        return guiTexturePath;
    }

    public String[] getArgs()
    {
        return args;
    }
}
