package org.xiph.speex;


public class Bits {

   public static final int DEFAULT_BUFFER_SIZE = 1024;
   private byte[] bytes;
   private int bytePtr;
   private int bitPtr;


   public void init() {
      this.bytes = new byte[1024];
      this.bytePtr = 0;
      this.bitPtr = 0;
   }

   public void advance(int var1) {
      this.bytePtr += var1 >> 3;
      this.bitPtr += var1 & 7;
      if(this.bitPtr > 7) {
         this.bitPtr -= 8;
         ++this.bytePtr;
      }

   }

   protected void setBuffer(byte[] var1) {
      this.bytes = var1;
   }

   public int peek() {
      return (this.bytes[this.bytePtr] & 255) >> 7 - this.bitPtr & 1;
   }

   public void read_from(byte[] var1, int var2, int var3) {
      System.arraycopy(var1, var2, this.bytes, 0, var3);

      this.bytePtr = 0;
      this.bitPtr = 0;
   }

   public int unpack(int var1) {
      int var2;
      for(var2 = 0; var1 != 0; --var1) {
         var2 <<= 1;
         var2 |= (this.bytes[this.bytePtr] & 255) >> 7 - this.bitPtr & 1;
         ++this.bitPtr;
         if(this.bitPtr == 8) {
            this.bitPtr = 0;
            ++this.bytePtr;
         }
      }

      return var2;
   }

   public void pack(int var1, int var2) {
      int var3;
      int var4;
      byte[] var5;
      for(var3 = var1; this.bytePtr + (var2 + this.bitPtr >> 3) >= this.bytes.length; this.bytes = var5) {
         var4 = this.bytes.length * 2;
         var5 = new byte[var4];
         System.arraycopy(this.bytes, 0, var5, 0, this.bytes.length);
      }

      for(; var2 > 0; --var2) {
         var4 = var3 >> var2 - 1 & 1;
         this.bytes[this.bytePtr] = (byte)(this.bytes[this.bytePtr] | var4 << 7 - this.bitPtr);
         ++this.bitPtr;
         if(this.bitPtr == 8) {
            this.bitPtr = 0;
            ++this.bytePtr;
         }
      }

   }

   public byte[] getBuffer() {
      return this.bytes;
   }

   public int getBufferSize() {
      return this.bytePtr + (this.bitPtr > 0?1:0);
   }
}
