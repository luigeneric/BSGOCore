package io.github.luigeneric.core.protocols.debug;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.UsersContainer;
import io.github.luigeneric.enums.BgoAdminRoles;

import java.io.IOException;
import java.util.Optional;

public class UpdateRoleCommand extends DebugCommand
{
    private final UsersContainer usersContainer;
    protected UpdateRoleCommand(final String command, final int requiredPermissions, UsersContainer usersContainer)
    {
        super("update_roles", BgoAdminRoles.ofRoles(BgoAdminRoles.Console, BgoAdminRoles.Developer));
        this.usersContainer = usersContainer;
    }

    @Override
    protected void internalProcess(final BgoProtocolReader br) throws IOException
    {
        try
        {
            final String rawPlayerId = br.readString();
            final String rawRoleBit = br.readString();

            final long playerID = Long.parseLong(rawPlayerId);
            final long roleBits = Long.parseLong(rawRoleBit);
            final Optional<User> optUserForPermissions = usersContainer.get(playerID);
            if (optUserForPermissions.isEmpty())
                return;
            final User userForPermissions = optUserForPermissions.get();
            userForPermissions.getPlayer().getBgoAdminRoles().setOr(roleBits);
            //userForPermissions.send(writeUpdateRoles(roleBits));
        }
        catch (final NumberFormatException numberFormatException)
        {
            //sendEzMsg(numberFormatException.getMessage());
        }
    }
}
