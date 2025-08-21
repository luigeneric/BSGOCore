package io.github.luigeneric.core.protocols.story;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.HelpScreenType;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;
import io.github.luigeneric.templates.cards.BannerCard;

public class StoryProtocolWriteOnly extends WriteOnlyProtocol
{
    public StoryProtocolWriteOnly()
    {
        super(ProtocolID.Story);
    }

    public BgoProtocolWriter writeBannerBox(final BannerCard bannerCard)
    {
        return writeBannerBox(bannerCard.getCardGuid());
    }
    /**
     * Write BannerCardGUID to buffer, show banner in client
     * @param bannerGuid BannerCard guid
     * @return buffer
     */
    public BgoProtocolWriter writeBannerBox(final long bannerGuid)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.BannerBox.shortValue);
        bw.writeGUID(bannerGuid);

        return bw;
    }

    public BgoProtocolWriter write(final boolean showOk, final long guiCard, final String mainText, final String advice, final String imagePath)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.MessageBox.shortValue);

        bw.writeBoolean(showOk);
        bw.writeGUID(guiCard);
        bw.writeString(mainText);
        bw.writeString(advice);
        bw.writeString(imagePath);

        return bw;
    }

    public BgoProtocolWriter writeHelpBox(final HelpScreenType helpScreenType)
    {
        return this.writeHelpBox(helpScreenType.intValue);
    }
    public BgoProtocolWriter writeHelpBox(final long id)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.HelpBox.shortValue);
        bw.writeUInt32(id);
        return bw;
    }

    public BgoProtocolWriter writeMissionLog(final String objectiveTextKey)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.MissionLog.shortValue);
        bw.writeString(objectiveTextKey);
        bw.writeBoolean(false); //not used anymore

        return bw;
    }

    public BgoProtocolWriter writeSelectTarget(final long objectID)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.SelectTarget.shortValue);
        bw.writeUInt32(objectID);

        return bw;
    }

    /**
     * Seems like its not working
     * @param objectID obj id to highlight
     * @param isHighlighted if highlight on or off
     * @return buffer
     */
    public BgoProtocolWriter writeHighlightObject(final long objectID, final boolean isHighlighted)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.HighlightObject.shortValue);
        bw.writeUInt32(objectID);
        bw.writeBoolean(isHighlighted);
        return bw;
    }



    public BgoProtocolWriter writePlayCutscene(final String str)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.PlayCutscene.shortValue);
        bw.writeString(str);
        return bw;
    }


}
