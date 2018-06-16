package org.xiph.speex.spi;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class FilteredAudioInputStream extends AudioInputStream {

   public static final int DEFAULT_BUFFER_SIZE = 2048;
   protected InputStream in;
   protected byte[] buf;
   protected int count;
   protected int pos;
   protected int markpos;
   protected int marklimit;
   private final byte[] single;
   protected byte[] prebuf;
   protected int precount;
   protected int prepos;


   protected void checkIfStillOpen() throws IOException {
      if(this.in == null) {
         throw new IOException("Stream closed");
      }
   }

   public FilteredAudioInputStream(InputStream var1, AudioFormat var2, long var3) {
      this(var1, var2, var3, 2048);
   }

   public FilteredAudioInputStream(InputStream var1, AudioFormat var2, long var3, int var5) {
      this(var1, var2, var3, var5, var5);
   }

   public FilteredAudioInputStream(InputStream var1, AudioFormat var2, long var3, int var5, int var6) {
      super(var1, var2, var3);
      this.single = new byte[1];
      this.in = var1;
      if(var5 > 0 && var6 > 0) {
         this.buf = new byte[var5];
         this.count = 0;
         this.prebuf = new byte[var6];
         this.precount = 0;
         this.marklimit = var5;
         this.markpos = -1;
      } else {
         throw new IllegalArgumentException("Buffer size <= 0");
      }
   }

   protected void fill() throws IOException {
      this.makeSpace();

      while(true) {
         int var1 = this.in.read(this.prebuf, this.precount, this.prebuf.length - this.precount);
         if(var1 < 0) {
            break;
         }

         if(var1 > 0) {
            this.precount += var1;
            break;
         }
      }

   }

   protected void makeSpace() {
      if(this.markpos < 0) {
         this.pos = 0;
      } else if(this.pos >= this.buf.length) {
         int var1;
         if(this.markpos > 0) {
            var1 = this.pos - this.markpos;
            System.arraycopy(this.buf, this.markpos, this.buf, 0, var1);
            this.pos = var1;
            this.markpos = 0;
         } else if(this.buf.length >= this.marklimit) {
            this.markpos = -1;
            this.pos = 0;
         } else {
            var1 = this.pos * 2;
            if(var1 > this.marklimit) {
               var1 = this.marklimit;
            }

            byte[] var2 = new byte[var1];
            System.arraycopy(this.buf, 0, var2, 0, this.pos);
            this.buf = var2;
         }
      }

      this.count = this.pos;
   }

   public synchronized int read() throws IOException {
      return this.read(this.single, 0, 1) == -1?-1:this.single[0] & 255;
   }

   public synchronized int read(byte[] var1, int var2, int var3) throws IOException {
      this.checkIfStillOpen();
      if(var2 >= 0 && var2 <= var1.length && var3 >= 0 && var2 + var3 <= var1.length && var2 + var3 >= 0) {
         if(var3 == 0) {
            return 0;
         } else {
            int var4 = this.count - this.pos;
            if(var4 <= 0) {
               this.fill();
               var4 = this.count - this.pos;
               if(var4 <= 0) {
                  return -1;
               }
            }

            int var5 = var4 < var3?var4:var3;
            System.arraycopy(this.buf, this.pos, var1, var2, var5);
            this.pos += var5;
            return var5;
         }
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public synchronized long skip(long var1) throws IOException {
      this.checkIfStillOpen();
      if(var1 <= 0L) {
         return 0L;
      } else {
         int var3;
         if(this.pos < this.count) {
            var3 = this.count - this.pos;
            if((long)var3 > var1) {
               this.pos = (int)((long)this.pos + var1);
               return var1;
            } else {
               this.pos = this.count;
               return (long)var3;
            }
         } else {
            this.fill();
            var3 = this.count - this.pos;
            if(var3 <= 0) {
               return 0L;
            } else {
               long var4 = (long)var3 < var1?(long)var3:var1;
               this.pos = (int)((long)this.pos + var4);
               return var4;
            }
         }
      }
   }

   public synchronized int available() throws IOException {
      this.checkIfStillOpen();
      return this.count - this.pos;
   }

   public synchronized void mark(int var1) {
      if(var1 > this.buf.length - this.pos) {
         byte[] var2;
         if(var1 <= this.buf.length) {
            var2 = this.buf;
         } else {
            var2 = new byte[var1];
         }

         System.arraycopy(this.buf, this.pos, var2, 0, this.count - this.pos);
         this.buf = var2;
         this.count -= this.pos;
         this.pos = this.markpos = 0;
      } else {
         this.markpos = this.pos;
      }

      this.marklimit = var1;
   }

   public synchronized void reset() throws IOException {
      this.checkIfStillOpen();
      if(this.markpos < 0) {
         throw new IOException("Attempt to reset when no mark is valid");
      } else {
         this.pos = this.markpos;
      }
   }

   public boolean markSupported() {
      return true;
   }

   public synchronized void close() throws IOException {
      if(this.in != null) {
         this.in.close();
         this.in = null;
         this.buf = null;
         this.prebuf = null;
      }
   }
}
