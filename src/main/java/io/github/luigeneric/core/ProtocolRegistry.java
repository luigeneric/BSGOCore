package io.github.luigeneric.core;


import io.github.luigeneric.core.community.guild.GuildRegistry;
import io.github.luigeneric.core.community.party.PartyRegistry;
import io.github.luigeneric.core.database.DbProvider;
import io.github.luigeneric.core.gameplayalgorithms.ExperienceToLevelAlgo;
import io.github.luigeneric.core.player.login.SessionRegistry;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.CatalogueProtocol;
import io.github.luigeneric.core.protocols.IProtocolRegistry;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.arena.ArenaProtocol;
import io.github.luigeneric.core.protocols.community.CommunityProtocol;
import io.github.luigeneric.core.protocols.debug.DebugProtocol;
import io.github.luigeneric.core.protocols.debug.RefundProcessor;
import io.github.luigeneric.core.protocols.dialog.DialogProtocol;
import io.github.luigeneric.core.protocols.feedback.FeedbackProtocol;
import io.github.luigeneric.core.protocols.game.GameProtocol;
import io.github.luigeneric.core.protocols.login.LoginProtocol;
import io.github.luigeneric.core.protocols.notification.NotificationProtocol;
import io.github.luigeneric.core.protocols.player.CharacterServices;
import io.github.luigeneric.core.protocols.player.PlayerProtocol;
import io.github.luigeneric.core.protocols.ranking.RankingProtocol;
import io.github.luigeneric.core.protocols.room.RoomProtocol;
import io.github.luigeneric.core.protocols.scene.SceneProtocol;
import io.github.luigeneric.core.protocols.setting.SettingProtocol;
import io.github.luigeneric.core.protocols.shop.ShopProtocol;
import io.github.luigeneric.core.protocols.story.StoryProtocol;
import io.github.luigeneric.core.protocols.subscribe.SubscribeProtocol;
import io.github.luigeneric.core.protocols.sync.SyncProtocol;
import io.github.luigeneric.core.protocols.universe.UniverseProtocol;
import io.github.luigeneric.core.protocols.wof.WofProtocol;
import io.github.luigeneric.core.protocols.zone.ZoneProtocol;
import io.github.luigeneric.core.sector.management.SectorRegistry;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProtocolRegistry implements IProtocolRegistry
{
    private final ProtocolContext ctx;
    private final Map<ProtocolID, BgoProtocol> protocols;
    private final SectorRegistry sectorRegistry;
    private final DbProvider dbProviderProvider;
    private final ExperienceToLevelAlgo experienceToLevelAlgo;
    private final PartyRegistry partyRegistry;
    private final GuildRegistry guildRegistry;
    private final UsersContainer usersContainer;
    private final CharacterServices characterServices;
    private final ReadWriteLock readWriteLock;
    private final ChatAccessBlocker chatAccessBlocker;
    private final RefundProcessor refundProcessor;
    private User user;

    public ProtocolRegistry(final ProtocolContext ctx,
                            final SectorRegistry sectorRegistry,
                            final DbProvider dbProviderProvider,
                            final SessionRegistry sessionRegistry,
                            final PartyRegistry partyRegistry,
                            final GuildRegistry guildRegistry,
                            final UsersContainer usersContainer,
                            final ExperienceToLevelAlgo experienceToLevelAlgo,
                            final UserDisconnectedSubscriber connectionClosedSubscriber,
                            final CharacterServices characterServices,
                            final ChatAccessBlocker chatAccessBlocker,
                            final MissionUpdater missionUpdater,
                            final RefundProcessor refundProcessor
                            )
    {
        this.ctx = ctx;
        this.experienceToLevelAlgo = experienceToLevelAlgo;
        this.sectorRegistry = sectorRegistry;
        this.protocols = new HashMap<>();
        this.dbProviderProvider = dbProviderProvider;
        this.partyRegistry = partyRegistry;
        this.guildRegistry = guildRegistry;
        this.usersContainer = usersContainer;
        this.characterServices = characterServices;
        this.readWriteLock = new ReentrantReadWriteLock();
        this.chatAccessBlocker = chatAccessBlocker;
        this.refundProcessor = refundProcessor;

        registerProtocol(
                new LoginProtocol(
                        ctx,
                        sessionRegistry,
                        dbProviderProvider,
                        usersContainer,
                        this,
                        connectionClosedSubscriber,
                        missionUpdater
                )
        );
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends BgoProtocol> T getProtocol(final ProtocolID protocolID) throws NoSuchElementException
    {
        Objects.requireNonNull(protocolID, "protocolId cannot be null!");

        readWriteLock.readLock().lock();
        try
        {
            final BgoProtocol bgoProtocol = protocols.get(protocolID);
            if (bgoProtocol == null)
            {
                throw new NoSuchElementException("could not find Protocol: " + protocolID);
            }
            return (T) bgoProtocol;
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }

    public void loginFinished(final User user)
    {
        readWriteLock.writeLock().lock();
        try
        {
            this.user = user;
            registerProtocol(
                    new PlayerProtocol(
                            ctx,
                            dbProviderProvider,
                            experienceToLevelAlgo,
                            usersContainer,
                            guildRegistry,
                            characterServices
                    )
            );
            registerProtocol(new SyncProtocol(ctx));
            registerProtocol(new SceneProtocol(ctx));
            registerProtocol(new SettingProtocol(ctx));
            registerProtocol(new CatalogueProtocol(ctx));
            registerProtocol(new ShopProtocol(ctx));
            registerProtocol(new CommunityProtocol(ctx, usersContainer, guildRegistry, partyRegistry, chatAccessBlocker));
            registerProtocol(new FeedbackProtocol(ctx));
            registerProtocol(new GameProtocol(ctx, sectorRegistry, usersContainer));
            registerProtocol(new RoomProtocol(ctx));
            registerProtocol(new DialogProtocol(ctx));
            registerProtocol(new WofProtocol(ctx));
            registerProtocol(new UniverseProtocol(ctx));
            registerProtocol(new DebugProtocol(ctx, sectorRegistry, usersContainer, chatAccessBlocker, refundProcessor));
            registerProtocol(new SubscribeProtocol(ctx, usersContainer));
            registerProtocol(new RankingProtocol(ctx));
            registerProtocol(new ArenaProtocol(ctx));
            registerProtocol(new NotificationProtocol(ctx));
            registerProtocol(new StoryProtocol(ctx));
            registerProtocol(new ZoneProtocol(ctx));
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    private void registerProtocol(final BgoProtocol bgoProtocol)
    {
        if (!this.protocols.containsKey(bgoProtocol.getProtocolID()))
        {
            this.protocols.put(bgoProtocol.getProtocolID(), bgoProtocol);
            bgoProtocol.injectUser(user);
        }

    }

    @Override
    public Collection<BgoProtocol> getAllProtocols()
    {
        return this.protocols.values();
    }

    @Override
    public void injectOldRegistry(final User user)
    {
        readWriteLock.writeLock().lock();
        try
        {
            this.user = user;
            for (final BgoProtocol bgoProtocol : user.getProtocolRegistry().getAllProtocols())
            {
                this.registerProtocol(bgoProtocol);
            }
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void onDisconnect()
    {
        readWriteLock.writeLock().lock();
        try
        {
            for (BgoProtocol value : this.protocols.values())
            {
                value.onDisconnect();
            }
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }
}
