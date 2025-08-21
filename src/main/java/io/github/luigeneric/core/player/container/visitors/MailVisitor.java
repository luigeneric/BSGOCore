package io.github.luigeneric.core.player.container.visitors;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.container.MoveItemParser;

public class MailVisitor extends ContainerVisitor
{
    public MailVisitor(User user, MoveItemParser moveItemParser)
    {
        super(user, moveItemParser, null);
    }
}