package io.github.luigeneric.binaryreaderwriter;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CustomFastByteArrayOutputStream extends OutputStream
{
    /**
     * The buffer where data is stored.
     */
    protected byte[] buffer;

    /**
     * The number of valid bytes in the buffer.
     */
    protected int count;
    protected boolean lengthWritten;
    protected final Lock lock;

    /**
     * Creates a new {@code ByteArrayOutputStream}. The buffer capacity is
     * initially 64 bytes, though its size increases if necessary.
     * Best for BSGO measured 10/02/2023(d/m/y)
     */
    public CustomFastByteArrayOutputStream()
    {
        this(64);
    }

    /**
     * Creates a new {@code ByteArrayOutputStream}, with a buffer capacity of
     * the specified size, in bytes.
     *
     * @param size the initial size.
     * @throws IllegalArgumentException if size is negative.
     */
    public CustomFastByteArrayOutputStream(final int size)
    {
        if (size < 0)
        {
            throw new IllegalArgumentException("Negative initial size: "
                    + size);
        }
        this.buffer = new byte[size];
        this.lengthWritten = false;
        this.lock = new ReentrantLock();
    }

    /**
     * Increases the capacity if necessary to ensure that it can hold
     * at least the number of elements specified by the minimum
     * capacity argument.
     *
     * @param minCapacity the desired minimum capacity.
     * @throws OutOfMemoryError if {@code minCapacity < 0} and
     *                          {@code minCapacity - buf.length > 0}.  This is interpreted as a
     *                          request for the unsatisfiably large capacity.
     *                          {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
     */
    private void ensureCapacity(final int minCapacity)
    {
        final int oldCapacity = buffer.length;
        final int minGrowth = minCapacity - oldCapacity;
        if (minGrowth > 0)
        {
            buffer = Arrays.copyOf(buffer, minCapacity);
        }
    }

    /**
     * Public accessible growth method to minimize unnecessary copy of the buffer
     *
     * @param delta (number-of-valid-bytes + delta) > free bytes
     */
    public void ensureDeltaCapacity(final int delta)
    {
        this.ensureCapacity(count + delta);
    }

    /**
     * Writes the specified byte to this {@code ByteArrayOutputStream}.
     *
     * @param b the byte to be written.
     */
    @Override
    public void write(final int b)
    {
        if (lengthWritten)
        {
            log.error("try to modify the buffer even thou it's length already written");
        }
        ensureCapacity(count + 1);
        buffer[count] = (byte) b;
        count += 1;
    }

    /**
     * Writes {@code len} bytes from the specified byte array
     * starting at offset {@code off} to this {@code ByteArrayOutputStream}.
     *
     * @param b   {@inheritDoc}
     * @param off {@inheritDoc}
     * @param len {@inheritDoc}
     * @throws NullPointerException      if {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException if {@code off} is negative,
     *                                   {@code len} is negative, or {@code len} is greater than
     *                                   {@code b.length - off}
     */
    @Override
    public void write(final byte[] b, final int off, final int len)
    {
        if (lengthWritten)
        {
            log.error("try to modify the buffer even thou it's length already written");
        }
        Objects.checkFromIndexSize(off, len, b.length);
        ensureCapacity(count + len);
        System.arraycopy(b, off, buffer, count, len);
        count += len;
    }

    @Override
    public void write(final byte... b)
    {
        this.write(b, 0, b.length);
    }

    /**
     * Writes the complete contents of this {@code ByteArrayOutputStream} to
     * the specified output stream argument, as if by calling the output
     * stream's write method using {@code out.write(buf, 0, count)}.
     *
     * @param out the output stream to which to write the data.
     * @throws NullPointerException if {@code out} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     */
    public void writeTo(final OutputStream out) throws IOException
    {
        writeDataLength();
        out.write(this.buffer, 0, this.count);
    }


    /**
     * Resets the {@code count} field of this {@code ByteArrayOutputStream}
     * to zero, so that all currently accumulated output in the
     * output stream is discarded. The output stream can be used again,
     * reusing the already allocated buffer space.
     */
    public void reset()
    {
        count = 0;
    }

    /**
     * Creates a newly allocated byte array. Its size is the current
     * size of this output stream and the valid contents of the buffer
     * have been copied into it.
     *
     * @return the current contents of this output stream, as a byte array.
     * @see java.io.ByteArrayOutputStream#size()
     */
    public byte[] toByteArray()
    {
        return Arrays.copyOf(buffer, count);
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return the value of the {@code count} field, which is the number
     * of valid bytes in this output stream.
     */
    public int size()
    {
        return count;
    }

    /**
     * Converts the buffer's contents into a string decoding bytes using the
     * default charset. The length of the new {@code String}
     * is a function of the charset, and hence may not be equal to the
     * size of the buffer.
     *
     * <p> This method always replaces malformed-input and unmappable-character
     * sequences with the default replacement string for the
     * default charset. The {@linkplain java.nio.charset.CharsetDecoder}
     * class should be used when more control over the decoding process is
     * required.
     *
     * @return String decoded from the buffer's contents.
     * @see Charset#defaultCharset()
     * @since 1.1
     */
    @Override
    public String toString()
    {
        return new String(buffer, 0, count);
    }

    /**
     * Converts the buffer's contents into a string by decoding the bytes using
     * the named {@link Charset charset}.
     *
     * <p> This method is equivalent to {@code #toString(charset)} that takes a
     * {@link Charset charset}.
     *
     * <p> An invocation of this method of the form
     *
     * <pre> {@code
     *      ByteArrayOutputStream b = ...
     *      b.toString("UTF-8")
     *      }
     * </pre>
     * <p>
     * behaves in exactly the same way as the expression
     *
     * <pre> {@code
     *      ByteArrayOutputStream b = ...
     *      b.toString(StandardCharsets.UTF_8)
     *      }
     * </pre>
     *
     * @param charsetName the name of a supported
     *                    {@link Charset charset}
     * @return String decoded from the buffer's contents.
     * @throws UnsupportedEncodingException If the named charset is not supported
     * @since 1.1
     */
    public String toString(final String charsetName)
            throws UnsupportedEncodingException
    {
        return new String(buffer, 0, count, charsetName);
    }

    /**
     * Converts the buffer's contents into a string by decoding the bytes using
     * the specified {@link Charset charset}. The length of the new
     * {@code String} is a function of the charset, and hence may not be equal
     * to the length of the byte array.
     *
     * <p> This method always replaces malformed-input and unmappable-character
     * sequences with the charset's default replacement string. The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     *
     * @param charset the {@linkplain Charset charset}
     *                to be used to decode the {@code bytes}
     * @return String decoded from the buffer's contents.
     * @since 10
     */
    public String toString(final Charset charset)
    {
        return new String(buffer, 0, count, charset);
    }

    /**
     * Creates a newly allocated string. Its size is the current size of
     * the output stream and the valid contents of the buffer have been
     * copied into it. Each character <i>c</i> in the resulting string is
     * constructed from the corresponding element <i>b</i> in the byte
     * array such that:
     * <blockquote><pre>{@code
     *     c == (char)(((hibyte & 0xff) << 8) | (b & 0xff))
     * }</pre></blockquote>
     *
     * @param hibyte the high byte of each resulting Unicode character.
     * @return the current contents of the output stream, as a string.
     * @see java.io.ByteArrayOutputStream#size()
     * @see java.io.ByteArrayOutputStream#toString(String)
     * @see java.io.ByteArrayOutputStream#toString()
     * @see Charset#defaultCharset()
     * @deprecated This method does not properly convert bytes into characters.
     * As of JDK&nbsp;1.1, the preferred way to do this is via the
     * {@link #toString(String charsetName)} or {@link #toString(Charset charset)}
     * method, which takes an encoding-name or charset argument,
     * or the {@code toString()} method, which uses the default charset.
     */
    @Deprecated
    public String toString(final int hibyte)
    {
        return new String(buffer, hibyte, 0, count);
    }

    /**
     * Closing a {@code ByteArrayOutputStream} has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an {@code IOException}.
     */
    @Override
    public void close() throws IOException
    {
    }

    /**
     * Writes the length into the first two bytes
     */
    protected void writeDataLength()
    {
        lock.lock();
        try
        {
            if (this.lengthWritten)
            {
                //log.info("length was already written!");
                return;
            }
            final int len = this.size() - 2;
            buffer[0] = (byte) (len >> 8 & 255);
            buffer[1] = (byte) (len & 255);
            this.lengthWritten = true;
        }
        finally
        {
            lock.unlock();
        }
    }
}
