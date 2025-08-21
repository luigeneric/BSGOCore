package io.github.luigeneric.core.spaceentities;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.movement.PlayerMovementController;
import io.github.luigeneric.core.spaceentities.bindings.PlayerVisibility;
import io.github.luigeneric.core.spaceentities.bindings.ShipAspects;
import io.github.luigeneric.core.spaceentities.bindings.ShipBindings;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.CreatingCause;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.templates.cards.OwnerCard;
import io.github.luigeneric.templates.cards.ShipCard;
import io.github.luigeneric.templates.cards.WorldCard;

public class PlayerShip extends Ship
{
    private final int roles;
    private final PlayerVisibility playerVisibility;
    private final long playerID;

    public PlayerShip(long objectID, final OwnerCard ownerCard, final WorldCard worldCard, final ShipCard shipCard,
                      final Faction faction, final FactionGroup factionGroup, ShipBindings shipBindings,
                      ShipAspects shipAspects,
                      final long playerId, final int bgoAdminRoles, final PlayerVisibility playerVisibility,
                      final SpaceSubscribeInfo playerShipInfo)
    {
        super(objectID, ownerCard, worldCard, SpaceEntityType.Player, faction, factionGroup, shipBindings, shipAspects,
                playerShipInfo, shipCard);
        this.playerID = playerId;
        this.roles = bgoAdminRoles;
        this.playerVisibility = playerVisibility;
        this.setCreatingCause(CreatingCause.AlreadyExists);
    }

    @Override
    public void createMovementController(final Transform transform)
    {
        this.movementController = new PlayerMovementController(transform, movementCard, playerID);
    }

    @Override
    public void write(BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeUInt32(this.playerID);
        bw.writeUInt32(this.roles);
        //bw.writeBoolean(this.playerVisibility.isVisible());
        bw.writeDesc(playerVisibility);
    }

    @Override
    public boolean isVisible()
    {
        return this.playerVisibility.isVisible();
    }

    public PlayerVisibility getPlayerVisibility()
    {
        return this.playerVisibility;
    }

    @Override
    public long getPlayerId()
    {
        return this.playerID;
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerShip that = (PlayerShip) o;

        return playerID == that.playerID;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (int) (playerID ^ (playerID >>> 32));
        return result;
    }
}
