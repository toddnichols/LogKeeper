
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

import java.math.*;
import org.tritonus.share.sampled.TConversionTool;

public class ConcatenatingAudioInputStream extends AudioInputStream {

    AudioInputStream[] m_streams = null;
    long[] frame_lengths = null;
    int cur_stream = 0;
    int marked_stream = 0;

    public ConcatenatingAudioInputStream(AudioInputStream[] streams) {
        super(new ByteArrayInputStream(new byte[0]), streams[0].getFormat(), AudioSystem.NOT_SPECIFIED);

        m_streams = streams;
        frame_lengths = new long[m_streams.length];

        // Populate the frame_lengths table to hold the length of each stream
        // Reinitialize the frameLength property to contain the sum of each stream's length
        this.frameLength = 0;
        for (int i = 0; i < m_streams.length; i++) {
            frame_lengths[i] = m_streams[i].getFrameLength();
            this.frameLength += frame_lengths[i];
        }
    }

    public int read() throws IOException {
        // Read the next byte
        int read_data = m_streams[cur_stream].read();
        // If we reached the end of the current stream
        if (read_data == -1) {
            // Advance to the next stream
            cur_stream++;
            // If there are more streams to read
            if (cur_stream < m_streams.length) {
                return read();
            }
        }
        return read_data;
    }

    public int read(byte[] b) throws IOException {
        // Read bytes into the buffer
        int read_data = m_streams[cur_stream].read(b);
        // If we reached the end of the current stream
        if (read_data == -1) {
            // Advance to the next stream
            cur_stream++;
            if (cur_stream < m_streams.length) {
                return read(b);
            }
        }
        return read_data;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        // Read bytes into the buffer
        int read_data = m_streams[cur_stream].read(b, off, len);
        // If we reached the end of the current stream
        if (read_data == -1) {
            // Advance to the next stream
            cur_stream++;
            if (cur_stream < m_streams.length) {
                return read(b, off, len);
            }
        }
        return read_data;
    }

    public long skip(long len) throws IOException {
        long skipped = m_streams[cur_stream].skip(len);
        if (skipped < 1) {
            cur_stream++;
            if (cur_stream < m_streams.length) {
                return skip(len);
            }
        }
        return skipped;
    }

    public void mark(int readlimit) {
        marked_stream = cur_stream;
        m_streams[cur_stream].mark(readlimit);
    }

    public boolean markSupported() {
        return true;
    }

    public void reset() throws IOException {
        // Reset the current stream to the marked stream
        cur_stream = marked_stream;
        // Starting with the current stream, reset positions on all streams following
        for (int i = cur_stream; i < m_streams.length; i++) {
            m_streams[i].reset();
        }
    }
}


