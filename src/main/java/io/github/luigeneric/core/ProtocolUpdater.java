package io.github.luigeneric.core;


import io.github.luigeneric.binaryreaderwriter.BgoProtocolReader;
import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import io.github.luigeneric.core.protocols.BgoProtocol;
import io.github.luigeneric.core.protocols.IProtocolRegistry;
import io.github.luigeneric.core.protocols.ProtocolID;
import io.github.luigeneric.core.protocols.ProtocolRegistryWriteOnly;
import io.github.luigeneric.core.protocols.login.LoginProtocolClientMessage;
import io.github.luigeneric.core.protocols.login.LoginProtocolWriteOnly;
import io.github.luigeneric.utils.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProtocolUpdater implements Runnable
{
    private final AbstractConnection connection;
    private final IProtocolRegistry protocolRegistry;
    private boolean isHelloSend;
    private boolean isInitReceived;
    private final LoginProtocolWriteOnly loginWriter;

    public ProtocolUpdater(final AbstractConnection connection, final IProtocolRegistry protocolRegistry)
    {
        this.connection = connection;
        this.protocolRegistry = protocolRegistry;
        this.isHelloSend = true;
        this.loginWriter = ProtocolRegistryWriteOnly.getProtocol(ProtocolID.Login);
        this.isInitReceived = false;
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName("ProtocolUpdater " + connection.getRemoteSocketAddress());
        try
        {
            while (!this.connection.isClosed())
            {
                if (this.isHelloSend)
                {
                    //send hello packet
                    final BgoProtocolWriter bw = loginWriter.writeHello();
                    this.connection.send(bw);
                    this.isHelloSend = false;
                }
                //otherwise read
                final BgoProtocolReader nextMsg = this.connection.recvNextMessage();
                final int protocolID = nextMsg.read();
                final ProtocolID protocol = ProtocolID.forValue(protocolID);
                if (protocol == null)
                {
                    this.connection.closeConnection("Protocol " + protocolID + " does not exist");
                    break;
                }
                final int msgType = nextMsg.readUint16();
                if (!this.isInitReceived)
                {
                    final boolean isLogin = protocol == ProtocolID.Login;
                    final boolean isInit = LoginProtocolClientMessage.Init.value == msgType;
                    if (isLogin && isInit)
                    {
                        connection.setSoTimeout((int) TimeUnit.MINUTES.toMillis(3));
                        this.isInitReceived = true;
                    }
                    else
                    {
                        this.connection.closeConnection(
                                String.format("First init received but neither login or init %s %s %s",
                                        protocol, msgType, connection.getRemoteSocketAddress()));
                        break;
                    }
                }
                try
                {
                    final BgoProtocol incomingBgoProtocol = this.protocolRegistry.getProtocol(protocol);
                    incomingBgoProtocol.parseMessage(msgType, nextMsg);
                }
                catch (NoSuchElementException noSuchElementException)
                {
                    log.info(noSuchElementException.getMessage());
                    break;
                }
                catch (IllegalStateException stateException)
                {
                    this.connection.closeConnection("Inside ProtocolUPdater stateexception: " + stateException.getMessage());
                }
                catch (IOException ioException)
                {
                    this.connection.closeConnection("ProtocolReader issue: " + Utils.getExceptionStackTrace(ioException));
                    break;
                }
            }
        }
        catch (IOException exception)
        {
            this.connection.closeConnection("IOException in ProtocolUpdater: " + exception.getMessage());
        }
        catch (IllegalStateException stateException)
        {
            this.connection.closeConnection("ProtocolUpdater in illegal state " + stateException.getMessage());
        }
        catch (Exception unexpectedException)
        {
            this.connection.closeConnection("Inside ProtocolUpdater, unexpected exception " + Utils.getExceptionStackTrace(unexpectedException));
        }
    }
}
