package io.github.luigeneric.core.protocols.login;

import java.util.Objects;

public final class SessionHandlingResult
{
    private final long playerID;
    private final String session;
    private final boolean isValid;

    private SessionHandlingResult(long playerID, String session, boolean isValid)
    {
        this.playerID = playerID;
        this.session = session;
        this.isValid = isValid;
    }

    public static SessionHandlingResult validSession(final long userID, final String session)
    {
        return new SessionHandlingResult(userID, session, true);
    }

    public static SessionHandlingResult invalidSession(final long userID)
    {
        return new SessionHandlingResult(userID, null, false);
    }
    public static SessionHandlingResult invalidSession()
    {
        return new SessionHandlingResult(-1, null, false);
    }

    public long playerID()
    {
        return playerID;
    }

    public String session()
    {
        return session;
    }

    public boolean isValid()
    {
        return isValid;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SessionHandlingResult) obj;
        return this.playerID == that.playerID &&
                Objects.equals(this.session, that.session) &&
                this.isValid == that.isValid;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(playerID, session, isValid);
    }

    @Override
    public String toString()
    {
        return "SessionHandlingResult[" +
                "userID=" + playerID + ", " +
                "session=" + session + ", " +
                "isValid=" + isValid + ']';
    }

}
