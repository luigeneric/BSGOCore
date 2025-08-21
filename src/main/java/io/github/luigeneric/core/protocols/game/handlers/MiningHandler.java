package io.github.luigeneric.core.protocols.game.handlers;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.Player;
import io.github.luigeneric.core.protocols.ProtocolMessageHandler;
import io.github.luigeneric.core.protocols.game.SectorPlayerShipFetchResult;
import io.github.luigeneric.core.sector.Sector;
import io.github.luigeneric.core.sector.management.SectorRegistry;
import io.github.luigeneric.templates.utils.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class MiningHandler implements ProtocolMessageHandler
{
    private final User user;
    private final SectorRegistry sectorRegistry;

    @Override
    public void handle(BgoProtocolReader br) throws IOException
    {
        final long planetoidID = br.readUint32();
        var secPlayerShip = this.getSectorAndPlayerShip();
        var sector = secPlayerShip.sector();

        log.info("planetoid request mining on(start): " + planetoidID);
        final Optional<Price> price = sector.getMiningSectorOperations().removeMiningRequest(user.getPlayer().getUserID(), planetoidID);
        if (price.isEmpty())
        {
            //cheat?, player used this but it should never happened
            log.warn("Player {} cheating(?), using mining requests even tho he never scanned or scan is too far in history!", user.getUserLog());
            return;
        }
        log.info("planetoid request mining on: " + planetoidID + " price(call onMiningShipRequest): " + price.get());
        sector.getMiningSectorOperations().onMiningShipRequest(this.user, planetoidID, price.get());
    }

    private SectorPlayerShipFetchResult getSectorAndPlayerShip()
    {
        final Optional<Sector> optSector = getSector();
        if (optSector.isEmpty())
            return SectorPlayerShipFetchResult.NO_SECTOR;

        var sector = optSector.get();
        var optPlayerShip = sector.getCtx().users().getPlayerShipByUserID(user.getPlayer().getUserID());
        return optPlayerShip
                .map(playerShip ->
                        new SectorPlayerShipFetchResult(
                                sector, playerShip)).orElseGet(() -> new SectorPlayerShipFetchResult(sector, null)
                );

    }
    private Optional<Sector> getSector()
    {
        if (user == null)
        {
            return Optional.empty();
        }
        final Player player = user.getPlayer();
        return sectorRegistry.getSectorById(player.getSectorId());
    }
}
