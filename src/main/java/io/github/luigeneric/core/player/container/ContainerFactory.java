package io.github.luigeneric.core.player.container;



import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.player.container.containerids.*;

import java.io.IOException;
import java.util.Optional;

public class ContainerFactory
{
    public static IContainer getContainer(final BgoProtocolReader br, final Player player) throws IOException, IllegalArgumentException
    {
        final IContainerID iContainerID = readContainerId(br);

        switch (iContainerID.getContainerType())
        {
            case Hold ->
            {
                return player.getHold();
            }
            case Locker ->
            {
                return player.getLocker();
            }
            case ShipSlot ->
            {
                final ShipSlotContainerID shipSlotContainerID = (ShipSlotContainerID) iContainerID;

                final HangarShip ship = player.getHangar().getByServerId(shipSlotContainerID.getShipID());
                final ShipSlot slot = ship.getShipSlots().getSlot(shipSlotContainerID.getSlotID());
                slot.setContainerID(shipSlotContainerID);
                return slot;
            }
            case Shop ->
            {
                return player.getShop();
            }
            case EventShop ->
            {
                return player.getEventShop();
            }
            case Mail ->
            {
                MailContainerID mailContainerID = new MailContainerID();
                mailContainerID.read(br);
                final Optional<Mail> mail = player.getMailBox().getByID(mailContainerID.getMailID());
                if (mail.isEmpty())
                {
                    throw new IllegalArgumentException("Mail is null");
                }
                return mail.get().getMailContainer();
            }
            case BlackHole ->
            {
                return player.getBlackHole();
            }
            default -> throw new IllegalStateException("could not find container from type: " + iContainerID);
        }
    }
    protected static IContainerID readContainerId(final BgoProtocolReader br) throws IOException, IllegalArgumentException
    {
        ContainerType containerType = ContainerType.forValue(br.readByte());
        IContainerID containerID = null;
        switch (containerType)
        {
            case Hold -> containerID = new HoldContainerID();
            case Locker -> containerID = new LockerContainerID();
            case Mail -> containerID = new MailContainerID();
            case ShipSlot -> containerID = new ShipSlotContainerID();
            case BlackHole -> containerID = new BlackHoleContainerID();
            case Shop -> containerID = new ShopContainerID();
            case EventShop -> containerID = new EventShopContainerID();
        }
        if (containerID == null)
        {
            throw new IllegalArgumentException("Could not find container id in readContainerId because no type found for "
                    + containerType);
        }

        containerID.read(br);
        return containerID;
    }
}
