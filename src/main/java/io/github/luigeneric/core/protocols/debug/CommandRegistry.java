package io.github.luigeneric.core.protocols.debug;

import java.util.Map;

public class CommandRegistry
{
    private final Map<String, DebugCommand> debugCommandMap;

    public CommandRegistry(final Map<String, DebugCommand> debugCommandMap)
    {
        this.debugCommandMap = debugCommandMap;
    }
}
