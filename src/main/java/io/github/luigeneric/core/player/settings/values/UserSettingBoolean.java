package io.github.luigeneric.core.player.settings.values;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.settings.UserSettingValueType;

public class UserSettingBoolean extends UserSettingValue<Boolean>
{
    public UserSettingBoolean(final boolean value)
    {
        super(value, UserSettingValueType.Boolean);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeBoolean(this.value);
    }
}
