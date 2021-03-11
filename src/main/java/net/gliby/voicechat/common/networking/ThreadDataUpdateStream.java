package net.gliby.voicechat.common.networking;

public class ThreadDataUpdateStream implements Runnable {
    private final ServerStreamManager dataManager;

    ThreadDataUpdateStream(ServerStreamManager dataManager) {
        this.dataManager = dataManager;
    }

    public void run() {
        while (this.dataManager.running) {
            if (!this.dataManager.currentStreams.isEmpty()) {
                for (ServerStream stream : this.dataManager.currentStreams) {
                    final int duration = stream.getLastTimeUpdated();

                    if (duration > 350 && duration > stream.player.ping * 2)
                        this.dataManager.killStream(stream);
                }
            }

            try {
                synchronized (this) {
                    this.wait(12L);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}