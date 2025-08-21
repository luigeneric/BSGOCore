package io.github.luigeneric;

import io.github.luigeneric.chatapi.ChatApi;
import io.github.luigeneric.core.GameServer;
import io.github.luigeneric.enums.StaticCardGUID;
import io.github.luigeneric.networking.LegacyTcpLoginServerListener;
import io.github.luigeneric.templates.augments.AugmentTemplate;
import io.github.luigeneric.templates.augments.AugmentTemplates;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.GalaxyMapCard;
import io.github.luigeneric.templates.cards.WorldCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.colliderstemplates.ColliderTemplates;
import io.github.luigeneric.templates.missiontemplates.MissionTemplates;
import io.github.luigeneric.templates.shipconfigs.ShipConfigs;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import io.github.luigeneric.templates.templates.readers.AugmentTemplateReader;
import io.github.luigeneric.templates.templates.readers.MissionTemplateReader;
import io.github.luigeneric.templates.templates.readers.ShipConfigReader;
import io.github.luigeneric.utils.Utils;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@ApplicationScoped
@Slf4j
public class ApplicationLifeCycle
{
    @Inject
    ChatApi chatApi;
    @Inject
    LegacyTcpLoginServerListener loginServerListener;
    @Inject
    ColliderTemplates colliderTemplates;
    @Inject
    GameServerParamsConfig gameServerParamsConfig;
    @Inject
    GameServer gameServer;
    @Inject
    Catalogue catalogue;

    boolean stopped = false;

    void onStartUp(@Observes StartupEvent startupEvent) {
        try {
            legacyMainMethod();
        }
        catch (Exception ex) {
            String stackTrace = Utils.getExceptionStackTrace(ex);
            log.error("Error in ex of legacyMainMethod(): {} \nStack Trace: {}", ex.getMessage(), stackTrace);
            Quarkus.asyncExit(-1);
        }
    }

    @PreDestroy
    public void preDestroy()
    {
        onShutdown();
    }


    public void onShutdown()
    {
        if (stopped)
        {
            log.error("shutdown already triggered");
            return;
        }
        stopped = true;
        log.warn("#################### SHUTDOWN EVENT TRIGGERED ####################");

        log.warn("LoginServerListener stopping..");
        loginServerListener.stop();
        log.warn("LoginServerListener stopped");

        log.warn("GameServer stopping..");
        gameServer.shutdownProcess();
        log.warn("GameServer stopped");
    }

    void legacyMainMethod()
    {
        ShipConfigReader shipConfigReader = new ShipConfigReader();
        shipConfigReader.fetchAndSet();
        final Set<Integer> freeConfigIds = ShipConfigs.getFreeIds();
        log.info("FreeShipConfigIds {}", freeConfigIds);
        log.info("Fetching mission templates");
        try
        {
            MissionTemplates.injectMissions(new MissionTemplateReader().fetchAllMissionTemplates());
        }
        catch (IllegalStateException ex)
        {
            log.error("ERROR IN SETUP MissionTemplates! {}", ex.getMessage());
            Quarkus.asyncExit(-1);
        }

        log.info("...mission template fetch finished");

        final List<WorldCard> worldCards = catalogue.getAllCardsOfView(CardView.World);

        final Set<String> uniqueNames = new HashSet<>();
        for (final WorldCard worldCard : worldCards)
        {
            uniqueNames.add(worldCard.getPrefabName());
        }
        colliderTemplates.buildAsteroidColliderTemplateDummies(uniqueNames);

        final Optional<GalaxyMapCard> optionalGalaxyMapCard = catalogue.fetchCard(StaticCardGUID.GalaxyMap, CardView.GalaxyMap);
        if (optionalGalaxyMapCard.isEmpty())
        {
            log.error("Error, could not find GalaxyMapCard!");
            Quarkus.asyncExit(-1);
        }

        //Augments
        AugmentTemplateReader augmentTemplateReader = new AugmentTemplateReader();
        final Map<Long, AugmentTemplate> augmentTemplateMap = augmentTemplateReader.getAllAugmentTemplates();
        AugmentTemplates.inject(augmentTemplateMap);


        loginServerListener.start();

        try
        {
            if (gameServerParamsConfig.shouldConnectToChatServer())
            {
                chatApi.start();
            }
        }
        catch (Exception ex)
        {
            //okay if that happens, chatserver might be offline
            ex.printStackTrace();
        }


        log.info("Server init...");
        gameServer.start();
        log.info("Server started!");


        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));
    }
}
