package io.github.luigeneric.core.sector.management.lootsystem.killtrace;

import com.google.gson.Gson;
import io.github.luigeneric.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

class KillPushReportTest
{
    @Test
    void testRunGsonWithoutErrors()
    {
        Assertions.assertDoesNotThrow(() ->
        {
            List<KilledObject> killedObjects = List.of(new KilledObject(111, "testkilled", LocalDateTime.now(Clock.systemUTC())));
            KillPushReport killPushReport = new KillPushReport(234, "ajshdia", killedObjects);
            Gson gson = Utils.getGson();
            String json = gson.toJson(killPushReport);
        });
    }
}