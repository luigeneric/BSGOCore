package io.github.luigeneric.core.gameplayalgorithms;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class MathVersionOriginalTest
{
    MathVersionOriginal mathVersionOriginal = new MathVersionOriginal();


    @Test
    void name()
    {
        short level = mathVersionOriginal.getLevelBasedOnExp(1220782);
        log.info("level {}", level);
    }
}