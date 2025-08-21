package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class BannerCard extends Card
{
    private final String artworkPath;
    private final String description;
    private final String footerText;
    private final String title;
    public BannerCard(long cardGuid, String artworkPath, String description, String footerText, String title)
    {
        super(cardGuid, CardView.Banner);
        this.artworkPath = artworkPath;
        this.description = description;
        this.footerText = footerText;
        this.title = title;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeString(artworkPath);
        bw.writeString(description);
        bw.writeString(footerText);
        bw.writeString(title);
    }
}
