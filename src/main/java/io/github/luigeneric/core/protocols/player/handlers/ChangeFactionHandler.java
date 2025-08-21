package io.github.luigeneric.core.protocols.player.handlers;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.community.guild.Guild;
import io.github.luigeneric.core.community.party.IParty;
import io.github.luigeneric.core.player.Hangar;
import io.github.luigeneric.core.player.HangarShip;
import io.github.luigeneric.core.player.PlayerAvatar;
import io.github.luigeneric.core.player.container.ShipSlot;
import io.github.luigeneric.core.player.container.visitors.HoldVisitor;
import io.github.luigeneric.core.player.container.visitors.ShipSlotVisitor;
import io.github.luigeneric.core.player.location.Location;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolMessageHandler;
import io.github.luigeneric.core.protocols.community.CommunityProtocol;
import io.github.luigeneric.core.protocols.player.CharacterServices;
import io.github.luigeneric.core.protocols.player.ShipCardConverter;
import io.github.luigeneric.core.protocols.scene.SceneProtocol;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.GameLocation;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.cards.ShipCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.utils.BgoRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ChangeFactionHandler implements ProtocolMessageHandler
{
    private final User user;
    private final CharacterServices characterServices;
    private final Catalogue catalogue;
    private final BgoRandom bgoRandom;

    @Override
    public void handle(BgoProtocolReader reader)
    {
        factionSwitchProcess(true, -1);
    }


    public void factionSwitchProcess(final boolean withPrice, final float cubitsPrice)
    {
        log.info("User faction change triggered " + user.getUserLog());
        if (withPrice)
        {
            final long finalPrice = cubitsPrice == -1 ? (long) characterServices.cubitsPriceFaction() : (long) cubitsPrice;
            final HoldVisitor holdVisitor = new HoldVisitor(user, bgoRandom);
            final boolean isReduceSuccessfully = holdVisitor
                    .reduceItemCountableByCount(ResourceType.Cubits, finalPrice);
            if (!isReduceSuccessfully)
            {
                log.warn("User used ChangeFaction but not enough cubits for change! " + user.getUserLog());
                return;
            }
        }

        final Location currentLocation = user.getPlayer().getLocation();
        final boolean isInRoom = currentLocation.getGameLocation() == GameLocation.Room;
        if (!isInRoom)
        {
            log.warn(user.getUserLog() + "faction switch but not in room");
            return;
        }

        final Hangar hangar = user.getPlayer().getHangar();
        ShipSlotVisitor shipSlotVisitor = new ShipSlotVisitor(user, null);
        for (final HangarShip hangarShip : hangar.getAllHangarShips())
        {
            for (ShipSlot slot : hangarShip.getShipSlots().values())
            {
                final ShipItem removedShipItem = slot.removeShipItem();
                if (removedShipItem == null || removedShipItem.getCardGuid() == 0)
                    continue;
                shipSlotVisitor.addShipItem(removedShipItem, user.getPlayer().getLocker());
            }
        }
        final List<ShipCard> shipCards = hangar.getAllHangarShips().stream()
                .map(HangarShip::getShipCard)
                .toList();

        final ShipCardConverter shipCardConverter = new ShipCardConverter(user.getPlayer().getFaction());
        final List<ShipCard> oppositeShipCards = shipCardConverter.convertAllCards(shipCards);
        hangar.removeAllHangarShips();
        for (final ShipCard oppositeShipCard : oppositeShipCards)
        {
            hangar.addHangarShip(new HangarShip(
                            user.getPlayer().getUserID(),
                            oppositeShipCard.getHangarId(),
                            oppositeShipCard.getCardGuid(),
                            ""
                    )
            );
        }
        hangar.setActiveShipIndex(1);
        final Location location = user.getPlayer().getLocation();
        final Faction invertedFaction = Faction.invert(user.getPlayer().getFaction());
        final int baseSectorId = GalaxyMapCard.getStartSector(invertedFaction);
        final GalaxyMapCard galaxyMapCard = catalogue.fetchCardUnsafe(StaticCardGUID.GalaxyMap, CardView.GalaxyMap);
        var optStar = galaxyMapCard.getStar(baseSectorId);
        if (optStar.isEmpty())
        {
            log.error("Error in faction switch, new base system not present");
            return;
        }
        log.info("Remove user from guild");
        if (user.getPlayer().getGuild().isPresent())
        {
            final Guild guild = user.getPlayer().getGuild().get();
            guild.removePlayer(user.getPlayer().getUserID());
            user.getPlayer().setGuild(null);
            CommunityProtocol communityProtocol = user.getProtocol(ProtocolID.Community);
            final BgoProtocolWriter guildLeaveBw = communityProtocol
                    .writer()
                    .writeGuildRemove(user.getPlayer().getUserID(), true);
            communityProtocol.getGuildProcessing().sendToEachGuildMember(guild, guildLeaveBw);
        }
        log.info("remove from party");
        final Optional<IParty> optParty = user.getPlayer().getParty();
        if (optParty.isPresent())
        {
            final IParty party = optParty.get();
            final CommunityProtocol communityProtocol = user.getProtocol(ProtocolID.Community);
            communityProtocol.getPartyProcessing().removeUserFromParty(user, party);
        }
        //double check
        if (user.getPlayer().getParty().isPresent())
        {
            log.error("warning for faction switch, switch created but still in party, user={}", user.getUserLog());
        }

        log.info("Remove missions");
        user.getPlayer().getCounterFacade().missionBook().resetWithoutTimestamp();


        log.info("Switch faction to " + invertedFaction);
        user.getPlayer().setFaction(invertedFaction);

        log.info("SetLocation to " + baseSectorId + " " + optStar.get().getSectorGuid());
        location.setLocation(GameLocation.Room, baseSectorId, optStar.get().getSectorGuid());
        log.info("setup dummy character");
        final PlayerAvatar avatarDesc = user.getPlayer().getAvatarDescription();

        log.info("Send disconnect!");
        SceneProtocol sceneProtocol = user.getProtocol(ProtocolID.Scene);
        user.send(sceneProtocol.writeDisconnect());
        log.info("Faction switch process finished for user " + user.getUserLog());
    }
}
