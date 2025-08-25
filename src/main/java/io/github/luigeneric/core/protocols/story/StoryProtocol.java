package io.github.luigeneric.core.protocols.story;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
public class StoryProtocol extends BgoProtocol
{
    private final StoryProtocolWriteOnly writer;
    public StoryProtocol(ProtocolContext ctx)
    {
        super(ProtocolID.Story, ctx);
        this.writer = new StoryProtocolWriteOnly();
    }

    public StoryProtocolWriteOnly writer()
    {
        return writer;
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.forValue(msgType);
        switch (clientMessage)
        {
            case TriggerControl ->
            {
                final ControlType controlType = ControlType.forValue(br.readByte());
                log.warn(user().getUserLog() + "StoryProtocol Trigger Control " + controlType);
            }
            case MessageBoxOk ->
            {
                log.warn(user().getUserLog() + "StoryProtocol MessageBoxOk");
            }
            case Skip ->
            {
                log.info(user().getUserLog() + "StoryProtocol Skip");
            }
            case Abandon ->
            {
                log.warn(user().getUserLog() + "StoryProtocol Abandon");
            }
            case Continue ->
            {
                log.warn(user().getUserLog() + "StoryProtocol Continue");
            }
            case Decline ->
            {
                log.warn(user().getUserLog() + "StoryProtocol Decline");
            }
            case CutsceneFinished ->
            {
                log.info(user().getUserLog() + "StoryProtocol cutsceneFinished called!");
            }

            case LookingAtTrigger ->
            {
                log.warn(user().getUserLog() + "StoryProtocol LookingAtTrigger");
            }
            default ->
            {
                log.error(user().getUserLog() + "StoryProtocol: Could not handle replyType: " + clientMessage + " msgType: " + msgType);
            }
        }
    }

    enum ClientMessage
    {
        TriggerControl((short)1),
        MessageBoxOk(((short)1) + 1),
        Skip(((short)1) + 2),
        Abandon(((short)1) + 3),
        Continue(((short)1) + 4),
        Decline(((short)1) + 5),
        CutsceneFinished(((short)1) + 6),
        LookingAtTrigger(((short)1) + 7);

        private final short shortValue;

        private static final class MappingsHolder
        {
            private static final HashMap<Short, ClientMessage> mappings = new HashMap<Short, ClientMessage>();
        }

        private static HashMap<Short, ClientMessage> getMappings()
        {
            return MappingsHolder.mappings;
        }

        ClientMessage(final int value)
        {
            this((short) value);
        }
        ClientMessage(final short value)
        {
            shortValue = value;
            getMappings().put(value, this);
        }

        public short getValue()
        {
            return shortValue;
        }

        public static ClientMessage forValue(final int value)
        {
            return getMappings().get((short)value);
        }
    }
}
