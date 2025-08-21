package io.github.luigeneric.core.protocols.story;

import java.util.HashMap;
import java.util.Map;

enum ServerMessage
{
    BannerBox(1),
    MessageBox(2),
    HelpBox(3),
    @Deprecated
    Mark(4),
    MissionLog(5),
    SelectTarget(6),
    HighlightControl(7),
    Progress(8),
    HighlightObject(9),
    CloseMessageBoxes(10),
    CloseMissionLog(11),
    PlayCutscene(12),
    SimplifyTutorialUi(13),
    AddSkipButton(14),
    EnableTargetting(15),
    StartLookAtTriggerClientCheck(16),
    EnableMissileTutorial(17),
    ShowOnScreenNotification(18),
    EnableGear(19),
    AskContinue(20);

    public final short shortValue;


    private static final class MappingsHolder
    {
        private static final Map<Short, ServerMessage> mappings = new HashMap<>();
    }

    private static Map<Short, ServerMessage> getMappings()
    {
        return MappingsHolder.mappings;
    }

    ServerMessage(final short value)
    {
        shortValue = value;
        getMappings().put(value, this);
    }

    ServerMessage(final int i)
    {
        this((short) i);
    }

    public short getValue()
    {
        return shortValue;
    }

    public static ServerMessage forValue(short value)
    {
        return getMappings().get(value);
    }
}
