package io.github.luigeneric.templates.zonestemplates;

import io.github.luigeneric.utils.Utils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class ZoneTemplateTest
{
    @Test
    void testToJson()
    {
        ZoneTemplate zoneTemplate = new ZoneTemplate(
                111,
                10000,
                new CronSchedule("0 * * * *", "30"),
                Set.of(
                        new ZoneObjectivePt(ZoneObjectivePtType.MotherShip, 1000),
                        new ZoneObjectivePt(ZoneObjectivePtType.Player, 100),
                        new ZoneObjectivePt(ZoneObjectivePtType.Platform, 150),
                        new ZoneObjectivePt(ZoneObjectivePtType.Drone, 25),
                        new ZoneObjectivePt(ZoneObjectivePtType.Torpedo, 15)
                ),
                List.of(
                        new ObjectiveTemplate(
                                1, ObjectiveType.DESTROY,null, 0, "Kill the mothership", null, null
                )),
                List.of(new ScriptableObject(ZoneObjectiveTriggerCondition.ON_START))
        );
        var json = Utils.getGson().toJson(zoneTemplate, ZoneTemplate.class);
        System.out.println(json);
    }
}