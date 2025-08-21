package io.github.luigeneric.networking;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.core.player.login.SessionRegistry;
import io.github.luigeneric.templates.startupconfig.GameServerParamsConfig;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
@ApplicationScoped
@Deprecated(forRemoval = false)
public class LegacyTcpLoginServerListener
{
    private final ServerSocket serverSocket;
    private final SessionRegistry sessionRegistry;
    private boolean isConnected;
    private boolean isActive;
    private Socket socket;

    public LegacyTcpLoginServerListener(final SessionRegistry sessionRegistry, final GameServerParamsConfig gameServerParamsConfig) throws IOException
    {
        this.serverSocket = new ServerSocket(gameServerParamsConfig.loginServerPort());
        this.serverSocket.setReuseAddress(true);
        this.sessionRegistry = sessionRegistry;
        this.isConnected = false;
        this.isActive = true;
    }

    public boolean isLoginServerConnected()
    {
        return this.isConnected;
    }

    public void stop()
    {
        this.isActive = false;
    }

    public void start()
    {
        Runnable r = () ->
        {
            while (isActive)
            {
                try
                {
                    if (isConnected)
                    {
                        recv();
                    } else
                    {
                        log.info("LoginServerEndpoint waiting for new connections");
                        this.socket = serverSocket.accept();
                        this.socket.setSoTimeout(1000 * 4);
                        log.info("LoginServer connection from " + socket.getRemoteSocketAddress());
                        this.isConnected = true;
                    }
                } catch (IOException e)
                {
                    log.warn("LoginServerEndpoint disconnected due to an exception: " + e.getMessage());
                }

            }


        };
        Thread t = new Thread(r);
        t.start();
        log.info("LoginServerListener successfully started");
    }

    private void recv()
    {
        try
        {
            if (socket == null || socket.isClosed())
            {
                isConnected = false;
                return;
            }
            final InputStream in = socket.getInputStream();
            int size = in.read();
            if (size <= 0)
            {
                throw new IOException("End of stream");
            }
            final byte[] bytesToRecv = in.readNBytes(size);
            try (BgoProtocolReader br = new BgoProtocolReader(bytesToRecv))
            {
                final int type = br.read();
                switch (type)
                {
                    case 1 ->
                    {
                        final long userId = br.readInt64();
                        final String sessionCode = br.readString();
                        log.info("Session received for user " + userId + " " + sessionCode);
                        sessionRegistry.createSession(userId, sessionCode);
                    }
                    case 2 ->
                    {
                        //just keep alive msg
                        //Log.serverInfo("Keep alive call");
                    }
                }
            }

            //socket.close();
        } catch (Exception e)
        {
            log.info("TcpLoginServer disconnected or error: " + e.getMessage());
            this.isConnected = false;
            try
            {
                this.socket.close();
            } catch (IOException ignored)
            {
            }
        }
    }
}
