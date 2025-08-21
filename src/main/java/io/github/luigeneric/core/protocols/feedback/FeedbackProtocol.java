package io.github.luigeneric.core.protocols.feedback;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.player.PlayerProtocol;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class FeedbackProtocol extends BgoProtocol
{

    public FeedbackProtocol()
    {
        super(ProtocolID.Feedback);
    }

    @Override
    public void parseMessage(final int msgType, final BgoProtocolReader br) throws IOException
    {
        final ClientMessage clientMessage = ClientMessage.forValue(msgType);
        if (clientMessage == null)
        {
            log.error("User {} FeedbackProtocol, unknown messageId: {}", user.getUserLogSimple(), msgType);
            return;
        }


        final UiElementId uiElementId = UiElementId.forValue((short) br.readUint16());

        log.info("FeedbackProtocol: {} {}", clientMessage, uiElementId);

        switch (uiElementId)
        {
            //send durability stats
            case HangarWindow, RepairWindow ->
            {
                final Player player = user.getPlayer();
                final HangarShip hangarShip = player.getHangar().getActiveShip();
                final PlayerProtocol playerProtocol = user.getProtocol(ProtocolID.Player);
                user.send(playerProtocol.writer().writeShipInfoDurability(hangarShip));
                user.send(playerProtocol.writer().writeShipSlots(hangarShip));
            }
        }
    }


}
