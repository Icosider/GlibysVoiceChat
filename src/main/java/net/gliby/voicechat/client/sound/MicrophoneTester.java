package net.gliby.voicechat.client.sound;

import net.gliby.voicechat.client.VoiceChatClient;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MicrophoneTester implements Runnable {
    private Thread thread;
    public boolean recording;
    private final VoiceChatClient voiceChat;
    public float currentAmplitude;

    public MicrophoneTester(VoiceChatClient voiceChat) {
        this.voiceChat = voiceChat;
    }

    private byte[] boostVolume(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer newBuf = ByteBuffer.allocate(data.length).order(ByteOrder.LITTLE_ENDIAN);

        while (buf.hasRemaining()) {
            int sample = buf.getShort();
            sample *= 1 + (int) (this.voiceChat.getSettings().getInputBoost() * 5.0F);
            newBuf.putShort((short) (sample));
        }
        return newBuf.array();
    }

    public Thread getThread() {
        return this.thread;
    }

    public void run() {
        this.voiceChat.setRecorderActive(false);
        this.voiceChat.recorder.stop();
        TargetDataLine line = this.voiceChat.getSettings().getInputDevice().getLine();

        if (line == null) {
            VoiceChatClient.getLogger().fatal("No line in found, cannot test input device.");
        } else {
            Info sourceInfo = new Info(SourceDataLine.class, ClientStreamManager.universalAudioFormat);

            try {
                line.open(ClientStreamManager.universalAudioFormat);
                line.start();
                SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
                sourceLine.open(ClientStreamManager.universalAudioFormat);
                sourceLine.start();
                byte[] targetData = new byte[line.getBufferSize() / 5];

                while (this.recording) {
                    int numBytesRead = line.read(targetData, 0, targetData.length);

                    if (numBytesRead == -1) {
                        break;
                    }

                    byte[] boostedTargetData = this.boostVolume(targetData);
                    sourceLine.write(boostedTargetData, 0, numBytesRead);
                    double sum = 0.0D;

                    for (int i = 0; i < numBytesRead; ++i) {
                        sum += boostedTargetData[i] * boostedTargetData[i];
                    }

                    if (numBytesRead > 0) {
                        this.currentAmplitude = (float) ((int) Math.sqrt(sum / (double) numBytesRead));
                    }
                }
                sourceLine.flush();
                sourceLine.close();
                line.flush();
                line.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        this.thread = new Thread(this, "Input Device Tester");
        this.recording = true;
        this.thread.start();
    }

    public void stop() {
        this.recording = false;
        this.thread = null;
    }

    public void toggle() {
        if (this.recording) {
            this.start();
        } else {
            this.stop();
        }
    }
}