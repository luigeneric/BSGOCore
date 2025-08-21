package io.github.luigeneric;

import io.github.luigeneric.templates.utils.ShipAbilityTarget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShipAbilityTargetTest
{
    @Test
    void testMappingInit()
    {
        var target = ShipAbilityTarget.forValue((byte) 8);
        Assertions.assertEquals(ShipAbilityTarget.Missile, target);
    }
}
