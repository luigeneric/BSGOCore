package io.github.luigeneric.core.protocols.wof;

import java.util.List;

public record WofBonusMapPart(boolean jackpot, int bonusMapId, List<Integer> cards, long guiCardGuid)
{

}
