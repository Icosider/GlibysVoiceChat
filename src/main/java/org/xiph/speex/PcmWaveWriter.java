package org.xiph.speex;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PcmWaveWriter extends AudioFileWriter {

   public static final short WAVE_FORMAT_PCM = 1;
   public static final short WAVE_FORMAT_SPEEX = -24311;
   public static final int[][][] WAVE_FRAME_SIZES = new int[][][]{{{8, 8, 8, 1, 1, 2, 2, 2, 2, 2, 2}, {2, 1, 1, 7, 7, 8, 8, 8, 8, 3, 3}}, {{8, 8, 8, 2, 1, 1, 2, 2, 2, 2, 2}, {1, 2, 2, 8, 7, 6, 3, 3, 3, 3, 3}}, {{8, 8, 8, 1, 2, 2, 1, 1, 1, 1, 1}, {2, 1, 1, 7, 8, 3, 6, 6, 5, 5, 5}}};
   public static final int[][][] WAVE_BITS_PER_FRAME = new int[][][]{{{43, 79, 119, 160, 160, 220, 220, 300, 300, 364, 492}, {60, 96, 136, 177, 177, 237, 237, 317, 317, 381, 509}}, {{79, 115, 155, 196, 256, 336, 412, 476, 556, 684, 844}, {96, 132, 172, 213, 273, 353, 429, 493, 573, 701, 861}}, {{83, 151, 191, 232, 292, 372, 448, 512, 592, 720, 880}, {100, 168, 208, 249, 309, 389, 465, 529, 609, 737, 897}}};
   private RandomAccessFile raf;
   private int mode;
   private int quality;
   private int sampleRate;
   private int channels;
   private int nframes;
   private boolean vbr;
   private int size;
   private boolean isPCM;


   public PcmWaveWriter() {
      this.size = 0;
   }

   public PcmWaveWriter(int var1, int var2) {
      this();
      this.setPCMFormat(var1, var2);
   }

   public PcmWaveWriter(int var1, int var2, int var3, int var4, int var5, boolean var6) {
      this();
      this.setSpeexFormat(var1, var2, var3, var4, var5, var6);
   }

   private void setPCMFormat(int var1, int var2) {
      this.channels = var2;
      this.sampleRate = var1;
      this.isPCM = true;
   }

   private void setSpeexFormat(int var1, int var2, int var3, int var4, int var5, boolean var6) {
      this.mode = var1;
      this.quality = var2;
      this.sampleRate = var3;
      this.channels = var4;
      this.nframes = var5;
      this.vbr = var6;
      this.isPCM = false;
   }

   public void close() throws IOException {
      this.raf.seek(4L);
      int var1 = (int)this.raf.length() - 8;
      writeInt(this.raf, var1);
      this.raf.seek(40L);
      writeInt(this.raf, this.size);
      this.raf.close();
   }

   public void open(File var1) throws IOException {
      var1.delete();
      this.raf = new RandomAccessFile(var1, "rw");
      this.size = 0;
   }

   public void open(String var1) throws IOException {
      this.open(new File(var1));
   }

   public void writeHeader(String var1) throws IOException {
      byte[] var2 = "RIFF".getBytes();
      this.raf.write(var2, 0, var2.length);
      writeInt(this.raf, 0);
      var2 = "WAVE".getBytes();
      this.raf.write(var2, 0, var2.length);
      var2 = "fmt ".getBytes();
      this.raf.write(var2, 0, var2.length);
      if(this.isPCM) {
         writeInt(this.raf, 16);
         writeShort(this.raf, (short)1);
         writeShort(this.raf, (short)this.channels);
         writeInt(this.raf, this.sampleRate);
         writeInt(this.raf, this.sampleRate * this.channels * 2);
         writeShort(this.raf, (short)(this.channels * 2));
         writeShort(this.raf, (short)16);
      } else {
         int var3 = var1.length();
         writeInt(this.raf, (short)(100 + var3));
         writeShort(this.raf, (short)-24311);
         writeShort(this.raf, (short)this.channels);
         writeInt(this.raf, this.sampleRate);
         writeInt(this.raf, calculateEffectiveBitrate(this.mode, this.channels, this.quality) + 7 >> 3);
         writeShort(this.raf, (short)calculateBlockSize(this.mode, this.channels, this.quality));
         writeShort(this.raf, (short)this.quality);
         writeShort(this.raf, (short)(82 + var3));
         this.raf.writeByte(1);
         this.raf.writeByte(0);
         this.raf.write(buildSpeexHeader(this.sampleRate, this.mode, this.channels, this.vbr, this.nframes));
         this.raf.writeBytes(var1);
      }

      var2 = "data".getBytes();
      this.raf.write(var2, 0, var2.length);
      writeInt(this.raf, 0);
   }

   public void writePacket(byte[] var1, int var2, int var3) throws IOException {
      this.raf.write(var1, var2, var3);
      this.size += var3;
   }

   private static final int calculateEffectiveBitrate(int var0, int var1, int var2) {
      return (WAVE_FRAME_SIZES[var0 - 1][var1 - 1][var2] * WAVE_BITS_PER_FRAME[var0 - 1][var1 - 1][var2] + 7 >> 3) * 50 * 8 / WAVE_BITS_PER_FRAME[var0 - 1][var1 - 1][var2];
   }

   private static final int calculateBlockSize(int var0, int var1, int var2) {
      return WAVE_FRAME_SIZES[var0 - 1][var1 - 1][var2] * WAVE_BITS_PER_FRAME[var0 - 1][var1 - 1][var2] + 7 >> 3;
   }

}
