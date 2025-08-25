package io.github.luigeneric.core.protocols.ranking;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.ProtocolContext;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.enums.RankingGroup;
import io.github.luigeneric.enums.RankingType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RankingProtocol extends BgoProtocol
{
    private final RankingProtocolWriteOnly writer;
    public RankingProtocol(ProtocolContext ctx)
    {
        super(ProtocolID.Ranking, ctx);
        this.writer = new RankingProtocolWriteOnly();
    }

    public RankingProtocolWriteOnly writer()
    {
        return writer;
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.forValue((short) msgType);
        //Log.info("RankingProtocol: " + clientMessage);
        if (clientMessage == null)
        {
            log.warn("Invalid ClientMessage in RankingProtocol " + msgType + " user: " + user().getPlayer().getPlayerLog());
            return;
        }


        switch (clientMessage)
        {
            case RequestRankingCounter ->
            {
                final int rawRankingGroup = br.readUint16();
                final int rawRankingType = br.readUint16();
                final long page = br.readUint32();
                final byte sortBy = br.readByte();

                boolean sendValuesInvalid = false;
                if (rawRankingGroup > 13)
                {
                    sendValuesInvalid = true;
                }
                if (rawRankingType > 1)
                {
                    sendValuesInvalid = true;
                }
                if (sendValuesInvalid)
                {
                    log.warn("Ranking Request counter, values invalid " + user().getPlayer().getPlayerLog());
                    return;
                }

                final RankingGroup rankingGroup = RankingGroup.forValue((short) rawRankingGroup);
                final RankingType rankingType = RankingType.forValue((short) rawRankingType);
            }
            case RequestRankingCounterPlayer ->
            {
                final int rawRankingGroup = br.readUint16();
                final int rawRankingType = br.readUint16();
                final byte sortBy = br.readByte();
            }
        }
    }


    enum ClientMessage
    {
        @Deprecated //old highscore system
        RequestRankingTab((short)3),
        @Deprecated //old highscore system
        RequestPlayerRank((short)5),
        RequestRankingCounter((short)7),
        RequestRankingCounterPlayer((short)9),
        RequestRankingTournament((short)11),
        RequestRankingTournamentPlayer((short)13);

        public static final int SIZE = Short.SIZE;

        private final short shortValue;

        private static final class MappingsHolder
        {
            private static final Map<Short, ClientMessage> mappings = new HashMap<>();
        }

        private static Map<Short, ClientMessage> getMappings()
        {
            return MappingsHolder.mappings;
        }

        ClientMessage(short value)
        {
            shortValue = value;
            getMappings().put(value, this);
        }

        public short getValue()
        {
            return shortValue;
        }

        public static ClientMessage forValue(short value)
        {
            return getMappings().get(value);
        }
    }


}
