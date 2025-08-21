package io.github.luigeneric.networking.sender;

import io.github.luigeneric.binaryreaderwriter.BgoProtocolWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


@Slf4j
public class BlockingQueueSender implements IPacketSender
{
    private final OutputStream outputStream;
    private final BlockingQueue<BgoProtocolWriter> bws;
    private boolean sendFailed;
    private boolean shutdownSender;

    public BlockingQueueSender(final OutputStream outputStream)
    {
        this.outputStream = outputStream;
        this.bws = new LinkedBlockingQueue<>();
        this.sendFailed = false;
        this.shutdownSender = false;
        start();
    }

    private void start()
    {
        log.info("Start BlockingQueueSender");
        Thread.ofVirtual().start(() ->
        {
            try
            {
                while (!shutdownSender)
                {
                    final BgoProtocolWriter bw = bws.poll(5 ,TimeUnit.SECONDS);
                    if (bw == null)
                    {
                        continue;
                    }

                    final long start = System.currentTimeMillis();
                    bw.writeTo(outputStream);

                    outputStream.flush();
                    final long end = System.currentTimeMillis();
                    if ((end-start) > TimeUnit.SECONDS.toMillis(5))
                    {
                        log.warn("SendCall delay was greater than 5 seconds!");
                    }
                }
            }
            catch (IOException ioException)
            {
                //log.warn("ioException in BlockingMode ", ioException);
                this.sendFailed = true;
            }
            catch (InterruptedException e)
            {
                log.error("issue in virtual thread sender", e);
            }
        });
    }

    @Override
    public boolean send(final BgoProtocolWriter bw)
    {
        if (sendFailed || shutdownSender)
            return false;

        final boolean result = bws.offer(bw);
        if (!result)
        {
            log.error("putting into queue did not work!");
            return false;
        }
        return true;
    }

    @Override
    public boolean send(final Collection<BgoProtocolWriter> bwsToSend)
    {
        for (BgoProtocolWriter bgoProtocolWriter : bwsToSend)
        {
            var rv = send(bgoProtocolWriter);
            if (!rv)
                return false;
        }
        return true;
    }

    @Override
    public void shutdown()
    {
        this.shutdownSender = true;
    }
}
