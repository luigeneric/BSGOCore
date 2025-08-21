package io.github.luigeneric;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.slf4j.Slf4j;

@QuarkusMain
@Slf4j
public class Main
{
    public static void main(final String[] args)
    {
        Quarkus.run(MainStartupLogic.class, args);
    }


    public static class MainStartupLogic implements QuarkusApplication
    {
        @Override
        public int run(String... args) throws Exception
        {
            log.info("Quarkus Startup with {}", MainStartupLogic.class.getName());
            Quarkus.waitForExit();
            log.info("Quarkus Shutdown with {}", MainStartupLogic.class.getName());
            return 0;
        }
    }
}
