package net.gliby.voicechat.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ModPackSettings {
    public ModPackSettings.GVCModPackInstructions init() throws UnsupportedEncodingException {
        InputStreamReader reader = new InputStreamReader(this.getClass().getResourceAsStream("/modpack.json"), StandardCharsets.UTF_8);
        Gson gson = (new GsonBuilder()).create();
        return gson.fromJson(reader, GVCModPackInstructions.class);
    }

    public class UIContainer {
        public float X;
        public float Y;
        public float SCALE;
        public int TYPE;

        public String toString() {
            return "X: " + this.X + ", Y: " + this.Y + ", SCALE: " + this.SCALE + ", TYPE: " + this.TYPE;
        }
    }

    public class GVCModPackInstructions {
        public ModPackSettings.UIContainer VOICE_PLATE;
        public ModPackSettings.UIContainer SPEAK_ICON;
        public boolean VOLUME_CONTROL;
        public boolean SHOW_PLATES;
        public boolean SHOW_PLAYER_ICONS;
        public float WORLD_VOLUME;
        public float UI_OPACITY;
        public int ID;

        public String toString() {
            return "PLATES: " + this.VOICE_PLATE + ", SPEAK ICON: " + this.SPEAK_ICON + ", WORLD VOLUME: " + this.WORLD_VOLUME + ", UI OPACITY: " + this.UI_OPACITY + ", VOLUME CONTROL: " + this.VOLUME_CONTROL + ", PLATES ENABLED: " + this.SHOW_PLATES + ", ICONS ENABLED: " + this.SHOW_PLAYER_ICONS;
        }
    }
}