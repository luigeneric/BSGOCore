package io.github.luigeneric.templates.cards;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;

public class MailTemplateCard extends Card
{
    private final long senderColonialGuiCardGuid;
    private final long senderCylonGuiCardGuid;
    private final int expire; //in original its read from uint16 so int should be enough..
    private final String bodyColonial;
    private final String bodyCylon;
    private final int mailLimit;
    private final MailType type;
    private final long bonusGuid;

    public MailTemplateCard(long cardGuid, long senderColonialGuiCardGuid, long senderCylonGuiCardGuid, int expire, String bodyColonial,
                            String bodyCylon, int mailLimit, MailType type, long bonusGuid)
    {
        super(cardGuid, CardView.MailTemplate);
        this.senderColonialGuiCardGuid = senderColonialGuiCardGuid;
        this.senderCylonGuiCardGuid = senderCylonGuiCardGuid;
        this.expire = expire;
        this.bodyColonial = bodyColonial;
        this.bodyCylon = bodyCylon;
        this.mailLimit = mailLimit;
        this.type = type;
        this.bonusGuid = bonusGuid;
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeGUID(senderColonialGuiCardGuid);
        bw.writeGUID(senderCylonGuiCardGuid);
        bw.writeUInt16(expire);
        bw.writeString(bodyColonial);
        bw.writeString(bodyCylon);
        bw.writeUInt16(mailLimit);
        bw.writeByte(type.getValue());
        bw.writeGUID(bonusGuid);
    }

    public enum MailType
    {
        Mail,
        TgbPopupMail;

        public static final int SIZE = Byte.SIZE;

        public byte getValue()
        {
            return (byte) this.ordinal();
        }

        public static MailType forValue(byte value)
        {
            return values()[value];
        }
    }

}
