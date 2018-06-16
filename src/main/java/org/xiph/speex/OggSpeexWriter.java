package org.xiph.speex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class OggSpeexWriter extends AudioFileWriter {

   public static final int PACKETS_PER_OGG_PAGE = 250;
   private OutputStream out;
   private int mode;
   private int sampleRate;
   private int channels;
   private int nframes;
   private boolean vbr;
   private int size;
   private int streamSerialNumber;
   private byte[] dataBuffer;
   private int dataBufferPtr;
   private byte[] headerBuffer;
   private int headerBufferPtr;
   private int pageCount;
   private int packetCount;
   private long granulepos;


   public OggSpeexWriter() {
      if(this.streamSerialNumber == 0) {
         this.streamSerialNumber = (new Random()).nextInt();
      }

      this.dataBuffer = new byte[65565];
      this.dataBufferPtr = 0;
      this.headerBuffer = new byte[255];
      this.headerBufferPtr = 0;
      this.pageCount = 0;
      this.packetCount = 0;
      this.granulepos = 0L;
   }

   public OggSpeexWriter(int var1, int var2, int var3, int var4, boolean var5) {
      this();
      this.setFormat(var1, var2, var3, var4, var5);
   }

   private void setFormat(int var1, int var2, int var3, int var4, boolean var5) {
      this.mode = var1;
      this.sampleRate = var2;
      this.channels = var3;
      this.nframes = var4;
      this.vbr = var5;
   }

   public void setSerialNumber(int var1) {
      this.streamSerialNumber = var1;
   }

   public void close() throws IOException {
      this.flush(true);
      this.out.close();
   }

   public void open(File var1) throws IOException {
      var1.delete();
      this.out = new FileOutputStream(var1);
      this.size = 0;
   }

   public void open(String var1) throws IOException {
      this.open(new File(var1));
   }

   public void writeHeader(String var1) throws IOException {
      byte[] var3 = buildOggPageHeader(2, 0L, this.streamSerialNumber, this.pageCount++, 1, new byte[]{(byte)80});
      byte[] var4 = buildSpeexHeader(this.sampleRate, this.mode, this.channels, this.vbr, this.nframes);
      int var2 = OggCrc.checksum(0, var3, 0, var3.length);
      var2 = OggCrc.checksum(var2, var4, 0, var4.length);
      writeInt(var3, 22, var2);
      this.out.write(var3);
      this.out.write(var4);
      var3 = buildOggPageHeader(0, 0L, this.streamSerialNumber, this.pageCount++, 1, new byte[]{(byte)(var1.length() + 8)});
      var4 = buildSpeexComment(var1);
      var2 = OggCrc.checksum(0, var3, 0, var3.length);
      var2 = OggCrc.checksum(var2, var4, 0, var4.length);
      writeInt(var3, 22, var2);
      this.out.write(var3);
      this.out.write(var4);
   }

   public void writePacket(byte[] var1, int var2, int var3) throws IOException {
      if(var3 > 0) {
         if(this.packetCount > 250) {
            this.flush(false);
         }

         System.arraycopy(var1, var2, this.dataBuffer, this.dataBufferPtr, var3);
         this.dataBufferPtr += var3;
         this.headerBuffer[this.headerBufferPtr++] = (byte)var3;
         ++this.packetCount;
         this.granulepos += (long)(this.nframes * (this.mode == 2?640:(this.mode == 1?320:160)));
      }
   }

   private void flush(boolean var1) throws IOException {
      byte[] var3 = buildOggPageHeader(var1?4:0, this.granulepos, this.streamSerialNumber, this.pageCount++, this.packetCount, this.headerBuffer);
      int var2 = OggCrc.checksum(0, var3, 0, var3.length);
      var2 = OggCrc.checksum(var2, this.dataBuffer, 0, this.dataBufferPtr);
      writeInt(var3, 22, var2);
      this.out.write(var3);
      this.out.write(this.dataBuffer, 0, this.dataBufferPtr);
      this.dataBufferPtr = 0;
      this.headerBufferPtr = 0;
      this.packetCount = 0;
   }
}
