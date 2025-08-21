package io.github.luigeneric.core.player.settings.values;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.HelpScreenType;
import io.github.luigeneric.core.player.settings.UserSettingValueType;

import java.util.List;

public class UserSettingHelpScreen extends UserSettingValue<List<HelpScreenType>>
{
    public UserSettingHelpScreen(final List<HelpScreenType> value)
    {
        super(value, UserSettingValueType.HelpScreenType);
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeLength(this.value.size());
        for (final HelpScreenType helpScreenType : this.value)
        {
            bw.writeUInt16(helpScreenType.intValue);
        }
    }
}
