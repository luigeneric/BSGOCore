package io.github.luigeneric.core.sector.timers;

import io.github.luigeneric.core.User;
import io.github.luigeneric.core.player.container.Hold;
import io.github.luigeneric.core.player.container.visitors.HoldVisitor;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.game.GameProtocol;
import io.github.luigeneric.core.sector.Tick;
import io.github.luigeneric.core.sector.management.*;
import io.github.luigeneric.core.spaceentities.SpaceObject;
import io.github.luigeneric.enums.BgoAdminRoles;
import io.github.luigeneric.enums.RemovingCause;
import io.github.luigeneric.enums.ResourceType;
import io.github.luigeneric.linearalgebra.base.Vector2;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.sectortemplates.SectorDesc;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import io.github.luigeneric.templates.shipitems.ShipItem;
import io.github.luigeneric.templates.utils.MapStarDesc;
import io.github.luigeneric.templates.utils.ObjectStat;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class JumpTimer extends UpdateTimer
{
    private final Tick tick;
    private final JumpRegistry jumpRegistry;
    private final ISpaceObjectRemover remover;
    private final GalaxyMapCard galaxyMapCard;
    private final SectorDesc sectorDesc;
    private final SectorUsers users;

    public JumpTimer(final SectorSpaceObjects sectorSpaceObjects, final Tick tick,
                     final JumpRegistry jumpRegistry, final ISpaceObjectRemover remover, final GalaxyMapCard galaxyMapCard,
                     final SectorDesc sectorDesc, final SectorUsers sectorUsers)
    {
        super(sectorSpaceObjects);
        this.tick = tick;
        this.jumpRegistry = jumpRegistry;
        this.remover = remover;
        this.galaxyMapCard = galaxyMapCard;
        this.sectorDesc = sectorDesc;
        this.users = sectorUsers;
    }

    @Override
    public void update(final float dt)
    {
        // idk what this is about anymore ._.
        if (sectorDesc.getSectorID() == 9999)
        {
            for (User user : users.getUsers())
            {
                if (user.getPlayer().getBgoAdminRoles().hasOneRole(BgoAdminRoles.Developer, BgoAdminRoles.CommunityManager, BgoAdminRoles.Mod))
                {
                    continue;
                }
                users.getPlayerShipByUserID(user.getPlayer().getUserID())
                        .ifPresent(ps ->
                        {
                            if (!jumpRegistry.containsSpaceObject(ps.getObjectID()))
                            {
                                final GameProtocol gameProtocol = user.getProtocol(ProtocolID.Game);
                                gameProtocol.jumpProcedure(
                                        jumpRegistry,
                                        GalaxyMapCard.getStartSector(user.getPlayer().getFaction()),
                                        10,
                                        false);
                            }
                        });
            }
        }

        while (jumpRegistry.hasItemsWithTimeout(tick))
        {
            final JumpScheduleItem jumpScheduleItem = jumpRegistry.getItem();
            if (jumpScheduleItem == null)
                continue;
            final SpaceObject r = jumpScheduleItem.getEntry();
            //this.sector.addSpaceObjectRemoveRequest(r, RemovingCause.JumpOut);


            if (!r.isPlayer())
            {
                remover.notifyRemovingCauseAdded(r, RemovingCause.JumpOut);
                continue;
            }


            final long removeObject = r.getPlayerId();
            final Optional<User> optUser = users.getUser(removeObject);
            if (optUser.isEmpty())
                continue;
            final User opUser = optUser.get();
            final Map<Long, MapStarDesc> stars = galaxyMapCard.getStars();
            final MapStarDesc from = stars.get(this.sectorDesc.getSectorID());
            final MapStarDesc to = stars.get(jumpScheduleItem.getTargetSector());
            if (from != null && to != null)
            {
                final float distance = Vector2.distance(from.getPosition(), to.getPosition());
                final float ftlCosts = r.getSpaceSubscribeInfo().getStatOrDefault(ObjectStat.FtlCost);
                final float finalCosts = (float) Math.ceil(ftlCosts * distance);
                final Hold hold = opUser.getPlayer().getHold();
                final Optional<ShipItem> optTylium = hold.getByGUID(ResourceType.Tylium.guid);
                if (optTylium.isEmpty())
                    continue;
                final ItemCountable tylium = (ItemCountable) optTylium.get();
                tylium.decrementCount((long) finalCosts);
                HoldVisitor holdVisitor = new HoldVisitor(opUser, null);
                try
                {
                    holdVisitor.reduceItemCountableByCount(tylium, (long) finalCosts, hold);
                }
                catch (IllegalArgumentException illegalArgumentException)
                {
                    log.info("{} tried to jump but not resources!", opUser.getUserLog());
                    continue;
                }
            }
            GameProtocol gameProtocol = opUser.getProtocol(ProtocolID.Game);
            if (jumpScheduleItem.getPlayerIds().length == 0)
            {
                gameProtocol.jumpOutTimerSuccessful(null);
            } else
            {
                gameProtocol.jumpOutTimerSuccessful(jumpScheduleItem.getPlayerIds());
            }
            remover.notifyRemovingCauseAdded(r, RemovingCause.JumpOut);
        }
    }
}
