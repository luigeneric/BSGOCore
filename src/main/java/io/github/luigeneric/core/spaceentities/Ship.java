package io.github.luigeneric.core.spaceentities;

import com.google.gson.annotations.SerializedName;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.spaceentities.bindings.ShipAspects;
import io.github.luigeneric.core.spaceentities.bindings.ShipBindings;
import io.github.luigeneric.core.spaceentities.statsinfo.stats.SpaceSubscribeInfo;
import io.github.luigeneric.enums.Faction;
import io.github.luigeneric.enums.FactionGroup;
import io.github.luigeneric.enums.SpaceEntityType;
import io.github.luigeneric.templates.cards.*;
import io.github.luigeneric.templates.catalogue.Catalogue;
import jakarta.enterprise.inject.spi.CDI;

import java.util.Optional;

public abstract class Ship extends SpaceObject
{
    @SerializedName("bindings")
    protected final ShipBindings shipBindings;
    @SerializedName("aspects")
    protected final ShipAspects shipAspects;
    protected final ShipCard shipCard;

    protected final MovementCard movementCard;
    protected final Catalogue catalogue;

    protected Ship(long objectID, final OwnerCard ownerCard, final WorldCard worldCard, SpaceEntityType spaceEntityType, Faction faction,
                   FactionGroup factionGroup, ShipBindings shipBindings, ShipAspects shipAspects,
                   final SpaceSubscribeInfo shipSubscribeInfo, final ShipCard shipCard)
    {
        super(objectID, ownerCard, worldCard, spaceEntityType, faction, factionGroup, shipSubscribeInfo);
        this.catalogue = CDI.current().select(Catalogue.class).get();
        this.shipBindings = shipBindings;
        this.shipAspects = shipAspects;
        final Optional<MovementCard> optionalMovementCard = catalogue.fetchCard(this.getWorldCard().getCardGuid(), CardView.Movement);
        if (optionalMovementCard.isEmpty())
            throw new IllegalArgumentException("MovementCard cannot be null");
        this.movementCard = optionalMovementCard.get();
        this.shipCard = shipCard;
    }

    @Override
    public void write(final BgoProtocolWriter bw)
    {
        super.write(bw);
        bw.writeDesc(this.shipBindings);
        bw.writeDesc(this.shipAspects);
    }

    public ShipBindings getShipBindings()
    {
        return shipBindings;
    }

    public ShipAspects getShipAspects()
    {
        return shipAspects;
    }

    public ShipCard getShipCard()
    {
        return shipCard;
    }

    public MovementCard getMovementCard()
    {
        return movementCard;
    }

    @Override
    public boolean isShip()
    {
        return true;
    }

    @Override
    public String getPrefabName()
    {
        final ShipSystemPaintCard paintCard = this.shipBindings.getShipSystemPaintCard();
        if (paintCard == null || paintCard.isUseDefaultModel())
        {
            return super.getPrefabName();
        }
        return paintCard.getPrefabName().toLowerCase();
    }
}
