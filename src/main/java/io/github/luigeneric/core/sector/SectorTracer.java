package io.github.luigeneric.core.sector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class SectorTracer
{
    private final Map<String, Long> events;
    private final Map<String, Long> startTimerMap;
    private String resultStr;


    public SectorTracer(final Map<String, Long> events, final Map<String, Long> startTimerMap)
    {
        this.events = events;
        this.startTimerMap = startTimerMap;
        this.resultStr = "";
    }

    public SectorTracer()
    {
        this(new LinkedHashMap<>(), new HashMap<>());
    }

    private void addEvent(final String name, final long timeNeeded)
    {
        this.events.put(name, timeNeeded);
    }

    public void startTimer(final String name)
    {
        this.startTimerMap.put(name, System.currentTimeMillis());
    }
    public void startTimer(final SectorTraceEvent sectorTraceEvent)
    {
        this.startTimer(sectorTraceEvent.name());
    }
    public void endTimer(final SectorTraceEvent sectorTraceEvent)
    {
        this.endTimer(sectorTraceEvent.name());
    }
    public void endTimer(final String name)
    {
        final Long old = this.startTimerMap.remove(name);
        if (old == null)
            return;

        final long deltaTimeStamp = System.currentTimeMillis() - old;

        this.addEvent(name, deltaTimeStamp);
    }

    public boolean isEventTimeAboveMs(final long milliseconds)
    {
        long sum = 0;
        JsonArray eventsArray = new JsonArray();

        for (Map.Entry<String, Long> eventTimeEntry : this.events.entrySet()) {
            long timeValue = eventTimeEntry.getValue();
            JsonObject eventObject = new JsonObject();
            eventObject.addProperty("Event", eventTimeEntry.getKey());
            eventObject.addProperty("time", timeValue);
            eventsArray.add(eventObject);
            sum += timeValue;
        }

        JsonObject result = new JsonObject();
        result.add("Events", eventsArray);
        result.addProperty("Sum", sum);

        this.resultStr = new Gson().toJson(result);
        //log.info("sector tracer sum {}", sum);
        return sum >= milliseconds;
    }

    public String getResultStr()
    {
        return resultStr;
    }

    public void resetTrace()
    {
        this.events.clear();
        this.startTimerMap.clear();
    }

    @Override
    public String toString()
    {
        return "SectorTracer{" +
                ", events=" + events +
                ", startTimerMap=" + startTimerMap +
                '}';
    }

    public boolean isTimerStarted(final String name)
    {
        return this.startTimerMap.containsKey(name);
    }

    public boolean hasTimedEvent(final String name)
    {
        return this.events.containsKey(name);
    }

    public enum SectorTraceEvent
    {
        JoinQueue,
        MovementUpdater,
        CollisionUpdater,
        AbilityCastRequestQueue,
        TimerUpdater,
        SpaceObjectRemover
    }
}
