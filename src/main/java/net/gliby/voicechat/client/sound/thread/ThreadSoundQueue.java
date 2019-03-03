package net.gliby.voicechat.client.sound.thread;

import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.client.sound.Datalet;

public class ThreadSoundQueue implements Runnable {
    private final ClientStreamManager sndManager;

    public ThreadSoundQueue(ClientStreamManager sndManager) {
        this.sndManager = sndManager;
    }

    @Override
    public void run() {
        while (true) {
            if (!this.sndManager.queue.isEmpty()) {
                Datalet e = this.sndManager.queue.poll();

                if (e != null) {
                    boolean end = e.data == null;

                    if (this.sndManager.newDatalet(e) && !end) {
                        this.sndManager.createStream(e);
                    } else if (end) {
                        this.sndManager.giveEnd(e.id);
                    } else {
                        this.sndManager.giveStream(e);
                    }
                }
            } else {
                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}