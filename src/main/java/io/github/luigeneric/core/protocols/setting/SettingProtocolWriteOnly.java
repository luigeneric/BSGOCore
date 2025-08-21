package io.github.luigeneric.core.protocols.setting;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.settings.InputBindings;
import io.github.luigeneric.core.player.settings.UserSettings;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.WriteOnlyProtocol;

public class SettingProtocolWriteOnly extends WriteOnlyProtocol
{
    public SettingProtocolWriteOnly()
    {
        super(ProtocolID.Setting);
    }

    public BgoProtocolWriter writeInputBindings(final InputBindings inputBindings)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Keys.value);
        bw.writeDesc(inputBindings);
        return bw;
    }

    public BgoProtocolWriter writeSettings(final UserSettings userSettings)
    {
        final BgoProtocolWriter bw = newMessage();
        bw.writeMsgType(ServerMessage.Settings.value);
        bw.writeDesc(userSettings);

        return bw;
    }
}
