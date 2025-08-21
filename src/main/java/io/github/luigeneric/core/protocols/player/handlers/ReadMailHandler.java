package io.github.luigeneric.core.protocols.player.handlers;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.MailBox;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.container.Mail;
import io.github.luigeneric.core.protocols.ProtocolMessageHandler;
import io.github.luigeneric.core.protocols.player.PlayerProtocolWriteOnly;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ReadMailHandler implements ProtocolMessageHandler
{
    private final User user;
    private final PlayerProtocolWriteOnly writer;

    @Override
    public void handle(BgoProtocolReader br) throws IOException
    {
        log.info("ReadMailHandler");
        final int mailID = br.readUint16();
        final Player player = this.user.getPlayer();
        final MailBox mailBox = player.getMailBox();
        final Optional<Mail> optionalMail = mailBox.getByID(mailID);
        if (optionalMail.isEmpty()) return;
        final Mail mail = optionalMail.get();
        mail.setMailStatus(Mail.MailStatus.Normal);
        user.send(writer.writeMailBox(mailBox));
    }
}
