package net.gliby.voicechat.client.sound;

import javax.sound.sampled.AudioFormat;

class JitterBuffer
{
    private byte[] buffer;
    private final AudioFormat format;
    private int threshold;

    JitterBuffer(AudioFormat format, int jitter)
    {
        this.format = format;
        this.updateJitter(jitter);
    }

    void clearBuffer(int jitterSize)
    {
        this.buffer = new byte[0];
        this.updateJitter(jitterSize);
    }

    byte[] get()
    {
        return this.buffer;
    }

    private int getSizeInBytes(AudioFormat fmt, int size)
    {
        int s = (int)(fmt.getSampleRate() / 1000.0F);
        int sampleSize = (int)((float)(fmt.getSampleSizeInBits() / 8) * 0.49F);
        return sampleSize != 0?s * size / sampleSize:0;
    }

    boolean isReady()
    {
        return this.buffer.length > this.threshold;
    }

    void push(byte[] data)
    {
        this.write(data);
    }

    void updateJitter(int size)
    {
        this.threshold = this.getSizeInBytes(this.format, size);

        if (this.buffer == null)
        {
            this.buffer = this.threshold != 0?new byte[3 * this.threshold]:new byte[320];
        }
    }

    private void write(byte[] write)
    {
        byte[] result = new byte[this.buffer.length + write.length];
        System.arraycopy(this.buffer, 0, result, 0, this.buffer.length);
        System.arraycopy(write, 0, result, this.buffer.length, write.length);
        this.buffer = result;
    }
}