package io.github.luigeneric.core.protocols.debug;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.User;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public abstract class DebugCommand
{
    protected final String command;
    protected final int requiredPermissions;
    protected User user;

    protected DebugCommand(final String command, final int requiredPermissions)
    {
        this.command = command;
        this.requiredPermissions = requiredPermissions;
    }

    public void injectUser(final User user) throws NullPointerException
    {
        this.user = Objects.requireNonNull(user);
    }



    public void execute(final BgoProtocolReader br) throws IOException
    {
        final boolean hasPermissions = permissionsCheck();

        if (!hasPermissions)
        {
            log.warn(user.getUserLog() + "User tried command but has no permissions!");
            return;
        }

        internalProcess(br);
    }

    private boolean permissionsCheck()
    {
        return user.getPlayer().getBgoAdminRoles().hasPermissions(requiredPermissions);
    }

    protected abstract void internalProcess(final BgoProtocolReader br) throws IOException;
}
