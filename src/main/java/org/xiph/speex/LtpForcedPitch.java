package org.xiph.speex;

public class LtpForcedPitch extends Ltp {

   public final int quant(float[] var1, float[] var2, int var3, float[] var4, float[] var5, float[] var6, float[] var7, int var8, int var9, int var10, float var11, int var12, int var13, Bits var14, float[] var15, int var16, float[] var17, int var18) {
      if(var11 > 0.99F) {
         var11 = 0.99F;
      }

      for(int var19 = 0; var19 < var13; ++var19) {
         var7[var8 + var19] = var7[var8 + var19 - var9] * var11;
      }

      return var9;
   }

   public final int unquant(float[] var1, int var2, int var3, float var4, int var5, float[] var6, Bits var7, int var8, int var9, float var10) {
      if(var4 > 0.99F) {
         var4 = 0.99F;
      }

      for(int var11 = 0; var11 < var5; ++var11) {
         var1[var2 + var11] = var1[var2 + var11 - var3] * var4;
      }

      var6[0] = var6[2] = 0.0F;
      var6[1] = var4;
      return var3;
   }
}
