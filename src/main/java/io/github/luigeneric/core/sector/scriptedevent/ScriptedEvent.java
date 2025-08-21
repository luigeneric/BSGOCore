package io.github.luigeneric.core.sector.scriptedevent;

import io.github.luigeneric.core.sector.npcbehaviour.NpcObjective;

/**
 * assuming we have sector conquest which is
 *  n objectives on both factions
 *  let's say 3 each
 *  one mothership and two other objectives to kill
 *  if the two other objectives got killed it's easier to kill the mothership
 *
 *  Mothership
 *  - only missiles (nuke-prefabs)
 *  - hughe guns?
 *  - no point defense
 *
 *  Supportships
 *  - point defense for motherships
 *  - only the nukes deal insane damage, other ships won't be able to deal enough damage to make a difference
 */
public class ScriptedEvent
{
}

class IScriptedSectorEvent
{
    ScriptedEventPart scriptedEventPart;
}

class ScriptedEventPart
{
    long start;
    long end;
}

class ScriptEventPart2
{
    NpcObjective npcObjective;

    
}
