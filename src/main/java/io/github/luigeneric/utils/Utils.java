package io.github.luigeneric.utils;

import com.google.gson.*;
import io.github.luigeneric.templates.cards.CardView;
import io.github.luigeneric.templates.cards.ShipConsumableCard;
import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.shipitems.ItemCountable;
import jakarta.enterprise.inject.spi.CDI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Utils
{
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))) // Serialise
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)) // Deserializer
            .create();

    public static Gson getGson() {
        return GSON;
    }


    public static boolean checkOneOptionInvalid(final Optional<?>... optionals)
    {
        for (Optional<?> optional : optionals)
        {
            if (optional.isEmpty())
                return true;
        }
        return false;
    }
    public static long timeToTicks(final TimeUnit timeUnit, final double value)
    {
        double multiplyer = 0;
        switch (timeUnit)
        {
            case MILLISECONDS ->
            {
                multiplyer = 0.1;
            }
            case SECONDS ->
            {
                multiplyer = 10;
            }
            case MINUTES ->
            {
                multiplyer = 10 * 60;
            }
            case HOURS ->
            {
                multiplyer  = 10 * 60 * 60;
            }
        }
        return (long) (value * multiplyer);
    }


    public static String getExceptionStackTrace(final Exception exception)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return new StringBuilder()
                //.append("Critical servererror! ")
                .append(exception.getMessage())
                .append(" stackTraceMessage ")
                .append(stringWriter)
                .toString();
    }

    public static void jdk20CloseExecutorServiceLanguageLevel(final ExecutorService executorService)
    {
        boolean terminated = executorService.isTerminated();
        if (!terminated) {
            executorService.shutdown();
            boolean interrupted = false;
            while (!terminated) {
                try {
                    terminated = executorService.awaitTermination(1L, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    if (!interrupted) {
                        executorService.shutdownNow();
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static ShipConsumableCard fetchShipConsumableCard(ItemCountable currentConsumable)
    {
        var catalogue = CDI.current().select(Catalogue.class).get();
        return catalogue.fetchCardUnsafe(currentConsumable.getCardGuid(), CardView.ShipConsumable);
    }
}

