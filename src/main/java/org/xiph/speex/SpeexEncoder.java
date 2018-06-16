package org.xiph.speex;

public class SpeexEncoder {

   public static final String VERSION = "Java Speex Encoder v0.9.7 ($Revision: 1.6 $)";
   private Encoder encoder;
   private Bits bits = new Bits();
   private float[] rawData;
   private int sampleRate;
   private int channels;
   private int frameSize;


   public boolean init(int var1, int var2, int var3, int var4) {
      switch(var1) {
      case 0:
         this.encoder = new NbEncoder();
         ((NbEncoder)this.encoder).nbinit();
         break;
      case 1:
         this.encoder = new SbEncoder();
         ((SbEncoder)this.encoder).wbinit();
         break;
      case 2:
         this.encoder = new SbEncoder();
         ((SbEncoder)this.encoder).uwbinit();
         break;
      default:
         return false;
      }

      this.encoder.setQuality(var2);
      this.frameSize = this.encoder.getFrameSize();
      this.sampleRate = var3;
      this.channels = var4;
      this.rawData = new float[var4 * this.frameSize];
      this.bits.init();
      return true;
   }

   public Encoder getEncoder() {
      return this.encoder;
   }

   public int getSampleRate() {
      return this.sampleRate;
   }

   public int getChannels() {
      return this.channels;
   }

   public int getFrameSize() {
      return this.frameSize;
   }

   public int getProcessedData(byte[] var1, int var2) {
      int var3 = this.bits.getBufferSize();
      System.arraycopy(this.bits.getBuffer(), 0, var1, var2, var3);
      this.bits.init();
      return var3;
   }

   public int getProcessedDataByteSize() {
      return this.bits.getBufferSize();
   }

   public boolean processData(byte[] var1, int var2, int var3) {
      mapPcm16bitLittleEndian2Float(var1, var2, this.rawData, 0, var3 / 2);
      return this.processData(this.rawData, var3 / 2);
   }

   public boolean processData(short[] var1, int var2, int var3) {
      int var4 = this.channels * this.frameSize;
      if(var3 != var4) {
         throw new IllegalArgumentException("SpeexEncoder requires " + var4 + " samples to process a Frame, not " + var3);
      } else {
         for(int var5 = 0; var5 < var3; ++var5) {
            this.rawData[var5] = (float)var1[var2 + var5];
         }

         return this.processData(this.rawData, var3);
      }
   }

   public boolean processData(float[] var1, int var2) {
      int var3 = this.channels * this.frameSize;
      if(var2 != var3) {
         throw new IllegalArgumentException("SpeexEncoder requires " + var3 + " samples to process a Frame, not " + var2);
      } else {
         if(this.channels == 2) {
            Stereo.encode(this.bits, var1, this.frameSize);
         }

         this.encoder.encode(this.bits, var1);
         return true;
      }
   }

   public static void mapPcm16bitLittleEndian2Float(byte[] var0, int var1, float[] var2, int var3, int var4) {
      if(var0.length - var1 < 2 * var4) {
         throw new IllegalArgumentException("Insufficient Samples to convert to floats");
      } else if(var2.length - var3 < var4) {
         throw new IllegalArgumentException("Insufficient float buffer to convert the samples");
      } else {
         for(int var5 = 0; var5 < var4; ++var5) {
            var2[var3 + var5] = (float)(var0[var1 + 2 * var5] & 255 | var0[var1 + 2 * var5 + 1] << 8);
         }

      }
   }
}
