package io.github.luigeneric.core.player.settings.values;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.player.settings.UserSettingValueType;
import io.github.luigeneric.linearalgebra.base.Vector2;

public class UserSettingFloat2 extends UserSettingValue<Vector2>
{
    public UserSettingFloat2(final float x, final float y)
    {
        this(new Vector2(x, y));
    }
    public UserSettingFloat2(final Vector2 value)
    {
        super(value, UserSettingValueType.Float2);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        bw.writeVector2(this.value);
    }

}
