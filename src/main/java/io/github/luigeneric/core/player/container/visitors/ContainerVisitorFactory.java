package io.github.luigeneric.core.player.container.visitors;


import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.container.ContainerType;
import io.github.luigeneric.core.player.container.MoveItemParser;
import io.github.luigeneric.utils.BgoRandom;

public class ContainerVisitorFactory
{
    public static ContainerVisitor createVisitor(final ContainerType containerType,
                                                 final User user,
                                                 final MoveItemParser moveItemParser,
                                                 final BgoRandom bgoRandom
    )
    {
        switch (containerType)
        {
            case Hold ->
            {
                return new HoldVisitor(user, moveItemParser, bgoRandom);
            }
            case Locker ->
            {
                return new LockerVisitor(user, moveItemParser, bgoRandom);
            }
            case ShipSlot ->
            {
                return new ShipSlotVisitor(user, moveItemParser);
            }
            case Shop ->
            {
                return new ShopVisitor(user, moveItemParser, bgoRandom);
            }
            case Mail ->
            {
                return new MailVisitor(user, moveItemParser);
            }
            case EventShop ->
            {
                return new EventShopVisitor(user, moveItemParser, bgoRandom);
            }

            default ->
            {
                throw new IllegalArgumentException(containerType + " not implemented");
            }
        }
    }
}
