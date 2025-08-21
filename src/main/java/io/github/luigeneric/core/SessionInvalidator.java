package io.github.luigeneric.core;


import io.github.luigeneric.core.player.login.Session;
import io.github.luigeneric.core.player.login.SessionRegistry;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Optional;

@ApplicationScoped
public class SessionInvalidator
{
    private final SessionRegistry sessionRegistry;
    private final UsersContainer usersContainer;

    public SessionInvalidator(final SessionRegistry sessionRegistry, final UsersContainer usersContainer)
    {
        this.sessionRegistry = sessionRegistry;
        this.usersContainer = usersContainer;
    }

    @Scheduled(every = "2m")
    public void run()
    {
        final Collection<Session> allSessionsInUse = sessionRegistry.getAllSessions(session -> session.getSessionState() == Session.SessionState.InUse);
        for (final Session session : allSessionsInUse)
        {
            final Optional<User> optUser = usersContainer.get(session.getUserId());
            if (optUser.isEmpty())
            {
                session.setSessionState(Session.SessionState.Expired);
                continue;
            }
            final User user = optUser.get();
            final boolean isEquals = user.getSession().equals(session);
            //not equal, invalidate session which appears to be in use but is not realted to the given user!
            if (!isEquals)
            {
                session.setSessionState(Session.SessionState.Expired);
            }
        }
    }
}
