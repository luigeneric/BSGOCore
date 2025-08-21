package io.github.luigeneric.core.protocols.player;


import io.github.luigeneric.core.database.DbProvider;
import io.quarkus.arc.Lock;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class NameValidation
{
    private final DbProvider dbProvider;
    private final Set<NameUserId> reservedNames;
    public NameValidation(final DbProvider dbProvider)
    {
        this.dbProvider = dbProvider;
        this.reservedNames = new HashSet<>();
    }

    @Lock
    public boolean checkNameIsFree(final String nameToCheck, final long userId)
    {
        final boolean nameIsSafe = nameSafeCheck(nameToCheck);
        if (!nameIsSafe)
        {
            log.warn("Cheat name={} is not safe; requested by userId={}", nameToCheck, userId);
            return false;
        }
        final boolean alreadyPresent = dbProvider.checkNameAlreadyPresentNoCase(nameToCheck);
        if (alreadyPresent)
            return false;
        //not reserved => free
        return reservedNames.add(new NameUserId(nameToCheck, userId));
    }
    private static boolean nameSafeCheck(final String name)
    {
        if (name.length() < 3)
            return false;
        if (name.length() > 20)
            return false;
        for (final char c : name.toCharArray())
        {
            final boolean isOk = checkCharIsDigitOrLatinAlphabet(c);
            if (!isOk)
            {
                return false;
            }
        }
        return true;
    }
    private static boolean checkCharIsDigitOrLatinAlphabet(final char c)
    {
        final boolean digitFlag = Character.isDigit(c);
        final boolean isAlphabet = Character.isAlphabetic(c);
        final boolean isLatin = Character.toString(c).matches("[a-zA-Z]");
        final boolean isUnderlined = c == '_';
        return isUnderlined || digitFlag || (isAlphabet && isLatin);
    }


    /**
     * removes a name from the reservedNames set
     * @param name the name to remove
     * @return true if the username is contained
     */
    @Lock
    public boolean removeReservation(final String name, final long userId)
    {
        return this.reservedNames.remove(new NameUserId(name, userId));
    }
}


record NameUserId(String name, long playerId)
{
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NameUserId that = (NameUserId) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}