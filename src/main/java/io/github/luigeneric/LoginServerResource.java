package io.github.luigeneric;

import io.github.luigeneric.core.player.login.SessionRegistry;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * LoginServer Resource alternative for deprecated legacy tcp listener
 * Normally I would expect a flow like this: <br>
 * 1. Client sends a login request to the login server <br>
 * 2. Login server validates the request and creates a session <br>
 * 3. Client sends the session code to the game server <br>
 * 4. Game server validates the session code and creates a player session <br>
 * @implNote step 4 is done in a risky situation but I guess it's too late to reverse control flow
 */
@Slf4j
@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginServerResource
{
    private final SessionRegistry sessionRegistry;

    public LoginServerResource(SessionRegistry sessionRegistry)
    {
        this.sessionRegistry = sessionRegistry;
    }

    @POST
    public Response login(LoginServerRequest loginServerRequest)
    {
        log.info("Login request received: {}", loginServerRequest);
        sessionRegistry.createSession(loginServerRequest.userId(), loginServerRequest.sessionCode());
        return Response.ok().build();
    }
}

