package io.github.luigeneric.core.player.settings;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolRead;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.player.HelpScreenType;
import io.github.luigeneric.core.player.settings.values.*;
import io.github.luigeneric.utils.AutoLock;
import org.jboss.logging.MDC;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UserSettings implements IProtocolWrite, IProtocolRead
{
    private final Map<UserSetting, UserSettingValue<?>> settingsMap;
    private final Lock lock;

    public UserSettings(final Map<UserSetting, UserSettingValue<?>> settingsMap, final long userID)
    {
        this.settingsMap = settingsMap;
        MDC.put("userID", String.valueOf(userID));
        this.lock = new ReentrantLock();
    }
    public UserSettings(final long userID)
    {
        this(new HashMap<>(), userID);
    }

    public void put(final UserSetting userSetting, final UserSettingValue<?> settingValue)
    {
        try(var l = new AutoLock(lock))
        {
            putInternal(userSetting, settingValue);
        }
    }

    private void putInternal(final UserSetting userSetting, final UserSettingValue<?> settingValue)
    {
        this.settingsMap.put(userSetting, settingValue);
    }

    public UserSettingValue<?> get(final UserSetting userSetting)
    {
        try(var l = new AutoLock(lock))
        {
            return this.settingsMap.get(userSetting);
        }
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        try(var l = new AutoLock(lock))
        {
            bw.writeLength(this.settingsMap.size());
            for (final Map.Entry<UserSetting, UserSettingValue<?>> entry : this.settingsMap.entrySet())
            {
                //write UserSetting
                bw.writeByte(entry.getKey().value);
                //now comes an empty readByte
                bw.writeByte((byte) 0);
                //now write the value
                bw.writeDesc(entry.getValue());
            }
        }
    }

    public Map<UserSetting, UserSettingValue<?>> getSettingsUnmodifiableMap()
    {
        try(var l = new AutoLock(lock))
        {
            return Collections.unmodifiableMap(settingsMap);
        }
    }

    @Override
    public void read(final BgoProtocolReader br) throws IOException
    {
        try(var l = new AutoLock(lock))
        {
            final int len = br.readLength();
            for (int i = 0; i < len; i++)
            {
                readSingleUserSetting(br);
            }
        }
    }

    private void readSingleUserSetting(BgoProtocolReader br) throws IOException
    {
        final UserSetting userSetting = UserSetting.forValue(br.readByte());
        final UserSettingValueType userSettingValueType = UserSettingValueType.forValue(br.readByte());
        switch (userSettingValueType)
        {
            case Byte ->
            {
                this.putInternal(userSetting, new UserSettingByte(br.readByte()));
            }
            case Boolean ->
            {
                this.putInternal(userSetting, new UserSettingBoolean(br.readBoolean()));
            }
            case Float ->
            {
                this.putInternal(userSetting, new UserSettingFloat(br.readSingle()));
            }
            case Float2 ->
            {
                this.putInternal(userSetting, new UserSettingFloat2(br.readVector2()));
            }
            case Integer ->
            {
                this.putInternal(userSetting, new UserSettingInteger(br.readInt32()));
            }
            case HelpScreenType ->
            {
                final int screenTypesSize = br.readLength();
                final List<HelpScreenType> helpScreenTypes = new ArrayList<>(screenTypesSize);
                for (int i1 = 0; i1 < screenTypesSize; i1++)
                {
                    helpScreenTypes.add(HelpScreenType.forValue(br.readUint16()));
                }
                this.putInternal(userSetting, new UserSettingHelpScreen(helpScreenTypes));
            }
            default ->
            {
                br.readByte(); //there seems to be an empty 0 byte
            }
        }
    }
}
