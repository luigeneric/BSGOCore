package io.github.luigeneric.core.player.settings;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolRead;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.enums.KeyCode;
import io.github.luigeneric.enums.KeyModifier;

import java.io.IOException;

public class InputBinding implements IProtocolRead, IProtocolWrite
{
    protected Action action;
    /**
     * original value type: unsigned short
     */
    protected int deviceTriggerCode;
    /**
     * original value type: unsigned byte
     */
    protected short deviceModifierCode; //byte
    protected short device;             //byte
    protected short flags;              //byte
    protected short profileNo;          //byte

    public InputBinding(Action action, int deviceTriggerCode, short deviceModifierCode,
                        short device, short flags, short profileNo)
    {
        this.action = action;
        this.deviceTriggerCode = deviceTriggerCode;
        this.deviceModifierCode = deviceModifierCode;
        this.device = device;
        this.flags = flags;
        this.profileNo = profileNo;
    }
    public InputBinding() {}

    public InputBinding(final Action action, final KeyCode keyCode, final KeyModifier keyModifier)
    {
        this(action, keyCode.getValue(), keyModifier.getValue());
    }
    public InputBinding(final Action action, final int keyCode, final short modifierCode)
    {
        this(action, keyCode, modifierCode, (short) 0, (short) 0, (short) 0);
    }
    public InputBinding(final Action action, final int keyCode)
    {
        this(action, keyCode, (short) 0);
    }
    public InputBinding(final Action action, final KeyCode keyCode)
    {
        this(action, keyCode.getValue());
    }

    @Override
    public void read(BgoProtocolReader br) throws IOException
    {
        this.deviceTriggerCode = br.readUint16();
        this.action = Action.forValue(br.readUint16());
        this.deviceModifierCode = br.readByte();
        this.device = br.readByte();
        this.flags = br.readByte();
        this.profileNo = br.readByte();
    }


    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeUInt16(this.deviceTriggerCode);
        bw.writeUInt16(this.action.intValue);
        bw.writeByte((byte) this.deviceModifierCode);
        bw.writeByte((byte) this.device);
        bw.writeByte((byte) this.flags);
        bw.writeByte((byte) this.profileNo);
    }

    @Override
    public String toString()
    {
        return "InputBinding{" +
                "action=" + action +
                ", deviceTriggerCode=" + deviceTriggerCode +
                ", deviceModifierCode=" + deviceModifierCode +
                ", device=" + device +
                ", flags=" + flags +
                ", profileNo=" + profileNo +
                '}';
    }

    public Action getAction()
    {
        return action;
    }

    public int getDeviceTriggerCode()
    {
        return deviceTriggerCode;
    }

    public short getDeviceModifierCode()
    {
        return deviceModifierCode;
    }

    public short getDevice()
    {
        return device;
    }

    public short getFlags()
    {
        return flags;
    }

    public short getProfileNo()
    {
        return profileNo;
    }
}