package io.github.luigeneric.core.protocols.player.handlers;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.dradisverification.DradisData;
import io.github.luigeneric.core.dradisverification.DradisUpdate;
import io.github.luigeneric.core.protocols.ProtocolMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class SendDradisDataHandler implements ProtocolMessageHandler
{
    private final User user;
    private final DradisData dradisData;

    @Override
    public void handle(BgoProtocolReader br) throws IOException
    {
        final float detectionVisualRadius = br.readSingle();
        final float detectionInnerRadius = br.readSingle();
        final float detectionOuterRadius = br.readSingle();

        final DradisUpdate dradisUpdate = new DradisUpdate(System.currentTimeMillis(),
                detectionVisualRadius, detectionInnerRadius, detectionOuterRadius);
        final boolean isDelayOkay = this.dradisData.updateDradis(dradisUpdate);
        final boolean isCheat = dradisUpdate.detectionOuterRadius() >= 10_000f;
        if (isCheat)
        {
            log.warn("{}Dradis-Cheat! UserID:  {} currentShip {}",
                    user.getUserLog(), dradisUpdate, user.getPlayer().getHangar().getActiveShip().getShipCard().getCardGuid());
        }
    }
}
