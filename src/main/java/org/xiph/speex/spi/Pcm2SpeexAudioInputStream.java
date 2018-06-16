package org.xiph.speex.spi;

import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.Encoder;
import org.xiph.speex.OggCrc;
import org.xiph.speex.SpeexEncoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.util.Random;

public class Pcm2SpeexAudioInputStream extends FilteredAudioInputStream {

   public static final int DEFAULT_BUFFER_SIZE = 2560;
   public static final int DEFAULT_SAMPLERATE = 8000;
   public static final int DEFAULT_CHANNELS = 1;
   public static final int DEFAULT_QUALITY = 3;
   public static final int DEFAULT_FRAMES_PER_PACKET = 1;
   public static final int DEFAULT_PACKETS_PER_OGG_PAGE = 20;
   public static final int UNKNOWN = -1;
   private SpeexEncoder encoder;
   private int mode;
   private int frameSize;
   private int framesPerPacket;
   private int channels;
   private String comment;
   private int granulepos;
   private int streamSerialNumber;
   private int packetsPerOggPage;
   private int packetCount;
   private int pageCount;
   private int oggCount;
   private boolean first;


   public Pcm2SpeexAudioInputStream(InputStream var1, AudioFormat var2, long var3) {
      this(-1, -1, var1, var2, var3, 2560);
   }

   public Pcm2SpeexAudioInputStream(int var1, int var2, InputStream var3, AudioFormat var4, long var5) {
      this(var1, var2, var3, var4, var5, 2560);
   }

   public Pcm2SpeexAudioInputStream(InputStream var1, AudioFormat var2, long var3, int var5) {
      this(-1, -1, var1, var2, var3, var5);
   }

   public Pcm2SpeexAudioInputStream(int var1, int var2, InputStream var3, AudioFormat var4, long var5, int var7) {
      super(var3, var4, var5, var7);
      this.comment = null;
      this.granulepos = 0;
      if(this.streamSerialNumber == 0) {
         this.streamSerialNumber = (new Random()).nextInt();
      }

      this.packetsPerOggPage = 20;
      this.packetCount = 0;
      this.pageCount = 0;
      this.framesPerPacket = 1;
      int var8 = (int)var4.getSampleRate();
      if(var8 < 0) {
         var8 = 8000;
      }

      this.channels = var4.getChannels();
      if(this.channels < 0) {
         this.channels = 1;
      }

      if(var1 < 0) {
         var1 = var8 < 12000?0:(var8 < 24000?1:2);
      }

      this.mode = var1;
      Encoding var9 = var4.getEncoding();
      if(var2 < 0) {
         if(var9 instanceof SpeexEncoding) {
            var2 = ((SpeexEncoding)var9).getQuality();
         } else {
            var2 = 3;
         }
      }

      this.encoder = new SpeexEncoder();
      this.encoder.init(var1, var2, var8, this.channels);
      if(var9 instanceof SpeexEncoding && ((SpeexEncoding)var9).isVBR()) {
         this.setVbr(true);
      } else {
         this.setVbr(false);
      }

      this.frameSize = 2 * this.channels * this.encoder.getFrameSize();
      this.comment = "Encoded with Java Speex Encoder v0.9.7 ($Revision: 1.6 $)";
      this.first = true;
   }

   public void setSerialNumber(int var1) {
      if(this.first) {
         this.streamSerialNumber = var1;
      }

   }

   public void setFramesPerPacket(int var1) {
      if(var1 <= 0) {
         var1 = 1;
      }

      this.framesPerPacket = var1;
   }

   public void setPacketsPerOggPage(int var1) {
      if(var1 <= 0) {
         var1 = 20;
      }

      if(var1 > 255) {
         var1 = 255;
      }

      this.packetsPerOggPage = var1;
   }

   public void setComment(String var1, boolean var2) {
      this.comment = var1;
      if(var2) {
         this.comment = this.comment + "Java Speex Encoder v0.9.7 ($Revision: 1.6 $)";
      }

   }

   public void setQuality(int var1) {
      this.encoder.getEncoder().setQuality(var1);
      if(this.encoder.getEncoder().getVbr()) {
         this.encoder.getEncoder().setVbrQuality((float)var1);
      }

   }

   public void setVbr(boolean var1) {
      this.encoder.getEncoder().setVbr(var1);
   }

   public Encoder getEncoder() {
      return this.encoder.getEncoder();
   }

   protected void fill() throws IOException {
      this.makeSpace();
      if(this.first) {
         this.writeHeaderFrames();
         this.first = false;
      }

      while(true) {
         int var1;
         if(super.prebuf.length - super.prepos < this.framesPerPacket * this.frameSize * this.packetsPerOggPage) {
            var1 = super.prepos + this.framesPerPacket * this.frameSize * this.packetsPerOggPage;
            byte[] var2 = new byte[var1];
            System.arraycopy(super.prebuf, 0, var2, 0, super.precount);
            super.prebuf = var2;
         }

         var1 = super.in.read(super.prebuf, super.precount, super.prebuf.length - super.precount);
         int var3;
         byte[] var4;
         int var5;
         if(var1 < 0) {
            if((super.precount - super.prepos) % 2 != 0) {
               throw new StreamCorruptedException("Incompleted last PCM sample when stream ended");
            }

            do {
               if(super.prepos >= super.precount) {
                  if(this.packetCount > 0) {
                     super.buf[super.count + 5] = 4;
                     super.buf[super.count + 26] = (byte)(255 & this.packetCount);
                     System.arraycopy(super.buf, super.count + 27 + this.packetsPerOggPage, super.buf, super.count + 27 + this.packetCount, this.oggCount - (super.count + 27 + this.packetsPerOggPage));
                     this.oggCount -= this.packetsPerOggPage - this.packetCount;
                     this.writeOggPageChecksum();
                  }

                  return;
               }

               if(super.precount - super.prepos < this.framesPerPacket * this.frameSize) {
                  while(super.precount < super.prepos + this.framesPerPacket * this.frameSize) {
                     super.prebuf[super.precount] = 0;
                     ++super.precount;
                  }
               }

               if(this.packetCount == 0) {
                  this.writeOggPageHeader(this.packetsPerOggPage, 0);
               }

               for(var5 = 0; var5 < this.framesPerPacket; ++var5) {
                  this.encoder.processData(super.prebuf, super.prepos, this.frameSize);
                  super.prepos += this.frameSize;
               }

               for(var5 = this.encoder.getProcessedDataByteSize(); super.buf.length - this.oggCount < var5; super.buf = var4) {
                  var3 = super.buf.length * 2;
                  var4 = new byte[var3];
                  System.arraycopy(super.buf, 0, var4, 0, this.oggCount);
               }

               super.buf[super.count + 27 + this.packetCount] = (byte)(255 & var5);
               this.encoder.getProcessedData(super.buf, this.oggCount);
               this.oggCount += var5;
               ++this.packetCount;
            } while(this.packetCount < this.packetsPerOggPage);

            this.writeOggPageChecksum();
            return;
         }

         if(var1 > 0) {
            super.precount += var1;
            if(super.precount - super.prepos >= this.framesPerPacket * this.frameSize * this.packetsPerOggPage) {
               while(super.precount - super.prepos >= this.framesPerPacket * this.frameSize * this.packetsPerOggPage) {
                  if(this.packetCount == 0) {
                     this.writeOggPageHeader(this.packetsPerOggPage, 0);
                  }

                  while(this.packetCount < this.packetsPerOggPage) {
                     for(var5 = 0; var5 < this.framesPerPacket; ++var5) {
                        this.encoder.processData(super.prebuf, super.prepos, this.frameSize);
                        super.prepos += this.frameSize;
                     }

                     for(var5 = this.encoder.getProcessedDataByteSize(); super.buf.length - this.oggCount < var5; super.buf = var4) {
                        var3 = super.buf.length * 2;
                        var4 = new byte[var3];
                        System.arraycopy(super.buf, 0, var4, 0, this.oggCount);
                     }

                     super.buf[super.count + 27 + this.packetCount] = (byte)(255 & var5);
                     this.encoder.getProcessedData(super.buf, this.oggCount);
                     this.oggCount += var5;
                     ++this.packetCount;
                  }

                  if(this.packetCount >= this.packetsPerOggPage) {
                     this.writeOggPageChecksum();
                  }
               }

               System.arraycopy(super.prebuf, super.prepos, super.prebuf, 0, super.precount - super.prepos);
               super.precount -= super.prepos;
               super.prepos = 0;
               return;
            }
         } else {
            if(super.precount < super.prebuf.length) {
               return;
            }

            if(super.prepos <= 0) {
               return;
            }

            System.arraycopy(super.prebuf, super.prepos, super.prebuf, 0, super.precount - super.prepos);
            super.precount -= super.prepos;
            super.prepos = 0;
         }
      }
   }

   public synchronized int available() throws IOException {
      int var1 = super.available();
      int var2 = super.precount - super.prepos + super.in.available();
      if(this.encoder.getEncoder().getVbr()) {
         switch(this.mode) {
         case 0:
            return var1 + (27 + 2 * this.packetsPerOggPage) * (var2 / (this.packetsPerOggPage * this.framesPerPacket * 320));
         case 1:
            return var1 + (27 + 2 * this.packetsPerOggPage) * (var2 / (this.packetsPerOggPage * this.framesPerPacket * 640));
         case 2:
            return var1 + (27 + 3 * this.packetsPerOggPage) * (var2 / (this.packetsPerOggPage * this.framesPerPacket * 1280));
         default:
            return var1;
         }
      } else {
         int var3 = this.encoder.getEncoder().getEncodedFrameSize();
         if(this.channels > 1) {
            var3 += 17;
         }

         var3 *= this.framesPerPacket;
         var3 = var3 + 7 >> 3;
         int var4 = 27 + this.packetsPerOggPage * (var3 + 1);
         int var5;
         switch(this.mode) {
         case 0:
            var5 = this.framesPerPacket * 320 * this.encoder.getChannels();
            var1 += var4 * (var2 / (this.packetsPerOggPage * var5));
            return var1;
         case 1:
            var5 = this.framesPerPacket * 640 * this.encoder.getChannels();
            var1 += var4 * (var2 / (this.packetsPerOggPage * var5));
            return var1;
         case 2:
            var5 = this.framesPerPacket * 1280 * this.encoder.getChannels();
            var1 += var4 * (var2 / (this.packetsPerOggPage * var5));
            return var1;
         default:
            return var1;
         }
      }
   }

   private void writeOggPageHeader(int var1, int var2) {
      while(super.buf.length - super.count < 27 + var1) {
         int var3 = super.buf.length * 2;
         byte[] var4 = new byte[var3];
         System.arraycopy(super.buf, 0, var4, 0, super.count);
         super.buf = var4;
      }

      AudioFileWriter.writeOggPageHeader(super.buf, super.count, var2, (long)this.granulepos, this.streamSerialNumber, this.pageCount++, var1, new byte[var1]);
      this.oggCount = super.count + 27 + var1;
   }

   private void writeOggPageChecksum() {
      this.granulepos += this.framesPerPacket * this.frameSize * this.packetCount / 2;
      AudioFileWriter.writeLong(super.buf, super.count + 6, (long)this.granulepos);
      int var1 = OggCrc.checksum(0, super.buf, super.count, this.oggCount - super.count);
      AudioFileWriter.writeInt(super.buf, super.count + 22, var1);
      super.count = this.oggCount;
      this.packetCount = 0;
   }

   private void writeHeaderFrames() {
      int var1 = this.comment.length();
      if(var1 > 247) {
         this.comment = this.comment.substring(0, 247);
         var1 = 247;
      }

      int var2;
      while(super.buf.length - super.count < var1 + 144) {
         var2 = super.buf.length * 2;
         byte[] var3 = new byte[var2];
         System.arraycopy(super.buf, 0, var3, 0, super.count);
         super.buf = var3;
      }

      AudioFileWriter.writeOggPageHeader(super.buf, super.count, 2, (long)this.granulepos, this.streamSerialNumber, this.pageCount++, 1, new byte[]{(byte)80});
      this.oggCount = super.count + 28;
      AudioFileWriter.writeSpeexHeader(super.buf, this.oggCount, this.encoder.getSampleRate(), this.mode, this.encoder.getChannels(), this.encoder.getEncoder().getVbr(), this.framesPerPacket);
      this.oggCount += 80;
      var2 = OggCrc.checksum(0, super.buf, super.count, this.oggCount - super.count);
      AudioFileWriter.writeInt(super.buf, super.count + 22, var2);
      super.count = this.oggCount;
      AudioFileWriter.writeOggPageHeader(super.buf, super.count, 0, (long)this.granulepos, this.streamSerialNumber, this.pageCount++, 1, new byte[]{(byte)(var1 + 8)});
      this.oggCount = super.count + 28;
      AudioFileWriter.writeSpeexComment(super.buf, this.oggCount, this.comment);
      this.oggCount += var1 + 8;
      var2 = OggCrc.checksum(0, super.buf, super.count, this.oggCount - super.count);
      AudioFileWriter.writeInt(super.buf, super.count + 22, var2);
      super.count = this.oggCount;
      this.packetCount = 0;
   }
}
