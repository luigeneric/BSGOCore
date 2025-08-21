package io.github.luigeneric.core.player;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.binaryreaderwriter.IProtocolWrite;
import io.github.luigeneric.core.player.container.Mail;
import io.github.luigeneric.utils.collections.SmartMap;

import java.util.HashMap;
import java.util.Map;

public class MailBox extends SmartMap<Mail> implements IProtocolWrite
{
    public MailBox(Map<Integer, Mail> items)
    {
        super(items);
    }
    public MailBox()
    {
        this(new HashMap<>());
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        bw.writeDescCollection(this.items.values());
    }
}
