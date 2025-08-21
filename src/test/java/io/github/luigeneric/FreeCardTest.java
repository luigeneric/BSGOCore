package io.github.luigeneric;

import io.github.luigeneric.templates.catalogue.Catalogue;
import io.github.luigeneric.templates.templates.readers.CardBuilder;
import io.quarkus.logging.Log;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FreeCardTest
{
    Catalogue catalogue = new Catalogue(new CardBuilder());

    @Test @Disabled
    void testFreeCard()
    {
        var ids = catalogue.getFreeCardGUIDs(10);
        Log.info("ids: " + ids);
    }
}
