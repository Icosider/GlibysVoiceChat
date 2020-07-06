package net.gliby.voicechat.client.sound;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.thread.ThreadSoundQueue;
import net.gliby.voicechat.client.sound.thread.ThreadUpdateStream;
import net.gliby.voicechat.common.PlayerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundManager.SoundSystemStarterThread;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import org.lwjgl.util.vector.Vector3f;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientStreamManager {
    static AudioFormat universalAudioFormat = new AudioFormat(Encoding.PCM_SIGNED, 16000.0F, 16, 1, 2, 16000.0F, false);
    public static Map<Integer, String> playerMutedData = new HashMap<>();
    public List<ClientStream> currentStreams = new ArrayList<>();
    public List<Integer> playersMuted = new ArrayList<>();
    public ConcurrentLinkedQueue<Datalet> queue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<Integer, ClientStream> streaming = new ConcurrentHashMap<>();
    private final SoundPreProcessor soundPreProcessor;
    public ConcurrentHashMap<Integer, PlayerProxy> playerData = new ConcurrentHashMap<>();
    private Thread threadUpdate;
    private ThreadSoundQueue threadQueue;
    private final Minecraft mc;
    private final VoiceChatClient voiceChat;
    private boolean volumeControlActive;
    private float WEATHER;
    private float RECORDS;
    private float BLOCKS;
    private float MOBS;
    private float ANIMALS;

    public static AudioFormat getUniversalAudioFormat() {
        return universalAudioFormat;
    }

    public ClientStreamManager(Minecraft mc, VoiceChatClient voiceChatClient) {
        this.mc = mc;
        this.voiceChat = voiceChatClient;
        this.soundPreProcessor = new SoundPreProcessor(voiceChatClient, mc);
    }

    void addQueue(byte[] decoded_data, boolean global, int id, byte volume) {
        if (!this.playersMuted.contains(id)) {
            this.queue.offer(new Datalet(global, id, decoded_data, volume));

            synchronized (this.threadQueue) {
                this.threadQueue.notify();
            }
        }
    }

    private void addStreamSafe(ClientStream stream) {
        this.streaming.put(stream.id, stream);

        synchronized (this.threadUpdate) {
            this.threadUpdate.notify();
        }

        String entityName = stream.player.entityName();

        for (int streams = 0; streams < this.voiceChat.getTestPlayers().length; ++streams) {
            String name = this.voiceChat.getTestPlayers()[streams];

            if (stream.player.equals(name)) {
                stream.special = 2;
            }
        }

        if (this.voiceChat.specialPlayers.containsKey(entityName)) {
            stream.special = this.voiceChat.specialPlayers.get(entityName);
        }

        if (!this.containsStream(stream.id)) {
            List<ClientStream> arr = new ArrayList<>(this.currentStreams);
            arr.add(stream);
            arr.sort(new ClientStream.PlayableStreamComparator());
            this.currentStreams.removeAll(this.currentStreams);
            this.currentStreams.addAll(arr);
        }
    }

    public void alertEnd(int id) {
        if (!this.playersMuted.contains(id)) {
            this.queue.offer(new Datalet(false, id, null, (byte) 0));

            synchronized (this.threadQueue) {
                this.threadQueue.notify();
            }
        }
    }

    private boolean containsStream(int id) {
        ClientStream currentStream = this.streaming.get(id);

        for (ClientStream stream : this.currentStreams) {
            String currentName = currentStream.player.entityName();
            String otherName = stream.player.entityName();

            if (stream.player.entityName() != null && currentStream.player.entityName() != null && currentName.equals(otherName))
                return true;

            if (stream.id == id)
                return true;
        }
        return false;
    }

    public void createStream(Datalet data) {
        SoundSystemStarterThread sndSystem = this.mc.getSoundHandler().sndManager.sndSystem;
        String identifier = this.generateSource(data.id);
        PlayerProxy player = this.getPlayerData(data.id);

        if (data.direct) {
            Vector3f position = player.position();
            sndSystem.rawDataStream(universalAudioFormat, true, identifier, position.x, position.y, position.z, 2, (float) this.voiceChat.getSettings().getSoundDistance());
        } else
            sndSystem.rawDataStream(universalAudioFormat, true, identifier, (float) this.mc.player.posX, (float) this.mc.player.posY, (float) this.mc.player.posZ, 2, (float) this.voiceChat.getSettings().getSoundDistance());

        sndSystem.setPitch(identifier, 1.0F);

        if (data.volume != -1) {
            sndSystem.setVolume(identifier, this.voiceChat.getSettings().getWorldVolume() * (float) data.volume);
        } else {
            sndSystem.setVolume(identifier, this.voiceChat.getSettings().getWorldVolume());
        }
        this.addStreamSafe(new ClientStream(player, data.id, data.direct));
        this.giveStream(data);
    }

    private String generateSource(int let) {
        return "" + let;
    }

    private PlayerProxy getPlayerData(int entityId) {
        PlayerProxy proxy = this.playerData.get(entityId);
        EntityPlayer entity = (EntityPlayer) this.mc.world.getEntityByID(entityId);

        if (proxy == null) {
            if (entity != null) {
                proxy = new PlayerProxy(entity, entity.getEntityId(), entity.getName(), entity.posX, entity.posY, entity.posZ);
            } else {
                VoiceChat.getLogger().error("Major error, no entity found for player.");
                proxy = new PlayerProxy(null, entityId, "" + entityId, 0.0D, 0.0D, 0.0D);
            }
            this.playerData.put(entityId, proxy);
        } else if (entity != null) {
            proxy.setPlayer(entity);
            proxy.setName(entity.getName());
        }
        return proxy;
    }

    public SoundPreProcessor getSoundPreProcessor() {
        return this.soundPreProcessor;
    }

    public void giveEnd(int id) {
        ClientStream stream = this.streaming.get(id);

        if (stream != null)
            stream.needsEnd = true;
    }

    public void giveStream(Datalet data) {
        SoundSystemStarterThread sndSystem = this.mc.getSoundHandler().sndManager.sndSystem;
        ClientStream stream = this.streaming.get(data.id);

        if (stream != null) {
            String identifier = this.generateSource(data.id);
            stream.update(data, (int) (System.currentTimeMillis() - stream.lastUpdated));
            stream.buffer.push(data.data);
            stream.buffer.updateJitter(stream.getJitterRate());

            if (stream.buffer.isReady() || stream.needsEnd) {
                sndSystem.flush(identifier);
                sndSystem.feedRawAudioData(identifier, stream.buffer.get());
                stream.buffer.clearBuffer(stream.getJitterRate());
            }
            stream.lastUpdated = System.currentTimeMillis();
        }
    }

    public void init() {
        Thread thread = new Thread(this.threadQueue = new ThreadSoundQueue(this), "Client Stream Queue");
        thread.start();
        this.threadUpdate = new Thread(new ThreadUpdateStream(this, this.voiceChat), "Client Stream Updater");
        this.threadUpdate.start();
    }

    public void killStream(ClientStream stream) {
        if (stream != null) {
            List<ClientStream> streams = new ArrayList<>(this.currentStreams);
            streams.remove(stream);
            streams.sort(new ClientStream.PlayableStreamComparator());
            this.currentStreams.removeAll(this.currentStreams);
            this.currentStreams.addAll(streams);
            this.currentStreams.remove(stream);
            this.currentStreams.sort(new ClientStream.PlayableStreamComparator());
            this.streaming.remove(stream.id);
        }
    }

    public boolean newDatalet(Datalet let) {
        return !this.streaming.containsKey(let.id);
    }

    public void reload() {
        if (!this.currentStreams.isEmpty()) {
            VoiceChatClient.getLogger().info("Reloading SoundManager, removing all active streams.");

            for (ClientStream stream : this.currentStreams) {
                this.killStream(stream);
            }
        }
    }

    public void reset() {
        this.voiceChat.setRecorderActive(false);
        this.voiceChat.recorder.stop();
        this.volumeControlStop();
        this.queue.clear();
        this.streaming.clear();
        this.currentStreams.clear();
        this.playerData.clear();
    }

    public void volumeControlStart() {
        if (!(this.mc.currentScreen instanceof GuiScreenOptionsSounds) && !this.volumeControlActive) {
            this.WEATHER = this.mc.gameSettings.getSoundLevel(SoundCategory.WEATHER);
            this.RECORDS = this.mc.gameSettings.getSoundLevel(SoundCategory.RECORDS);
            this.BLOCKS = this.mc.gameSettings.getSoundLevel(SoundCategory.BLOCKS);
            this.MOBS = this.mc.gameSettings.getSoundLevel(SoundCategory.HOSTILE);
            this.ANIMALS = this.mc.gameSettings.getSoundLevel(SoundCategory.PLAYERS);

            if (this.mc.gameSettings.getSoundLevel(SoundCategory.WEATHER) > 0.15F)
                this.mc.gameSettings.setSoundLevel(SoundCategory.WEATHER, 0.15F);

            if (this.mc.gameSettings.getSoundLevel(SoundCategory.RECORDS) > 0.15F)
                this.mc.gameSettings.setSoundLevel(SoundCategory.RECORDS, 0.15F);

            if (this.mc.gameSettings.getSoundLevel(SoundCategory.BLOCKS) > 0.15F)
                this.mc.gameSettings.setSoundLevel(SoundCategory.BLOCKS, 0.15F);

            if (this.mc.gameSettings.getSoundLevel(SoundCategory.HOSTILE) > 0.15F)
                this.mc.gameSettings.setSoundLevel(SoundCategory.HOSTILE, 0.15F);

            if (this.mc.gameSettings.getSoundLevel(SoundCategory.NEUTRAL) > 0.15F)
                this.mc.gameSettings.setSoundLevel(SoundCategory.NEUTRAL, 0.15F);
            this.volumeControlActive = true;
        }
    }

    public void volumeControlStop() {
        if (this.volumeControlActive) {
            this.mc.gameSettings.setSoundLevel(SoundCategory.WEATHER, this.WEATHER);
            this.mc.gameSettings.setSoundLevel(SoundCategory.RECORDS, this.RECORDS);
            this.mc.gameSettings.setSoundLevel(SoundCategory.BLOCKS, this.BLOCKS);
            this.mc.gameSettings.setSoundLevel(SoundCategory.HOSTILE, this.MOBS);
            this.mc.gameSettings.setSoundLevel(SoundCategory.NEUTRAL, this.ANIMALS);
            this.volumeControlActive = false;
        }
    }
}