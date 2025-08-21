package io.github.luigeneric.core.sector.management.spawn;


import io.github.luigeneric.core.sector.SpaceObjectFactory;
import io.github.luigeneric.core.sector.management.SectorJoinQueue;
import io.github.luigeneric.core.sector.npcbehaviour.PatrolObjective;
import io.github.luigeneric.core.spaceentities.BotFighterMoving;
import io.github.luigeneric.linearalgebra.base.Quaternion;
import io.github.luigeneric.linearalgebra.base.Transform;
import io.github.luigeneric.linearalgebra.base.Vector3;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.ShipCard;
import io.github.luigeneric.templates.npcbehaviour.NpcBehaviourTemplate;
import io.github.luigeneric.templates.npcbehaviour.NpcBehaviourTemplates;
import io.github.luigeneric.templates.sectortemplates.spaceobjectttemplates.BotTemplate;
import io.github.luigeneric.utils.BgoRandom;

import java.util.List;
import java.util.Optional;

public class DynamicNpcSpawn extends SpawnAble
{
    private final SectorJoinQueue joinQueue;
    private final SpaceObjectFactory factory;
    private final BgoRandom bgoRandom;
    private final BotTemplate botTemplate;

    public DynamicNpcSpawn(final SpawnController spawnController,
                           final SectorJoinQueue joinQueue,
                           final SpaceObjectFactory factory,
                           final BgoRandom bgoRandom,
                           final BotTemplate botTemplate)
    {
        super(spawnController, spawnController);
        this.joinQueue = joinQueue;
        this.factory = factory;
        this.bgoRandom = bgoRandom;
        this.botTemplate = botTemplate;
    }

    @Override
    public void spawn()
    {
        final long guid = botTemplate.getObjectGUID();
        final Optional<ShipCard> optShipCard = catalogue.fetchCard(guid, CardView.Ship);
        if (optShipCard.isEmpty())
        {
            return;
        }
        final ShipCard shipCard = optShipCard.get();
        //objectives to kill
        //objectives to defend
        //position
        final Vector3 spawnVector3 = new Vector3(bgoRandom.getInsideVectors(
                botTemplate.getSpawnBox().min().toArray(), botTemplate.getSpawnBox().max().toArray())
        );
        final Quaternion spawnRotation = Quaternion.randomRotation(bgoRandom.getRndBetweenInt(0, 3));
        final Transform spawnTransform = new Transform(spawnVector3, spawnRotation);
        //behaviour template
        final NpcBehaviourTemplate behaviourTemplate = NpcBehaviourTemplates.createTemplateForTier(shipCard.getTier(),
                bgoRandom.variateByPercentage(botTemplate.getLifeTimeSeconds(), 0.1f), botTemplate.isJumpOutIfInCombat(),
                400);

        final long[] lootId = botTemplate.getLootTemplateIds();

        final PatrolObjective patrolObjective = new PatrolObjective(0, botTemplate.getSpawnBox());
        final BotFighterMoving botFighterMoving = this.factory
                .createBotFighter(guid, List.of(), List.of(), List.of(patrolObjective), spawnTransform, behaviourTemplate, lootId);
        this.joinQueue.addSpaceObject(botFighterMoving);
        this.spawnController.getObjectTemplateAssociations().put(botFighterMoving.getObjectID(),
                new TemplateSpaceObjectRecord(botTemplate, botFighterMoving));
    }

    @Override
    public SpawnAble getNext()
    {
        return null;
    }
}
