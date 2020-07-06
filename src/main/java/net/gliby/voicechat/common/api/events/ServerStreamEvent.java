package net.gliby.voicechat.common.api.events;

import net.gliby.voicechat.common.networking.ServerDatalet;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ServerStreamEvent extends Event {
    public ServerStream stream;
    public ServerStreamManager streamManager;

    ServerStreamEvent(ServerStreamManager serverStreamManager, ServerStream stream) {
        this.stream = stream;
        this.streamManager = serverStreamManager;
    }

    public static class StreamCreated extends ServerStreamEvent {
        public ServerDatalet voiceLet;

        public StreamCreated(ServerStreamManager serverStreamManager, ServerStream stream, ServerDatalet let) {
            super(serverStreamManager, stream);
            this.voiceLet = let;
        }
    }

    public static class StreamFeed extends ServerStreamEvent {
        public ServerDatalet voiceLet;

        public StreamFeed(ServerStreamManager serverStreamManager, ServerStream stream, ServerDatalet voiceLet) {
            super(serverStreamManager, stream);
            this.voiceLet = voiceLet;
        }
    }

    public static class StreamDestroyed extends ServerStreamEvent {
        public StreamDestroyed(ServerStreamManager serverStreamManager, ServerStream stream) {
            super(serverStreamManager, stream);
        }
    }
}