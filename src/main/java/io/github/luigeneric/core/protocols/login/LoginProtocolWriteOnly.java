package io.github.luigeneric.core.protocols.login;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.AdminRoles;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LoginProtocolWriteOnly extends WriteOnlyProtocol
{
    public LoginProtocolWriteOnly()
    {
        super(ProtocolID.Login);
    }

    protected BgoProtocolWriter writeLoginError(final LoginError loginError, final String errorMessage)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Error.value);
        bw.writeByte(loginError.getValue());
        bw.writeString(errorMessage);
        return bw;
    }

    protected BgoProtocolWriter writePlayer(final AdminRoles bgoAdminRoles)
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeUInt16(ServerMessage.Player.value);

        final LocalDateTime localDateTime = LocalDateTime.now(Clock.systemUTC());
        final int year = localDateTime.getYear();
        final int month = localDateTime.getMonthValue();
        final int day = localDateTime.getDayOfMonth();
        final int hour = localDateTime.getHour();
        final int minute = localDateTime.getMinute();
        final int second = localDateTime.getSecond();
        final long l = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();

        bw
                .writeInt32(year)
                .writeInt32(month)
                .writeInt32(day)
                .writeInt32(hour)
                .writeInt32(minute)
                .writeInt32(second)
                .writeInt64(l)
                .writeDesc(bgoAdminRoles);

        return bw;
    }

    protected BgoProtocolWriter writeSrvRevision()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Init.value);
        //bw.writeUInt32(4578);
        bw.writeUInt32(3);
        return bw;
    }

    public BgoProtocolWriter writeHello()
    {
        BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Hello.value);
        return bw;
    }


    public BgoProtocolWriter writeLoginQueue(final long queuePosition)
    {
        return newMessage()
                .writeMsgType(ServerMessage.Wait.value)
                .writeUInt32(queuePosition);
    }
}
